package auth.verification;

import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import auth.redis.RateLimiterStore;
import global.annotation.ValidEmail;
import global.annotation.ValidPhone;
import global.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/public/verify")
public class VerificationController {

  private final VerificationService verificationService;
  private final RateLimiterStore rateLimiter;

  @PostMapping("/compare/sms/jwt")
  public ResponseEntity<ApiResponse<Map<String,String>>> compareSmsVerificatioForJwt(
      @Valid @RequestBody VerificationPhoneRequestDto verificationPhoneRequestDto,
      HttpServletRequest request) throws TimeoutException{
    
    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByIp(clientIp);

    String token = verificationService.compareSmsForJwt(
        verificationPhoneRequestDto.phone(), 
        verificationPhoneRequestDto.verificationCode(), 
        clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "인증 성공.", Map.of("accessToken",token)));
  }
  
  @PostMapping("/compare/sms/uuid")
  public ResponseEntity<ApiResponse<Map<String,String>>> compareSmsVerification(
      @Valid @RequestBody VerificationPhoneRequestDto verificationPhoneRequestDto,
      HttpServletRequest request) throws TimeoutException{
    
    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByIp(clientIp);

    String token = verificationService.compareSms(
        verificationPhoneRequestDto.phone(), 
        verificationPhoneRequestDto.verificationCode(), 
        clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "인증 성공.", Map.of("X-Verfication-Phone-Token",token)));
  }
  
  @PostMapping("/compare/email/uuid")
  public ResponseEntity<ApiResponse<Map<String,String>>> compareEmailVerification(
      @Valid @RequestBody VerificationEmailRequestDto verificationEmailRequestDto,
      HttpServletRequest request) throws TimeoutException{
    
    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByIp(clientIp);

    String token = verificationService.compareEmail(
        verificationEmailRequestDto.email(), 
        verificationEmailRequestDto.verificationCode(), 
        clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "인증 성공.", Map.of("X-Verfication-Email-Token",token)));
  }
  
  @PostMapping("/sms")
  public ResponseEntity<ApiResponse<Void>> verifyToSms(
      @RequestBody @ValidPhone String phone,
      HttpServletRequest request) {
    
    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByPhone(phone);
    rateLimiter.isAllowedByIp(clientIp);
    
    verificationService.sendSmsVerificationCode(phone, clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "인증번호 요청 완료",null));
  }

  @PostMapping("/email")
  public ResponseEntity<ApiResponse<Void>> verifyToEmail(
      @RequestBody @ValidEmail String email,
      HttpServletRequest request) {
    
    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByEmail(email);
    rateLimiter.isAllowedByIp(clientIp);
    
    verificationService.sendEmailVerificationCode(email, clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "인증번호 요청 완료", null));
  }
//  @PostMapping("/compare")
//  public ResponseEntity<?> compare(Map<String, Object> requestBody, HttpServletRequest req, HttpSession session) throws TimeoutException{
//    
//    RequestUtil.getIP(req);
//    String to = RequestUtil.getString(requestBody, "to").orElseThrow(()->new IllegalArgumentException("리퀘스트 'to' 값 이상."));
//    String verificationCode = RequestUtil.getString(requestBody, "verificationCode").orElseThrow(()->new IllegalArgumentException("리퀘스트 'verificationCode' 값 이상."));
//    
//    ValidateUtil.validateEmailOrPhone(to);
//    ValidateUtil.validateVerificationCodeRegex(verificationCode);
//    
//    Verification verificationDto = verificationService.compareCode(to,verificationCode);
//    session.setAttribute("verificationSeq", verificationDto.getVerificationSeq());
//    
//    return ResponseEntity.ok(ApiResponse.createResponse(200, "인증 성공", null));
//  }
//  
//  @PostMapping("/send/sms")
//  public ResponseEntity<?> sendVerify(@RequestParam("phone") String reqPhone, HttpServletRequest request){
//    
//    String clientIp = RequestUtil.getIP(request);
//    if (!rateLimiterService.isAllowed(clientIp)) {
//      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.createResponse(429, "요청 횟수 초과.\n잠시 후 시도 해주세요.", null));
//    }
//    
//    try {
//      Verification verificationDto = smsService.sendSms(reqPhone);
//      Verification resultVerificationDto = verificationService.registerVerificationDto(verificationDto);
//      return ResponseEntity.ok(ApiResponse.createResponse(200, "인증 코드 발송 성공.", resultVerificationDto));
//      
//    } catch (JsonProcessingException | RestClientException | InvalidKeyException
//        | java.security.InvalidKeyException | NoSuchAlgorithmException
//        | UnsupportedEncodingException | URISyntaxException | SQLException e) {
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.createResponse(500, "현재 이용할 수 없습니다.", null));
//    }
//  }
//  
//  @PostMapping("/send/mail")
//  public ResponseEntity<?> sendMail(@RequestParam("email") String reqEmail, HttpServletRequest request){
//    
//    String clientIp = RequestUtil.getIP(request);
//    if (!rateLimiterService.isAllowed(clientIp)) {
//      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.createResponse(429, "요청 횟수 초과.\n잠시 후 시도 해주세요.", null));
//    }
//    
//    try {
//      Verification verificationDto = mailService.sendMail(reqEmail);
//      Verification resultVerificationDto = verificationService.registerVerificationDto(verificationDto);
//      return ResponseEntity.ok(ApiResponse.createResponse(200, "인증 코드 발송 성공.", resultVerificationDto));
//      
//    } catch (java.security.InvalidKeyException | UnsupportedEncodingException
//        | NoSuchAlgorithmException | SQLException | JsonProcessingException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.createResponse(500, "현재 이용할 수 없습니다.", null));
//    }
//  }


  
}
