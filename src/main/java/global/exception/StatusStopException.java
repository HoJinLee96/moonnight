package global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//DB Status == STOP
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StatusStopException extends RuntimeException{
  public StatusStopException(String message) {
    super(message);
  }
}
