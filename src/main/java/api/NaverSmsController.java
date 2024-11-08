package api;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.management.openmbean.InvalidKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import dto.VerifyResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.NaverSmsService;
import service.RateLimiterService;
import service.VerificationServices;

@RestController
@RequestMapping("/api")
public class NaverSmsController {
  private final NaverSmsService smsService;
  private final VerificationServices verificationServices;
  private final RateLimiterService rateLimiterService;

  @Autowired
  public NaverSmsController(NaverSmsService smsService, VerificationServices verificationServices,
      RateLimiterService rateLimiterService) {
    this.smsService = smsService;
    this.verificationServices = verificationServices;
    this.rateLimiterService = rateLimiterService;
  }

  @PostMapping("/verify/sendsms")
  public ResponseEntity<?> sendSms(@RequestParam String reqPhone, HttpServletRequest request)
      throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException,
      JsonProcessingException, RestClientException, InvalidKeyException,
      java.security.InvalidKeyException {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");

    String clientIp = request.getRemoteAddr();
    if (!rateLimiterService.isAllowed(clientIp)) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body("요청 횟수 초과. 잠시 후 시도 해주세요.");
    }

    try {
      VerifyResponseDto verifyResponseDto = smsService.sendSms(reqPhone);
      VerifyResponseDto newVerifyResponseDto = verificationServices.register(verifyResponseDto);
      HttpSession session = request.getSession();
      session.setAttribute("verifyResponseDto", newVerifyResponseDto);
    } catch (JsonProcessingException | RestClientException | InvalidKeyException
        | java.security.InvalidKeyException | NoSuchAlgorithmException
        | UnsupportedEncodingException | URISyntaxException | SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body("현재 이용할 수 없습니다.");
    }
    return ResponseEntity.status(HttpStatus.OK).headers(headers).body("발송 성공.");
  }

}
