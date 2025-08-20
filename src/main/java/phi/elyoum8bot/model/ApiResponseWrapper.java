package phi.elyoum8bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class ApiResponseWrapper<T>{
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponseWrapper<T> success(T data)
    {
        return new ApiResponseWrapper<>(true,data,null);
    }

    public static <T> ApiResponseWrapper<T> error(String message)
    {
        return new ApiResponseWrapper<>(false,null,message);
    }
}