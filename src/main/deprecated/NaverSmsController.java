package api;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.management.openmbean.InvalidKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import deprecated.ValidateUtil;
import entity.Verification;
import infra.security.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import service.NaverSmsService;
import service.VerificationService;
import util.ApiResponse;

@RestController
@RequestMapping("/sms")
public class NaverSmsController {
  private final NaverSmsService smsService;
  private final VerificationService verificationService;
  private final RateLimiterService rateLimiterService;

  @Autowired
  public NaverSmsController(NaverSmsService smsService, VerificationService verificationService,
      RateLimiterService rateLimiterService) {
    this.smsService = smsService;
    this.verificationService = verificationService;
    this.rateLimiterService = rateLimiterService;
  }

  @PostMapping("/send/verify")
  public ResponseEntity<?> sendVerify(@RequestParam("phone") String reqPhone, HttpServletRequest request){

      String clientIp = ValidateUtil.validateIP(request);
      if (!rateLimiterService.isAllowed(clientIp)) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.createResponse(429, "요청 횟수 초과.\n잠시 후 시도 해주세요.", null));
      }
      
    try {
      Verification verificationDto = smsService.sendSms(reqPhone);
      Verification resultVerificationDto = verificationService.registerVerificationDto(verificationDto);
      return ResponseEntity.ok(ApiResponse.createResponse(200, "인증 코드 발송 성공.", resultVerificationDto));
      
    } catch (JsonProcessingException | RestClientException | InvalidKeyException
        | java.security.InvalidKeyException | NoSuchAlgorithmException
        | UnsupportedEncodingException | URISyntaxException | SQLException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.createResponse(500, "현재 이용할 수 없습니다.", null));
    }
  }

}
