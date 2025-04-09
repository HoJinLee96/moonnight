package global.util;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
  
    public static <T> ApiResponse<T> of(int status, String message, T data) {
      return new ApiResponse<>(status, message, data, LocalDateTime.now());
    }
  
}
