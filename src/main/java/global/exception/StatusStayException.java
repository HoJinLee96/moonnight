package global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// DB Status == STAY
@ResponseStatus(HttpStatus.FORBIDDEN)
public class StatusStayException extends RuntimeException{
    public StatusStayException(String message) {
      super(message);
    }
}
