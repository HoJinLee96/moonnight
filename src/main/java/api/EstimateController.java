package api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.EstimateDto;
import dto.EstimateDto.Status;
import dto.RequestEstimateDto;
import dto.UserDto;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.EstimateService;
import service.NaverSmsService;
import service.RateLimiterService;

@RestController
@RequestMapping("/estimate")
@MultipartConfig
public class EstimateController {
  
  EstimateService estimateService;
  RateLimiterService rateLimiterService;
  NaverSmsService smsService;

  @Autowired
  public EstimateController(EstimateService estimateService, RateLimiterService rateLimiterService, NaverSmsService smsService) {
    this.estimateService = estimateService;
    this.rateLimiterService = rateLimiterService;
    this.smsService = smsService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerEstimate(
      @RequestBody RequestEstimateDto requestEstimateDto,
      HttpServletRequest request, 
      HttpSession session) {
    
    String clientIp = request.getRemoteAddr();
    if (!rateLimiterService.isAllowed(clientIp)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
    
    UserDto userDto = (UserDto) session.getAttribute("userDto");
    
    EstimateDto estimateDto = requestEstimateDto.getEstimateDto();
    estimateDto.setStatus(EstimateDto.Status.RECEIVED);
    
    if(userDto!=null)
      estimateDto.setUserSeq(userDto.getUserSeq());
    
    List<String> imageList =  requestEstimateDto.getImageList();
      
    try {
      int result = estimateService.registerEstimate(estimateDto,imageList);
      smsService.sendEstimateSeq(estimateDto.getPhone(), result);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException | IOException | RestClientException | InvalidKeyException | NoSuchAlgorithmException | URISyntaxException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
  }
  
  @PostMapping("/speedRegister")
  public ResponseEntity<?> speedRegisterEstimate(
      @RequestParam("phone") String phone,
      @RequestParam("cleaningService") String cleaningService,
      @RequestParam("region") String region,
      HttpServletRequest request,
      HttpSession session) {
    
    String clientIp = request.getRemoteAddr();
    if (!rateLimiterService.isAllowed(clientIp)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
    
    EstimateDto estimateDto = new EstimateDto(phone, cleaningService, region, Status.RECEIVED);
    
    UserDto userDto = (UserDto) session.getAttribute("userDto");
    if(userDto!=null)
      estimateDto.setUserSeq(userDto.getUserSeq());

    try {
      int result = estimateService.registerEstimate(estimateDto);
      smsService.sendEstimateSeq(estimateDto.getPhone(), result);

      return ResponseEntity.status(HttpStatus.OK).build();
      
    } catch (SQLException | RestClientException | JsonProcessingException | InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | URISyntaxException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } 
  }

  @GetMapping("/getAllEstimate")
  public ResponseEntity<?> getAllEstimate(HttpServletRequest request, HttpServletResponse res) {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json; charset=UTF-8");
      
      String reqPage = request.getParameter("page");
      String reqSize = request.getParameter("size");
      
      if(reqPage==null || reqPage=="") {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
      if(reqSize==null || reqPage=="") {
        reqSize = "50";
      }
      
      int page = Integer.parseInt(reqPage);
      int size = Integer.parseInt(reqSize);
      
      List<EstimateDto> list = null;
      try {
          list = estimateService.getAllEstimate(page,size);
      } catch (SQLException e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      String jsonResponse = "";
      try {
          jsonResponse = objectMapper.writeValueAsString(list);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

      return ResponseEntity.ok().headers(headers).body("{\"list\": " + jsonResponse + "}");
  }
      
  @GetMapping("/getCountAll")
  public ResponseEntity<?> getCountAll(HttpServletRequest req, HttpServletResponse res){
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json; charset=UTF-8");

      int total = 0;
      try {
          total = estimateService.getCountAll();
      } catch (SQLException e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .headers(headers)
                               .body("{\"error\": \"An error occurred while fetching the count.\"}");
      }
      return ResponseEntity.ok()
                           .headers(headers)
                           .body("{\"totalCount\": " + total + "}");
  }
  
  @GetMapping("/getEstimateByUserSeq")
  public ResponseEntity<?> getEstimateByUserSeq(HttpServletRequest req, HttpServletResponse res, HttpSession session) {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json; charset=UTF-8");

      System.out.println("EstimateController.getEstimateByUserSeq() 실행");
      UserDto userDto = (UserDto) session.getAttribute("userDto");
      int userSeq = userDto.getUserSeq();
//      System.out.println(userSeq);
      try {
        List<RequestEstimateDto> list = estimateService.getEstimateByUserSeq(userSeq);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

      String listJson = objectMapper.writeValueAsString(list);
//      Map<String, Object> responseBody = new HashMap<>();
//      responseBody.put("listJson", listJson);
//      System.out.println(listJson);
        return ResponseEntity.ok().headers(headers).body(listJson);
      } catch (SQLException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      } catch (JsonProcessingException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

  }
  
  @PostMapping("/delete")
  public ResponseEntity<?> deleteEstimate(HttpSession session,@RequestBody int estimateSeq){
    UserDto userDto = (UserDto) session.getAttribute("userDto");
    int userSeq = userDto.getUserSeq();
    
    
    return null;
  }
    
  
}
