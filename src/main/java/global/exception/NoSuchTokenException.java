package global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoSuchTokenException extends RuntimeException{
  public NoSuchTokenException(String message) {
    super(message);
  }
}
