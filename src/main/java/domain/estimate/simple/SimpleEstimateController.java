package domain.estimate.simple;

import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import auth.login.token.CustomUserDetails;
import auth.redis.RateLimiterStore;
import domain.estimate.EstimateResponseDto;
import global.annotation.ValidPhone;
import global.util.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/private/spem")
@RequiredArgsConstructor
public class SimpleEstimateController {
  
  private final SimpleEstimateService spemService;
  private final RateLimiterStore rateLimiter;

  // SimpleEstimate = Spem
  //1.로그인한 유저 조회 OAUTH, USER
  //2.휴대폰 인증 유저 조회 AUTH
  //3.휴대포번호, 견적서번호 조회 GUEST
  
  @PermitAll
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<SimpleEstimateResponseDto>> registerSimpleEstimate(
      @Valid @RequestBody SimpleEstimateRequestDto simpleEstimateRequestDto,
      HttpServletRequest request) {
    
    String clientIp = (String) request.getAttribute("clientIp");

    rateLimiter.isAllowedByIp(clientIp);
    
    SimpleEstimateResponseDto spemResponseDto = 
        spemService.registerSpem(simpleEstimateRequestDto, clientIp);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "간편 견적 신청 완료", spemResponseDto));
  }
  
//  1
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/user/get/all")
  public ResponseEntity<ApiResponse<List<EstimateResponseDto>>> getMyAllEstimateByUserId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<EstimateResponseDto> list = spemService.getMyAllSpem(userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }
  
//  1
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/user/get")
  public ResponseEntity<ApiResponse<SimpleEstimateResponseDto>> getMyEstimateByEstimateId(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int id
      ) throws AccessDeniedException {
    
    SimpleEstimateResponseDto spemResponseDto = spemService.getMySpemBySpemId(id, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", spemResponseDto));
  }
  
//  2
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/auth/get/all")
  public ResponseEntity<ApiResponse<List<SimpleEstimateResponseDto>>> getAllEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<SimpleEstimateResponseDto> list = 
        spemService.getAllSpemByAuthPhone(userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }
  
//  2
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/auth/get")
  public ResponseEntity<ApiResponse<SimpleEstimateResponseDto>> getEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int id) throws AccessDeniedException {
    
    SimpleEstimateResponseDto simpleEstimateResponseDto = 
        spemService.getSpemBySpemIdAndAuthPhone(id,userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", simpleEstimateResponseDto));
  }
  
//  3
  @PermitAll
  @PostMapping("/guest/get")
  public ResponseEntity<?> getEstimateByEstimateIdAndPhone(
      @ValidPhone @RequestParam String phone,
      @RequestParam int id) throws AccessDeniedException {
    
    SimpleEstimateResponseDto spemResponseDto = spemService.getSpemBySpemIdAndPhone(id,phone);
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", spemResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @DeleteMapping("/{simpleEstimateId}")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable int simpleEstimateId) throws AccessDeniedException{
    
    spemService.deleteMySpem(simpleEstimateId, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", null));
  }
  
  @PreAuthorize("hasRole('AUTH')")
  @DeleteMapping("/auth/{simpleEstimateId}")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable int simpleEstimateId) throws AccessDeniedException{
    
    spemService.deleteSpemByAuth(simpleEstimateId, userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", null));
  }
  
  
}
