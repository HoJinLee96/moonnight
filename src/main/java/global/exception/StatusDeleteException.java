package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//DB상 status == DELETE인 경우
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StatusDeleteException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(StatusDeleteException.class);

  public StatusDeleteException(String message) {
    super(message);
    logger.info(message);
  }
}
