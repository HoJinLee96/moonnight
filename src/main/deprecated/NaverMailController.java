package api;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import deprecated.ValidateUtil;
import entity.Verification;
import infra.security.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import service.NaverMailService;
import service.VerificationService;
import util.ApiResponse;

@RestController
@RequestMapping("/mail")
public class NaverMailController {
  
  private final NaverMailService mailService;
  private final RateLimiterService rateLimiterService;
  private final VerificationService verificationService;

  @Autowired
  public NaverMailController(NaverMailService mailService, RateLimiterService rateLimiterService,
      VerificationService verificationService) {
    this.mailService = mailService;
    this.rateLimiterService = rateLimiterService;
    this.verificationService = verificationService;
  }

  @PostMapping("/send/verify")
  public ResponseEntity<?> sendMail(@RequestParam("email") String reqEmail, HttpServletRequest request){

    String clientIp = ValidateUtil.validateIP(request);
    if (!rateLimiterService.isAllowed(clientIp)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.createResponse(429, "요청 횟수 초과.\n잠시 후 시도 해주세요.", null));
    }

    try {
      Verification verificationDto = mailService.sendMail(reqEmail);
      Verification resultVerificationDto = verificationService.registerVerificationDto(verificationDto);
      return ResponseEntity.ok(ApiResponse.createResponse(200, "인증 코드 발송 성공.", resultVerificationDto));

    } catch (java.security.InvalidKeyException | UnsupportedEncodingException
        | NoSuchAlgorithmException | SQLException | JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.createResponse(500, "현재 이용할 수 없습니다.", null));
    }
  }


}
