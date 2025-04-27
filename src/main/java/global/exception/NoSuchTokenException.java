package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoSuchTokenException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(NoSuchTokenException.class);

  public NoSuchTokenException(String message) {
    super(message);
    logger.info(message);
  }
  
  public NoSuchTokenException(String message, String logMessage, Object...strings) {
    super(message);
    logger.info(logMessage, strings);
  }
  
}
