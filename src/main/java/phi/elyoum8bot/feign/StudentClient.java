package phi.elyoum8bot.feign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import phi.elyoum8bot.model.ApiResponseWrapper;
import phi.elyoum8bot.model.Student;

import java.nio.charset.StandardCharsets;
import java.util.List;

@FeignClient(name = "student-client",url = "${application.services.students.url}")
public interface StudentClient {
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Student>> getStudent(@PathVariable long id);


    @GetMapping()
    public ResponseEntity<ApiResponseWrapper<List<Student>>>getAllByName(
            @RequestParam String name,
            @RequestParam(defaultValue="false") Boolean spellCheck,
            @RequestParam(defaultValue = "false") Boolean isMidName);


    @GetMapping("/all")
    public ResponseEntity<ApiResponseWrapper<List<Student>>>getAllSorted(
            @RequestParam(defaultValue = "0")   Integer page ,
            @RequestParam(defaultValue = "100") Integer size );

    @GetMapping("/ranged")
    public ResponseEntity<ApiResponseWrapper<List<Student>>>getAllBetween(
            @RequestParam Long from ,
            @RequestParam Long to);

    @GetMapping("/{id}/html")
    public ResponseEntity<byte[]> getStudentHtml(@PathVariable long id);

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getStudentPdf(@PathVariable long id);

    @GetMapping("/html")
    public ResponseEntity<byte[]> getStudentsHtml(
            @RequestParam @NotBlank(message = "Name cannot be blank") String name,
            @RequestParam(defaultValue = "false") Boolean spellCheck,
            @RequestParam(defaultValue = "false") Boolean isMidName);


    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getStudentsPdf(
            @RequestParam @NotBlank(message = "Name cannot be blank") String name,
            @RequestParam(defaultValue = "false") Boolean spellCheck,
            @RequestParam(defaultValue = "false") Boolean isMidName);


}
