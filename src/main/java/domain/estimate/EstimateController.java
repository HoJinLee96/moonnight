package domain.estimate;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import auth.redis.RateLimiterStore;
import auth.sign.token.CustomUserDetails;
import global.util.ApiResponse;
import global.validator.annotaion.ImageConstraint;
import global.validator.annotaion.ValidId;
import global.validator.annotaion.ValidPhone;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/estimate")
@MultipartConfig
@RequiredArgsConstructor
public class EstimateController {
  
  private final EstimateService estimateService;
  private final RateLimiterStore rateLimiter;

//  견적서 등록
  @PostMapping("/public/register")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> registerEstimate(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestPart("estimate") EstimateRequestDto estimateRequestDto,
      @ImageConstraint @RequestPart("images") List<MultipartFile> images,
      HttpServletRequest request) throws IOException {

    String clientIp = (String) request.getAttribute("clientIp");
    rateLimiter.isAllowedByIp(clientIp);
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.registerEstimate(estimateRequestDto, images, userDetails.getUserId());
    
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "견적서 등록 성공.", estimateResponseDto));
  }
  
//  유저 견적서 전체 조회
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @GetMapping("/private/user")
  public ResponseEntity<ApiResponse<List<EstimateResponseDto>>> getMyAllEstimateByUserId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<EstimateResponseDto> list = estimateService.getMyAllEstimate(userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }

//  유저 견적서 단건 조회
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @GetMapping("/private/user/{estimateId}")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> getMyEstimateByEstimateId(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable int estimateId
      ) throws AccessDeniedException {
    
    EstimateResponseDto estimateResponseDto = estimateService.getMyEstimateByEstimateId(estimateId,userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
//  인증된 전화번호로 견적서 전체 조회
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/private/auth")
  public ResponseEntity<ApiResponse<List<EstimateResponseDto>>> getAllEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<EstimateResponseDto> list = 
        estimateService.getAllEstimateByAuthPhone(userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }
  
//  인증된 전화번호로 견적서 단건 조회
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/auth/{estimateId}")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> getEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable int estimateId) throws AccessDeniedException {
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.getEstimateByEstimateIdAndPhone(estimateId,userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
//  비회원 견적서 조회
  @PostMapping("/public/guest")
  public ResponseEntity<?> getEstimateByEstimateIdAndPhone(
      @ValidId @RequestParam int estimateId,
      @ValidPhone @RequestParam String phone) throws AccessDeniedException {
    
    EstimateResponseDto estimateResponseDto = estimateService.getEstimateByEstimateIdAndPhone(estimateId,phone);
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
//  유저 견적서 수정
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PostMapping("/private/update/{estimateId}")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> updateEstimateByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ValidId @PathVariable int estimateId, 
      @Valid @RequestPart("estimate") EstimateRequestDto estimateRequestDto,
      @ImageConstraint @RequestPart("images") List<MultipartFile> images,
      HttpServletRequest request) throws IOException{
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.updateMyEstimate(estimateId, estimateRequestDto, images, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "수정 요청 성공", estimateResponseDto));
  }
  
//  인증된 전화번호로 견적서 수정
  @PreAuthorize("hasRole('AUTH')")
  @PostMapping("/private/auth/update")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> updateEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ValidId @PathVariable int estimateId, 
      @Valid @RequestPart EstimateRequestDto estimateRequestDto,
      @ImageConstraint @RequestPart("images") List<MultipartFile> images,
      HttpServletRequest request) throws IOException{
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.updateEstimateByAuthPhone(estimateId, estimateRequestDto, images, userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "수정 요청 성공", estimateResponseDto));
  }
  
