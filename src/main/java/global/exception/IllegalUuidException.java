package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IllegalUuidException extends RuntimeException{

  private static final Logger logger = LoggerFactory.getLogger(IllegalUuidException.class);

  public IllegalUuidException(String message) {
  super(message);
  logger.warn(message);
  }
}
