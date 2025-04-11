package global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
public class VerificationTimeoutException extends RuntimeException{
  private static final Logger logger = LoggerFactory.getLogger(TooManyLoginFailuresException.class);

  public VerificationTimeoutException(String message) {
  super(message);
  logger.info(message);
  }
}