//  사용자 견적서 삭제
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PostMapping("/private/delete")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ValidId @PathVariable int estimateId) throws AccessDeniedException{
    
    estimateService.deleteMyEstimate(estimateId, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공.", null));
  }
  
//  인증된 전화번호로 견적서 삭제
  @PreAuthorize("hasRole('AUTH') ")
  @PostMapping("/private/auth/delete")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ValidId @PathVariable int estimateId) throws AccessDeniedException{
    
    estimateService.deleteEstimateByAuth(estimateId, userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공.", null));
  }

//  @GetMapping("/getEstimateListByEstimateSearchRequest")
//  public ResponseEntity<?> getEstimateListByEstimateSearchRequest(@ModelAttribute EstimateSearchRequest estimateSearchRequest,HttpServletRequest request, HttpServletResponse res) throws SQLException {
//      estimateSearchRequest.validate();
//      
//      HashMap<String,Object> hashMap = estimateService.getEstimateSearch(estimateSearchRequest);
//      return ResponseEntity.ok().body(ApiResponse.createResponse(200, "조회 성공", hashMap));
//  }
//      
//  @GetMapping("/getCountAll")
//  public ResponseEntity<?> getCountAll(HttpServletRequest req, HttpServletResponse res){
//      HttpHeaders headers = new HttpHeaders();
//      headers.add("Content-Type", "application/json; charset=UTF-8");
//      HashMap<String, Integer> countMap = new HashMap<>();
//      try {
//        countMap = estimateService.getCountAllStatus();
//      } catch (SQLException e) {
//          e.printStackTrace();
//          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//      }
//      return ResponseEntity.ok().headers(headers).body(countMap);
//  }
//  
//  @GetMapping("/getEstimateByUserSeq")
//  public ResponseEntity<?> getEstimateByUserSeq(HttpServletRequest req, HttpServletResponse res, HttpSession session) {
//    HttpHeaders headers = new HttpHeaders();
//    headers.add("Content-Type", "application/json; charset=UTF-8");
//    
//    System.out.println("EstimateController.getEstimateByUserSeq() 실행");
//    UserRequestDto userDto = (UserRequestDto) session.getAttribute("userDto");
//    int userSeq = userDto.getUserSeq();
////      System.out.println(userSeq);
//    try {
//      List<RequestEstimateDto> list = estimateService.getEstimateByUserSeq(userSeq);
//      ObjectMapper objectMapper = new ObjectMapper();
//      objectMapper.registerModule(new JavaTimeModule());
//      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//      
//      String listJson = objectMapper.writeValueAsString(list);
////      Map<String, Object> responseBody = new HashMap<>();
////      responseBody.put("listJson", listJson);
////      System.out.println(listJson);
//      return ResponseEntity.ok().headers(headers).body(listJson);
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } catch (JsonProcessingException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } catch (IOException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//    
//  }
//  
//  @GetMapping("/getEstimateByEstimateSeq")
//  public ResponseEntity<?> getEstimateByEstimateSeq(@RequestParam("estimate_seq") String reqEstimate_seq) {
//    
//    reqEstimate_seq = reqEstimate_seq.trim();
//    if (!reqEstimate_seq.matches("\\d+")) { // 숫자가 아닌 값이 포함된 경우
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("estimate_seq는 숫자로만 작성되어야 합니다.");
//    }
//    
//    HttpHeaders headers = new HttpHeaders();
//    headers.add("Content-Type", "application/json; charset=UTF-8");
//    
//    int estimate_seq = Integer.parseInt(reqEstimate_seq);
//    EstimateDto estimateDto;
//    try {
//      estimateDto = estimateService.getEstimateByEstimateSeq(estimate_seq);
//    } catch (SQLException | IOException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 접속 불가.");
//    }  catch (NotFoundException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
//    }
//    
//    return ResponseEntity.status(HttpStatus.OK).headers(headers).body(estimateDto);
//  }
//  
//  @GetMapping("/getEstimateTextByEstimateSeq")
//  public ResponseEntity<?> getEstimateTextByEstimateSeq(@RequestParam("estimate_seq") String reqEstimate_seq) {
//      
//      reqEstimate_seq = reqEstimate_seq.trim();
//      if (!reqEstimate_seq.matches("\\d+")) { // 숫자가 아닌 값이 포함된 경우
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("estimate_seq는 숫자로만 작성되어야 합니다.");
//      }
//      
//      HttpHeaders headers = new HttpHeaders();
//      headers.add("Content-Type", "application/json; charset=UTF-8");
//      
//      int estimate_seq = Integer.parseInt(reqEstimate_seq);
//      EstimateDto estimateDto;
//      try {
//        estimateDto = estimateService.getEstimateTextByEstimateSeq(estimate_seq);
//      } catch (SQLException e) {
//        e.printStackTrace();
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 접속 불가.");
//      }  catch (NotFoundException e) {
//        e.printStackTrace();
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
//      }
//
//      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(estimateDto);
//  }
//  
//  @GetMapping("/getEstimateImagesByEstimateSeq")
//  public ResponseEntity<?> getEstimateImagesByEstimateSeq(@RequestParam("estimateSeq") String reqEstimateSeq) {
//      
//    reqEstimateSeq = reqEstimateSeq.trim();
//      if (!reqEstimateSeq.matches("\\d+")) { // 숫자가 아닌 값이 포함된 경우
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("estimate_seq는 숫자로만 작성되어야 합니다.");
//      }
//      
//      HttpHeaders headers = new HttpHeaders();
//      headers.add("Content-Type", "application/json; charset=UTF-8");
//      
//      int estimate_seq = Integer.parseInt(reqEstimateSeq);
//      List<String> imagesList;
//      try {
//        imagesList = estimateService.getEstimateImagesByEstimateSeq(estimate_seq);
//      } catch (SQLException | IOException e) {
//        e.printStackTrace();
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 접속 불가.");
//      }  catch (NotFoundException e) {
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
//      }
//
//      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(imagesList);
//  }
//  
//  @PostMapping("/delete")
//  public ResponseEntity<?> deleteEstimate(HttpSession session,@RequestBody int estimateSeq){
//    UserRequestDto userDto = (UserRequestDto) session.getAttribute("userDto");
//    int userSeq = userDto.getUserSeq();
//    return null;
//  }
//  
//  private boolean vlidateImages(List<MultipartFile> images) {
//    if (images != null && images.size()!=0) {
//      // 1. 이미지 개수 제한
//      if (images.size() > 10) {
//        throw new IllegalArgumentException("이미지는 최대 10장까지 업로드 가능합니다.");
//      }
//      // 2. 파일 크기 제한 (5MB)
//      for (MultipartFile file : images) {
//        if (file.getSize() > 5 * 1024 * 1024) {
//          throw new IllegalArgumentException("이미지 1장의 최대 크기는 10MB입니다.");
//        }
//      }
//    }
//    return true;
//  }
    
  
}
