package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IllegalJwtException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(IllegalJwtException.class);

  public IllegalJwtException(String message) {
  super(message);
  logger.info(message);
  }
}
