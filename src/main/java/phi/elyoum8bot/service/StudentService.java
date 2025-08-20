package phi.elyoum8bot.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import phi.elyoum8bot.model.ApiResponseWrapper;
import phi.elyoum8bot.feign.StudentClient;
import phi.elyoum8bot.model.Student;
import phi.elyoum8bot.util.MessageFormatter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final String DEFAULT_ERROR_MESSAGE = "Server Is Not Available Right Now!\n  Try reach: @Yusuf_7ussien";
    private final StudentClient studentClient;

    public String getStudentWithId(String idStr)
    {
        long id = 0L;
        try{
             id = Long.parseLong(idStr);
        }catch (NumberFormatException e){
            return "🚫 من فضلك ادخل رقم بالانجليزية ولا يحتوي على حروف!";
        }
        ApiResponseWrapper<Student> response = ApiResponseWrapper.error(DEFAULT_ERROR_MESSAGE);
        try {
        response = studentClient.getStudent(id).getBody();
        }catch (FeignException e){
            return MessageFormatter.formatError(response.getMessage());
        }
        if(!response.isSuccess())
            return MessageFormatter.formatError(response.getMessage());

        Student student = response.getData();
        return MessageFormatter.formatStudent(student);
    }


    public List<String> getStudentsWithName(String name,Boolean spellCheck,Boolean isMidName)
    {
        ApiResponseWrapper<List<Student>> response = ApiResponseWrapper.error(DEFAULT_ERROR_MESSAGE);
        try {
             response = studentClient.getAllByName(name, spellCheck, isMidName).getBody();
        }catch (FeignException e){
            return List.of(MessageFormatter.formatError(response.getMessage()));
        }
        if(!response.isSuccess())
            return List.of(MessageFormatter.formatError(response.getMessage()));

        List<Student> students = response.getData();
        return MessageFormatter.formatStudents(students);
    }

    public byte[] getStudentHtml(String id)
    {
        return studentClient.getStudentHtml(Long.parseLong(id)).getBody();
    }

    public byte[] getStudentPdf(String id)
    {
        return studentClient.getStudentPdf(Long.parseLong(id)).getBody();
    }

    public byte[] getStudentsHtml(String arabicName,Boolean isMidName)
    {
        return studentClient.getStudentsHtml(arabicName,false,isMidName).getBody();
    }

    public byte[] getStudentsPdf(String arabicName,Boolean spellCheck,Boolean isMidName)
    {
        return studentClient.getStudentsPdf(arabicName,spellCheck,isMidName).getBody();
    }

}
