package phi.elyoum8bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import phi.elyoum8bot.service.TelegramBotHandler;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TelegramBotConfig {

    public final LongPollingBot telegramBotHandler;

    @Bean
    public Boolean telegramBot() {
        try {
            var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBotHandler);
        } catch (TelegramApiException e) {
            log.error("Error while initializing TelegramBot : {}", e.getMessage());
        }
        return true;
    }

}
