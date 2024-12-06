package api;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import dto.VerifyResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import service.NaverMailService;
import service.RateLimiterService;
import service.VerificationServices;

@RestController
@RequestMapping("/mail")
public class NaverMailController {
  
  NaverMailService mailService;
  RateLimiterService rateLimiterService;
  VerificationServices verificationServices;

  @Autowired
  public NaverMailController(NaverMailService mailService, RateLimiterService rateLimiterService,
      VerificationServices verificationServices) {
    this.mailService = mailService;
    this.rateLimiterService = rateLimiterService;
    this.verificationServices = verificationServices;
  }

  @PostMapping("/send/verify")
  public ResponseEntity<?> sendMail(@RequestParam String reqEmail, HttpServletRequest request){

    String clientIp = request.getRemoteAddr();
    if (!rateLimiterService.isAllowed(clientIp)) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

      try {
        VerifyResponseDto verifyResponseDto = mailService.sendMail(reqEmail);
        VerifyResponseDto newVerifyResponseDto = verificationServices.register(verifyResponseDto);
        request.getSession().setAttribute("verifyResponseDto", newVerifyResponseDto);
      } catch (java.security.InvalidKeyException | UnsupportedEncodingException
          | NoSuchAlgorithmException | SQLException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
    return ResponseEntity.status(HttpStatus.OK).build();
  }



}
