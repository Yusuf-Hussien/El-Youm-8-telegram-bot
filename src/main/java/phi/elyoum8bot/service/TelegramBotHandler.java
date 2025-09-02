package phi.elyoum8bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import phi.elyoum8bot.model.BotState;
import phi.elyoum8bot.model.User;


import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TelegramBotHandler extends TelegramLongPollingBot {


    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final StudentService studentService;
    private final ExecutorService executor;
    private final Map<Long , User> userStates;

    public TelegramBotHandler(StudentService studentService, ExecutorService executor, Map<Long , User> userStates) {
        super(buildBotOptions());
        this.studentService = studentService;
        this.executor = executor;
        this.userStates = userStates;
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.submit(() -> {

            if (update.hasMessage() && update.getMessage().hasText())
                handleMessage(update);

             else if (update.hasCallbackQuery())
                handleCallBackQuery(update);

        });
    }

    private void handleMessage(Update update)
    {
        Message message = update.getMessage();
        log.info("new message received from user: @{} '{} {}' says: {}",
                message.getFrom().getUserName(),
                message.getFrom().getFirstName(),
                message.getFrom().getLastName(),
                message.getText()
                );
        Long chatId = message.getChatId();
        String textMessage = message.getText();

        BotState state = userStates.getOrDefault(chatId, new User(BotState.MAIN_MENU)).getState();

        switch (state)
        {
            case MAIN_MENU -> sendMainMenu(chatId);

            case WAITING_SEAT_INPUT -> {
                handleSeatSearch(chatId, textMessage);
                userStates.put(chatId,new User(BotState.MAIN_MENU));
                sendMainMenu(chatId);
            }

            case WAITING_ARABIC_NAME_INPUT -> {
                handleArabicNameSearch(chatId, textMessage);
                userStates.put(chatId, new User(BotState.MAIN_MENU));
                sendMainMenu(chatId);
            }

            case WAITING_ARABIC_MID_NAME_INPUT -> {
                handleArabicMidNameSearch(chatId, textMessage);
                userStates.put(chatId, new User(BotState.MAIN_MENU));
                sendMainMenu(chatId);
            }
        }
    }


    private void handleCallBackQuery(Update update)
    {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (data) {
            case "SEARCH_SEAT" -> {
                sendMessageWithBackOption(chatId, "ارسل رقم الجلوس :");
                userStates.put(chatId, new User(BotState.WAITING_SEAT_INPUT));
            }
            case "SEARCH_ARABIC_NAME" -> {
                sendMessageWithBackOption(chatId, "ارسل الاسم (بالعربي):");
                userStates.put(chatId, new User(BotState.WAITING_ARABIC_NAME_INPUT));
            }
            case "SEARCH_ARABIC_MID_NAME" -> {
                sendMessageWithBackOption(chatId, "ارسل الاسم (بالعربي):");
                userStates.put(chatId, new User(BotState.WAITING_ARABIC_MID_NAME_INPUT));
            }
            case "BACK" -> {
                userStates.put(chatId, new User(BotState.MAIN_MENU));
                sendMainMenu(chatId);
            }
        }
    }

    private void sendMessages( Long chatId, List<String> messages)
    {
        AtomicInteger idx = new AtomicInteger(0);
        messages.forEach(message -> {
            sendMessage(chatId,message);
            if(idx.getAndIncrement() %2==1 || messages.size()==1){
            String seatNumber = message
                    .split("رقم الجلوس:")[1]
                    .split("\n")[0]
                    .trim();
            if(messages.size()==1) {
                Message sentMessage = sendMessage(chatId,"جاري تحميل الملف...");
                byte[] pdfFile = studentService.getStudentPdf(seatNumber);
                deleteMessage(chatId,sentMessage.getMessageId());
                if(pdfFile!=null) sendPdfFile(chatId,pdfFile,seatNumber,null);
            }
            else {
                byte[] htmlFile = studentService.getStudentHtml(seatNumber);
                if (htmlFile != null) sendHtmlFile(chatId, htmlFile, seatNumber);
            }
            }
        });
    }


    @Retryable(
            value = TelegramApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delayExpression = "${retry.backoff.delay:1000}", multiplier = 2)
    )
    public Message sendMessage( Long chatId, String message)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
        //sendMessage.setParseMode("Markdown");
        try {
           return execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("There were a problem while sending the message : {}",e.getMessage());
        }
        return null;
    }

    public void deleteMessage( Long chatId, int messageId)
    {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);
        try {
             execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("There were a problem while sending the message : {}",e.getMessage());
        }
    }


    @Retryable(
            value = TelegramApiException.class,
            maxAttempts = 4,
            backoff = @Backoff(delayExpression = "${retry.backoff.delay:1000}", multiplier = 2)
    )
    private void sendPdfFile(Long chatId, byte[] htmlBytes, String seatNumber,String caption) {
        try {
            InputFile file = new InputFile(new ByteArrayInputStream(htmlBytes), "search_for_"+seatNumber+".pdf");

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setCaption(caption!=null?caption:"📁 كمان تقدر تحفظها كملف");
            sendDocument.setDocument(file);

            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Error while sending HTML file: {}", e.getMessage());
        }
    }

    @Retryable(
            value = TelegramApiException.class,
            maxAttempts = 4,
            backoff = @Backoff(delayExpression = "${retry.backoff.delay:1000}", multiplier = 2)
    )
    private void sendHtmlFile(Long chatId, byte[] htmlBytes, String seatNumber) {
        try {
            InputFile file = new InputFile(new ByteArrayInputStream(htmlBytes), "student_"+seatNumber+".html");

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setCaption("🌐 كمان تقدر تفتح الملف ده من المتصفح");
            sendDocument.setDocument(file);

            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Error while sending HTML file: {}", e.getMessage());
        }
    }


    // ==== Menus ====
    private void sendMainMenu(Long chatId)
    {
        sendMenu(chatId,mainMenuKeyboard(),"🔎 بحث باستخدام : ");
    }


    private InlineKeyboardMarkup mainMenuKeyboard()
    {
        InlineKeyboardButton seatButton = new InlineKeyboardButton("📍 رقم الجلوس");
        seatButton.setCallbackData("SEARCH_SEAT");

        InlineKeyboardButton arabicNameButton = new InlineKeyboardButton("🧑 الاسم");
        arabicNameButton.setCallbackData("SEARCH_ARABIC_NAME");

        InlineKeyboardButton arabicMidNameButton = new InlineKeyboardButton("👨‍👩‍👦 اسم الأب او العائلة");
        arabicMidNameButton.setCallbackData("SEARCH_ARABIC_MID_NAME");

        return new InlineKeyboardMarkup(
                List.of(
                        List.of(seatButton, arabicNameButton),
                        List.of(arabicMidNameButton)
                )
        );
    }


    private void sendMessageWithBackOption(Long chatId, String text) {
        InlineKeyboardButton back = new InlineKeyboardButton("🔙 رجوع");
        back.setCallbackData("BACK");
        InlineKeyboardMarkup backMenu = new InlineKeyboardMarkup(List.of(List.of(back)));

        sendMenu(chatId,backMenu,text);
    }


    private void sendMenu(Long chatId,InlineKeyboardMarkup menu,String optionsMessage)
    {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(
                optionsMessage != null && !optionsMessage.isBlank() ?
                        optionsMessage :
                        "Choose an option:"
        );
        message.setReplyMarkup(menu);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error while sending menu: {}", e.getMessage());
        }
    }

    // ==== Handlers ====
    private void handleSeatSearch(Long chatId, String seatNumber) {
        String student = studentService.getStudentWithId(seatNumber);
        sendMessage(chatId, student);

        //loading message
        Message sentMessage = sendMessage(chatId,"جاري تحميل الملف...");

        byte[] pdfFile = studentService.getStudentPdf(seatNumber);
        if (pdfFile != null) {
            deleteMessage(chatId,sentMessage.getMessageId());
            sendPdfFile(chatId, pdfFile, "student_"+seatNumber+".pdf",null);
        }
    }

    private void handleArabicNameSearch(Long chatId, String name) {
        List<String> students = studentService.getStudentsWithName(name,false,false);
        sendMessages(chatId, students);
        if (students.size() > 1) {
            Message sentMessage = sendMessage(chatId,"جاري تحميل الجدول...");
            byte[] pdfFile = studentService.getStudentsPdf(name,false,false);
            sendPdfFile(chatId, pdfFile, name,"📁 كمان تقدر تحفظ الجدول كملف");
            deleteMessage(chatId,sentMessage.getMessageId());
        }
    }

    private void handleArabicMidNameSearch(Long chatId, String name) {
        Message sentMessage = sendMessage(chatId,"🔎 يتم البحث...");
        List<String> students = studentService.getStudentsWithName(name,false,true);
        deleteMessage(chatId,sentMessage.getMessageId());
        sendMessages(chatId, students);
        if (students.size() > 1) {
            Message sentMessage2 = sendMessage(chatId,"جاري تحميل الجدول...");
            byte[] pdfFile = studentService.getStudentsPdf(name,false,true);
            sendPdfFile(chatId, pdfFile, name,"📁 كمان تقدر تحفظ الجدول كملف");
            deleteMessage(chatId,sentMessage2.getMessageId());
        }
    }


    @Override
    public String getBotUsername() {
        return this.botName;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }

    private static DefaultBotOptions buildBotOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setGetUpdatesTimeout(60); // long polling timeout in seconds
        options.setGetUpdatesLimit(100);  // how many updates to fetch at once
        options.setProxyType(DefaultBotOptions.ProxyType.NO_PROXY); // or SOCKS4/HTTP
        return options;
    }
}
