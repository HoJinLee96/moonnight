package global.exception;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import global.util.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // (입력받은 값) 기본적인 데이터 유효성 부적합, 비즈니스 로직 기반 검증 부적합
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(HttpStatus.BAD_REQUEST.value(), "다시 확인해 주세요.",null));
  }

  // 비정상적인 상태 (정상이여야 하는 상황)
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
    logger.error("서버 내부 상태 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "죄송합니다 현재 서버에서 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요.",
        null));
  }

  // 권한 이상
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
    logger.info("권한 부족: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.of(HttpStatus.FORBIDDEN.value(), "적합하지 않은 권한입니다.",null));
  }

  // 로그인 실패, 인증번호 불일치 또는 지난 인증요청 확인 결과 불, 정지된 SNS 로그인 계정
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
    logger.info("인증 실패: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(HttpStatus.UNAUTHORIZED.value(), "유효하지 않습니다.", null));
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(RuntimeException ex, WebRequest request) {
    logger.info("데이터 조회 실패: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.of(HttpStatus.NOT_FOUND.value(), "다시 확인해 주세요.", null));
  }
  
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFoundException(RuntimeException ex, WebRequest request) {
    logger.info("데이터 조회 실패: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.of(HttpStatus.NOT_FOUND.value(), "다시 확인해 주세요.", null));
  }

  // 회원가입 이메일 중복 시
  @ExceptionHandler(DuplicationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException(DuplicateKeyException ex,
      WebRequest request) {
    logger.info("중복된 값 입력: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.of(HttpStatus.CONFLICT.value(), "중복 입니다.", null));
  }
  
  // 요청 횟수 초과 시
  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ApiResponse<Void>> handle(TooManyRequestsException ex, WebRequest request) {
    logger.warn("요청 횟수 초과: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(ApiResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(), "요청 횟수 초과 입니다.\n잠시후 시도해 주세요.",null));
  }

  // 인증 시간 초과 시
  @ExceptionHandler(VerificationTimeoutException.class)
  public ResponseEntity<ApiResponse<Void>> handleTimeoutException(VerificationTimeoutException ex, WebRequest request) {
    logger.info("시간 초과: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(ApiResponse.of(HttpStatus.REQUEST_TIMEOUT.value(), "시간 초과 입니다.\n다시 시도해 주세요.", null));
  }

  // 입출력 예외 발생 시
  @ExceptionHandler(IOException.class)
  public ResponseEntity<ApiResponse<Void>> handleIOException(IOException ex, WebRequest request) {
    logger.error("입출력 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "죄송합니다 현재 서버에서 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요.", null));
  }
  
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    logger.info("입출력 오류 발생: {}", ex.getMessage(), ex); 
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ApiResponse.of(HttpStatus.BAD_REQUEST.value(),"업로드 가능한 최대 파일 크기를 초과했습니다.", null));
  }
  
  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex) {
    logger.info("입출력 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.of(HttpStatus.UNAUTHORIZED.value(),"로그인 시간 만료.", null));
  }
  
  // 기타 예기치 못한 모든 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception e, HttpServletRequest request) throws Exception {
    e.printStackTrace();
    logger.error("서버 내부 예외 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "예기치 못한 오류가 발생했습니다.", null));
  }

}
