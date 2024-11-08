package api;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import jakarta.servlet.http.HttpSession;
import service.VerificationServices;

@RestController
@RequestMapping("/api")
public class VerificationController {

  VerificationServices verificationServices;

  @Autowired
  public VerificationController(VerificationServices verificationServices) {
    super();
    this.verificationServices = verificationServices;
  }

  @PostMapping("/verify/comparecode")
  public ResponseEntity<?> verifySms(HttpServletRequest req,@RequestParam String reqCode) {

    HttpSession session = req.getSession(false);
    VerifyResponseDto verifyResponseDto = (VerifyResponseDto) session.getAttribute("verifyResponseDto");
    
    if(verifyResponseDto ==null)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    
    LocalDateTime reqTime = verifyResponseDto.getRequestTime();
    long reqTimeMillis = reqTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long nowTimeMillis = System.currentTimeMillis();

    if (Math.abs(nowTimeMillis - reqTimeMillis) > 180000)
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();

    try {
    
      if (verificationServices.compareCode(verifyResponseDto,reqCode))
        return ResponseEntity.ok("성공");
      else
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
