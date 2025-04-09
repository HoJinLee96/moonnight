package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// DB 생성자와 요청 조회자가 다른 경우
// AccessDenineException 유사 개념
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(ForbiddenException.class);

  public ForbiddenException(String message) {
  super(message);
  logger.info(message);
  }
}


