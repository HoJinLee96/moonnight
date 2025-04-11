package domain.estimate;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import auth.redis.RateLimiterStore;
import auth.sign.token.CustomUserDetails;
import global.annotation.ImageConstraint;
import global.annotation.ValidPhone;
import global.util.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/private/estimate")
@MultipartConfig
@RequiredArgsConstructor
public class EstimateController {
  
  private final EstimateService estimateService;
  private final RateLimiterStore rateLimiter;

  @PermitAll
  @PostMapping("/register")
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
  
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/user/get")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> getMyEstimateByEstimateId(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int id
      ) throws AccessDeniedException {
     
    EstimateResponseDto estimateResponseDto = estimateService.getMyEstimateByEstimateId(id,userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/user/get/all")
  public ResponseEntity<ApiResponse<List<EstimateResponseDto>>> getMyAllEstimateByUserId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<EstimateResponseDto> list = estimateService.getMyAllEstimate(userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }

  
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/auth/get/all")
  public ResponseEntity<ApiResponse<List<EstimateResponseDto>>> getAllEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<EstimateResponseDto> list = 
        estimateService.getAllEstimateByAuthPhone(userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", list));
  }
  
  @PreAuthorize("hasRole('AUTH')")
  @GetMapping("/auth/get")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> getEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int id) throws AccessDeniedException {
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.getEstimateByEstimateIdAndPhone(id,userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
  @PermitAll
  @PostMapping("/guest/get")
  public ResponseEntity<?> getEstimateByEstimateIdAndPhone(
      @RequestParam int id,
      @ValidPhone @RequestParam String phone) throws AccessDeniedException {
    
    EstimateResponseDto estimateResponseDto = estimateService.getEstimateByEstimateIdAndPhone(id,phone);
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 요청 성공", estimateResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @PostMapping("/update")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> updateEstimateByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody EstimateRequestDto estimateRequestDto,
      @ImageConstraint @RequestPart("images") List<MultipartFile> images,
      HttpServletRequest request) throws IOException{
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.updateMyEstimate(estimateRequestDto, images, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", estimateResponseDto));
  }
  
  @PreAuthorize("hasRole('AUTH')")
  @PostMapping("/auth/update")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> updateEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody EstimateRequestDto estimateRequestDto,
      @ImageConstraint @RequestPart("images") List<MultipartFile> images,
      HttpServletRequest request) throws IOException{
    
    EstimateResponseDto estimateResponseDto = 
        estimateService.updateEstimateByAuthPhone(estimateRequestDto, images, userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", estimateResponseDto));
  }
  
  
  @PreAuthorize("hasRole('OAUTH')")
  @PostMapping("/delete")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody EstimateRequestDto estimateRequestDto) throws AccessDeniedException{
    
    estimateService.deleteMyEstimate(estimateRequestDto.estimateSeq(), userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", null));
  }
  
  @PreAuthorize("hasRole('AUTH')")
  @PostMapping("/auth/delete")
  public ResponseEntity<ApiResponse<EstimateResponseDto>> deleteEstimateByAuthPhone(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody EstimateRequestDto estimateRequestDto) throws AccessDeniedException{
    
    estimateService.deleteEstimateByAuth(estimateRequestDto.estimateSeq(), userDetails.getUsername());
    
    return ResponseEntity.ok(ApiResponse.of(200, "삭제 요청 성공", null));
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
