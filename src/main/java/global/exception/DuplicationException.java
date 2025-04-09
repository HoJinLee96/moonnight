package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// create,register 요청한 값이 db에 이미 있는 경우
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicationException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(DuplicationException.class);
  public DuplicationException(String message) {
    super(message);
    logger.info(message);
    }
}