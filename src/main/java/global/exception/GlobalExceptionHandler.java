package global.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // (입력받은 값) 기본적인 데이터 유효성 부적합, 비즈니스 로직 기반 검증 부적합
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    logger.info("잘못된 요청: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(HttpStatus.BAD_REQUEST, "다시 확인해 주세요."));
  }

  // 비정상적인 상태 (정상이여야 하는 상황)
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
    logger.error("서버 내부 상태 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "죄송합니다 현재 서버에서 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요."));
  }

  // 권한 이상
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
    logger.warn("권한 부족: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(HttpStatus.FORBIDDEN, "적합하지 않은 권한입니다."));
  }

  // 로그인 실패, 인증번호 불일치 또는 지난 인증요청 확인 결과 불, 정지된 SNS 로그인 계정
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
    logger.info("인증 실패: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않습니다."));
  }

  
  @ExceptionHandler({NoSuchElementException.class, EmptyResultDataAccessException.class})
  public ResponseEntity<Object> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
    logger.info("데이터 조회 실패: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(HttpStatus.NOT_FOUND, "다시 확인해 주세요."));
  }

  // 회원가입 이메일 중복 시
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Object> handleDuplicateKeyException(DuplicateKeyException ex,
      WebRequest request) {
    logger.warn("중복된 값 입력: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(HttpStatus.CONFLICT, "중복 입니다."));
  }
  
  // 인증 시간 초과 시
//  @ExceptionHandler(TooManyRequestsException.class)
//  public ResponseEntity<Object> handle(TooManyRequestsException ex, WebRequest request) {
//    logger.warn("요청 횟수 초과: {}", ex.getMessage()); 
//    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
//        .body(createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수 초과 입니다.\n잠시후 시도해 주세요."));
//  }

  // 인증 시간 초과 시
  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<Object> handleTimeoutException(TimeoutException ex, WebRequest request) {
    logger.debug("시간 초과: {}", ex.getMessage()); 
    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(createErrorResponse(HttpStatus.REQUEST_TIMEOUT, "시간 초과 입니다.\n다시 시도해 주세요."));
  }

  // 데이터베이스 오류 발생 시
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
    logger.error("데이터베이스 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "죄송합니다 현재 서버에서 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요."));
  }

  // 입출력 예외 발생 시
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Object> handleIOException(IOException ex, WebRequest request) {
    logger.error("입출력 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "죄송합니다 현재 서버에서 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요."));
  }
  
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    logger.info("입출력 오류 발생: {}", ex.getMessage(), ex); 
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(createErrorResponse(HttpStatus.BAD_REQUEST,"업로드 가능한 최대 파일 크기를 초과했습니다."));
  }
  
  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
    logger.info("입출력 오류 발생: {}", ex.getMessage(), ex); 
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(createErrorResponse(HttpStatus.UNAUTHORIZED,"로그인 시간 만료."));
  }
  
  

  // 기타 예기치 못한 모든 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAllExceptions(Exception e, HttpServletRequest request) throws Exception {
    String uri = request.getRequestURI();
    
    if (uri.startsWith("/swagger") || uri.startsWith("/v3")) {
        throw e; // Swagger 요청은 예외 처리하지 않음
    }

    logger.error("서버 내부 예외 발생: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다."));
  }

  // 공통 에러 응답 생성
  private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", status.value());
    errorResponse.put("error", status.getReasonPhrase());
    errorResponse.put("message", message);
    errorResponse.put("timestamp", LocalDateTime.now());
    return errorResponse;
  }
}
