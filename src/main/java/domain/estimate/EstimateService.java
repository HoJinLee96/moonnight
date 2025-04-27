package domain.estimate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import auth.crypto.Obfuscator;
import auth.verification.VerificationService;
import domain.estimate.Estimate.EstimateStatus;
import domain.user.User;
import domain.user.UserService;
import global.exception.ForbiddenException;
import global.exception.StatusDeleteException;
import infra.naver.sms.GuidanceService;
import infra.s3.AwsS3Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstimateService {
  private final EstimateRepository estimateRepository;
  private final UserService userService;
  private final AwsS3Service awsS3Service;
  private final VerificationService verificationService;
  private final GuidanceService guidanceService;
  private final Obfuscator obfuscator;
  private static final Logger logger = LoggerFactory.getLogger(EstimateService.class);
  
  //1.로그인한 유저 조회 OAUTH, LOCAL
  //2.휴대폰 인증 유저 조회 AUTH
  //3.휴대포번호, 견적서번호 조회 GUEST

  @Transactional
  public EstimateResponseDto registerEstimate(
      EstimateRequestDto estimateRequestDto,
      List<MultipartFile> images,
      int userId)  {
    
    List<String> imagesPath = null;
    try {
      if (images != null && !images.isEmpty()) {
        imagesPath = awsS3Service.uploadEstimateImages(images, estimateRequestDto.phone());
      }
    }catch(IOException e) {
      logger.warn("견적서 이미지 업로드 실패. phone: {}", estimateRequestDto.phone());
      e.printStackTrace();
    }
    
    User user;
    try {
      user = userService.getUserByUserId(userId);
    }catch(NoSuchElementException e) {
      user = null;
    }
    
    Estimate estimate = estimateRequestDto.toEntity(user, imagesPath);
    estimateRepository.save(estimate);
    
    try {
      guidanceService.sendEstimateInfoSms(estimate.getPhone(), obfuscator.encode(estimate.getEstimateSeq())+"");
    } catch (InvalidKeyException | JsonProcessingException | NoSuchAlgorithmException
        | UnsupportedEncodingException | URISyntaxException e) {
      logger.warn("견적서 등록 이후 문자 발송 실패. phone: {}, estimateId: {}",estimate.getPhone(), estimate.getEstimateSeq());
      e.printStackTrace();
    }
    return EstimateResponseDto.fromEntity(estimate, obfuscator);
  }
  
  // 내부 전용
  public Estimate getEstimateById(int estimateId) {
    return estimateRepository.findById(obfuscator.decode(estimateId))
        .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 견적서."));
  }
  
//  1
  public List<EstimateResponseDto> getMyAllEstimate(int userId) {
    return estimateRepository.findByUser_UserSeq(userId)
        .stream()
        .filter(e->e.getEstimateStatus()!=EstimateStatus.DELETE)
        .map(e->EstimateResponseDto.fromEntity(e, obfuscator))
        .collect(Collectors.toList());
  }
  
//  1
  public EstimateResponseDto getMyEstimateByEstimateId(int estimateId, int userId) {
    Estimate estimate = getAuthorizedEstimate(estimateId, userId);
    return EstimateResponseDto.fromEntity(estimate, obfuscator);
  }

//  2
  public List<EstimateResponseDto> getAllEstimateByAuthPhone(String phone) {
    verificationService.validateVerify(phone);
    return estimateRepository.findByPhone(phone)
        .stream()
        .filter(e->e.getEstimateStatus()!=EstimateStatus.DELETE)
        .map(e->EstimateResponseDto.fromEntity(e, obfuscator))
        .collect(Collectors.toList());
  }
  
//  2
  public EstimateResponseDto getEstimateByEstimateIdAndAuthPhone(int estimateId, String phone) {
    verificationService.validateVerify(phone);  // 본인 인증 로직
    Estimate estimate = getAuthorizedEstimate(estimateId, phone);
    return EstimateResponseDto.fromEntity(estimate, obfuscator);
  }
  
//  3
  public EstimateResponseDto getEstimateByEstimateIdAndPhone(int estimateId, String phone) {
    Estimate estimate = getAuthorizedEstimate(estimateId, phone);
    return EstimateResponseDto.fromEntity(estimate, obfuscator);
  }
  
//  1
  @Transactional
  public EstimateResponseDto updateMyEstimate(
      int estimateId,
      EstimateRequestDto estimateRequestDto,
      List<MultipartFile> images, 
      int userId) throws IOException {
    
    Estimate estimate = getAuthorizedEstimate(estimateId, userId);

    Estimate updatedEstimate = updateEstimate(estimate, estimateRequestDto, images);
    
    return EstimateResponseDto.fromEntity(updatedEstimate, obfuscator);
  }
  
//  2
  @Transactional
  public EstimateResponseDto updateEstimateByAuthPhone(
      int estimateId,
      EstimateRequestDto estimateRequestDto, 
      List<MultipartFile> images, 
      String phone) throws IOException {
    
    verificationService.validateVerify(phone);
    
    Estimate estimate = getAuthorizedEstimate(estimateId, phone);
    
    Estimate updatedEstimate = updateEstimate(estimate, estimateRequestDto, images);
    
    return EstimateResponseDto.fromEntity(updatedEstimate, obfuscator);
  }
  
  //회원 전용
  @Transactional
  public void deleteMyEstimate(int estimateId, int userId) {
    Estimate estimate = getAuthorizedEstimate(estimateId, userId);
    estimate.setEstimateStatus(EstimateStatus.DELETE);
  }
  
  //비회원 전용
  @Transactional
  public void deleteEstimateByAuth(int estimateId, String phone) {
    verificationService.validateVerify(phone);
    Estimate estimate = getAuthorizedEstimate(estimateId, phone);
    estimate.setEstimateStatus(EstimateStatus.DELETE);
  }

  private Estimate updateEstimate(Estimate estimate, EstimateRequestDto estimateRequestDto, List<MultipartFile> images) throws IOException {
    if (estimate.getImagesPath() != null && !estimate.getImagesPath().isEmpty()) {
      awsS3Service.deleteEstimateImages(estimate.getImagesPath());
    }
    List<String> imagesPath = null;
    if (images != null && !images.isEmpty()) {
      imagesPath = awsS3Service.uploadEstimateImages(images, estimateRequestDto.phone());
    }
    
    estimate.setName(estimateRequestDto.name());
    estimate.setEmail(estimateRequestDto.email());
    estimate.setEmailAgree(estimateRequestDto.emailAgree());
    estimate.setSmsAgree(estimateRequestDto.smsAgree());
    estimate.setCallAgree(estimateRequestDto.callAgree());
    estimate.setPostcode(estimateRequestDto.postcode());
    estimate.setMainAddress(estimateRequestDto.mainAddress());
    estimate.setDetailAddress(estimateRequestDto.detailAddress());
    estimate.setContent(estimateRequestDto.content());
    estimate.setImagesPath(imagesPath);
    estimateRepository.flush();

    return estimateRepository.getReferenceById(estimate.getEstimateSeq());
  }
  
  
  protected Estimate getEstimateOrThrow(int estimateId) {
    return estimateRepository.findById(obfuscator.decode(estimateId))
        .orElseThrow(() -> new NoSuchElementException("일치하는 견적서가 없습니다."));
  }
  
  protected void validateNotDeleted(Estimate estimate) {
    if (estimate.getEstimateStatus() == EstimateStatus.DELETE) {
        throw new StatusDeleteException("이미 삭제된 견적서 입니다.");
    }
  }
  
  private Estimate getAuthorizedEstimate(int estimateId, int userId) {
    Estimate estimate = getEstimateOrThrow(estimateId);
    
    if (estimate.getUser().getUserSeq() != userId) {
        throw new ForbiddenException("견적서를 조회할 수 없습니다.");
    }
    
    validateNotDeleted(estimate);
    
    return estimate;
  }

  private Estimate getAuthorizedEstimate(int estimateId, String phone) {
    Estimate estimate = getEstimateOrThrow(estimateId);
    
    if (!Objects.equals(estimate.getPhone(), phone)) {
        throw new ForbiddenException("견적서를 조회할 수 없습니다.");
    }
    
    validateNotDeleted(estimate);
    
    return estimate;
  }


  
  
  
//  @Transactional
//  public int registerEstimate(EstimateDto estimateDto, List<String> imageList) throws SQLException, IOException {
//    
//    if (imageList != null && imageList.size()!=0) {
//      List<File> imageFilesList = new ArrayList<>();
//      
//      for (String base64Image : imageList) {
//        String[] parts = base64Image.split(",");
//        String imageData = parts[1];
//
//        byte[] imageBytes = Base64.getDecoder().decode(imageData);
//
//        // 파일 생성 (임시 디렉토리에 저장)
//        File tempFile = File.createTempFile("image_", ".png");
//        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
//          outputStream.write(imageBytes);
//        }
//        imageFilesList.add(tempFile);
//      }
//      String imagesPath = awss3Dao.uploadImagesToS3(imageFilesList, estimateDto.getPhone()).orElseThrow(()->new IllegalStateException("이미지 업로드 실패 : 이미지 파일 업로드 경로 이상"));
//      estimateDto.setImagesPath(imagesPath);
//    }
//    
//    int result = estimateDao.registerEstimate(estimateDto);
//    if(result==0)throw new IllegalStateException("등록 실패 : 견적서를 저장하지 못했습니다.");
//    
//    return result;
//  }
//  
//  @Transactional
//  public int registerEstimate(EstimateDto estimateDto)throws SQLException {
//    int result = estimateDao.registerEstimate(estimateDto);
//    if (result == 0) {
//      throw new SQLException("estimate 등록 실패");
//    }
//    return result;
//  }
//
//  
//  public List<RequestEstimateDto> getEstimateByUserSeq(int userSeq) throws SQLException, IOException {
//    List<RequestEstimateDto> requestEstimateDtoList = new ArrayList<>();
//    List<EstimateDto> EstimateDtolist = estimateDao.getEstimateByUserSeq(userSeq);
//    
//    for(EstimateDto estimateDto : EstimateDtolist) {
//      RequestEstimateDto requestEstimateDto = new RequestEstimateDto();
//      requestEstimateDto.setEstimateDto(estimateDto);
//      if(estimateDto.getImagesPath() != "" && estimateDto.getImagesPath() != null) {
//        List<File> imageFileList = awss3Dao.getImageFileList(estimateDto.getImagesPath());
//        List<String> base64ImageList = new ArrayList<>();
//        for(File image : imageFileList) {
//          byte[] fileContent = Files.readAllBytes(image.toPath());
//          String base64Image = Base64.getEncoder().encodeToString(fileContent);
//          base64ImageList.add(base64Image);
//        }
//        requestEstimateDto.setImageList(base64ImageList);
//      }
//      requestEstimateDtoList.add(requestEstimateDto);
//    }
//    return requestEstimateDtoList; 
//  }
//
//  public EstimateDto getEstimateByEstimateSeq(int estimateSeq) throws SQLException, IOException {
//    Optional<EstimateDto> result = estimateDao.getEstimateByEstimateSeq(estimateSeq);
//    EstimateDto estimateDto = result.orElseThrow(() -> new IllegalArgumentException("일치하는 견적서가 없습니다."));
//    if(estimateDto.getImagesPath() != "" && estimateDto.getImagesPath() != null) {
//      List<File> imageFileList = awss3Dao.getImageFileList(estimateDto.getImagesPath());
//      List<String> base64ImageList = new ArrayList<>();
//      for(File image : imageFileList) {
//        byte[] fileContent = Files.readAllBytes(image.toPath());
//        String base64Image = Base64.getEncoder().encodeToString(fileContent);
//        base64ImageList.add(base64Image);
//      }
//      estimateDto.setImageList(base64ImageList);
//    }
//    
//    return estimateDto;
//  }
//  
//  public EstimateDto getEstimateTextByEstimateSeq(int estimateSeq) throws SQLException{
//    Optional<EstimateDto> result = estimateDao.getEstimateByEstimateSeq(estimateSeq);
//    return result.orElseThrow(() -> new IllegalArgumentException("일치하는 견적서가 없습니다."));
//  }
//  
//  public List<String> getEstimateImagesByEstimateSeq(int estimateSeq) throws SQLException, IOException{
//    String imagesPath = estimateDao.getImagesPath(estimateSeq);
//    if(imagesPath==""||imagesPath==null) {
////      throw new NotFoundException("견적서에 등록된 이미지가 없습니다.");
//    }
//    List<File> imageFileList = awss3Dao.getImageFileList(imagesPath);
//    List<String> base64ImageList = new ArrayList<>();
//    for(File image : imageFileList) {
//      byte[] fileContent = Files.readAllBytes(image.toPath());
//      String base64Image = Base64.getEncoder().encodeToString(fileContent);
//      base64ImageList.add(base64Image);
//    }
//    return base64ImageList;
//  }
//  
//  @Transactional
//  public boolean updateEstimate(int userSeq, EstimateDto newEstimateDto) throws SQLException, AccessDeniedException {
//    EstimateDto oldEstimateDto = estimateDao.getEstimateByEstimateSeq(newEstimateDto.getEstimateSeq())
//        .orElseThrow(() -> new IllegalArgumentException());
//    if (oldEstimateDto.getUserSeq() != userSeq) {
//      throw new AccessDeniedException("");
//    }
//    int result = estimateDao.updateEstimate(newEstimateDto);
//    if (result != 1) {
//      throw new SQLException("estimate 업데이트 실패 estimateSeq : " + newEstimateDto.getEstimateSeq());
//    }
//    return true;
//  }
//
//  @Transactional
//  public boolean deleteEstimate(int userSeq, int estimateSeq) throws SQLException,AccessDeniedException {
//    EstimateDto estimateDto =
//        estimateDao.getEstimateByEstimateSeq(estimateSeq).orElseThrow(() -> new IllegalArgumentException());
//    if (estimateDto.getUserSeq() != userSeq) {
//      throw new AccessDeniedException("");
//    }
//    int result = estimateDao.deleteEstimate(estimateSeq);
//    if (result != 1) {
//      throw new SQLException("estimate 삭제 실패 estimateSeq : " + estimateSeq);
//    }
//    return true;
//  }
//  
//  public HashMap<String,Object> getEstimateSearch(EstimateSearchRequest estimateSearchRequest) throws SQLException {
//    List<EstimateDto> list = estimateDao.getEstimateSearch(estimateSearchRequest);
//    int count = estimateDao.getCountEstimateSearch(estimateSearchRequest);
//    if(count==0||list.isEmpty()) {
////      throw new NoContentException("일치하는 데이터가 없습니다.");
//    }
//    HashMap<String,Object> result = new HashMap<String,Object>();
//    result.put("count", count);
//    result.put("list", list);
//    return result;
//  }
//
//  public HashMap<String, Integer> getCountAllStatus() throws SQLException {
//    int allCount = estimateDao.getCountByStatus("ALL");
//    int receivedCount = estimateDao.getCountByStatus("RECEIVED");
//    int inprogressCount = estimateDao.getCountByStatus("IN_PROGRESS");
//    int completedCount = estimateDao.getCountByStatus("COMPLETED");
//    int deleteCount = estimateDao.getCountByStatus("DELETE");
//    HashMap<String, Integer> countMap = new HashMap<String,Integer>();
//    countMap.put("ALL", allCount);
//    countMap.put("RECEIVED", receivedCount);
//    countMap.put("IN_PROGRESS", inprogressCount);
//    countMap.put("COMPLETED", completedCount);
//    countMap.put("DELETE", deleteCount);
//    return countMap;
//  }
//  
//  public int getCountStatus(String status) throws SQLException {
//    return estimateDao.getCountByStatus(status);
//  }
  
}
