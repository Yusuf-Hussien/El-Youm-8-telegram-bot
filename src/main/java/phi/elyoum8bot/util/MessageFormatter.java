package phi.elyoum8bot.util;

import org.springframework.stereotype.Component;
import phi.elyoum8bot.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MessageFormatter {

    public static String formatStudent(Student student) {
        return String.format("""
                📍 رقم الجلوس:  %s
 
                🧑 الاسم:  %s
      
                📊 المجموع:  %s
       
                📈 النسبة:  %s%%
         
                🏅 الترتيب:  %s
        
                🔄 الترتيب مكرر:  %s
                """,
                student.getSeatNumber().toString(),
                student.getArabicName(),
                student.getTotalDegree().toString(),
                student.getPercentage().toString(),
                student.getStudentRank().toString(),
                student.getRankWithDuplicates().toString()
        );
    }


    public static List<String> formatStudents(List<Student> students)
    {
        var response = new ArrayList<String>();
        if(students.isEmpty()) response.add("❌ لا يوجد طلاب بهذا الاسم");
        else if(students.size() > 50) response.add(formatError("large number of students with that name can't send them because of limitations set by the admin :)\ncant send over than 50 students"));
        else {
            int idx = 1;
            for (Student student : students) {
                response.add(String.valueOf(idx++));
                response.add(formatStudent(student));
            }
            if (students.size() == 1) response.removeFirst();
        }
        return response;
    }

    public static String formatError(String error)
    {
        return "❌ Error: \n\n"+"- "+error;
    }
}
