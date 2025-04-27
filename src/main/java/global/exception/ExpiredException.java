package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//기간 만료
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ExpiredException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(ExpiredException.class);

  public ExpiredException(String message) {
  super(message);
  logger.info(message);
  }
}

