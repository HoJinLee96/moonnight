package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dao.AWSS3Dao;
import dao.EstimateDao;
import dto.EstimateDto;
import dto.RequestEstimateDto;
import exception.AccessDeniedException;
import exception.NotFoundException;

@Service
public class EstimateService {

  private EstimateDao estimateDao;
  private AWSS3Dao awss3Dao;

  @Autowired
  public EstimateService(EstimateDao estimateDao, AWSS3Dao awss3Dao) {
    this.estimateDao = estimateDao;
    this.awss3Dao = awss3Dao;
  }

  @Transactional
  public boolean registerEstimate(EstimateDto estimateDto, List<String> imageList)
      throws SQLException, IOException {
    if (imageList != null) {
      List<File> imageFilesList = new ArrayList<>();
      for (String base64Image : imageList) {
        String[] parts = base64Image.split(",");
        String imageData = parts[1];

        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        // 파일 생성 (임시 디렉토리에 저장)
        File tempFile = File.createTempFile("image_", ".png");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
          outputStream.write(imageBytes);
        }
        imageFilesList.add(tempFile);
      }
      String imagesPath = awss3Dao.uploadImagesToS3(imageFilesList, estimateDto.getPhone());
      estimateDto.setImagesPath(imagesPath);
    }
    int result = estimateDao.registerEstimate(estimateDto);
    if (result == 0) {
      throw new SQLException("estimate 등록 실패");
    }
    return true;
  }
  
  @Transactional
  public boolean registerEstimate(EstimateDto estimateDto)throws SQLException {
    int result = estimateDao.registerEstimate(estimateDto);
    if (result == 0) {
      throw new SQLException("estimate 등록 실패");
    }
    return true;
  }

  public Optional<EstimateDto> getEstimate(int estimateSeq) throws SQLException {
    return estimateDao.getEstimate(estimateSeq);
  }
  
  public List<RequestEstimateDto> getEstimateByUserSeq(int userSeq) throws SQLException, IOException {
    List<RequestEstimateDto> requestEstimateDtoList = new ArrayList<>();
    List<EstimateDto> EstimateDtolist = estimateDao.getEstimateByUserSeq(userSeq);
    
    for(EstimateDto estimateDto : EstimateDtolist) {
      RequestEstimateDto requestEstimateDto = new RequestEstimateDto();
      requestEstimateDto.setEstimateDto(estimateDto);
      if(estimateDto.getImagesPath()!="") {
        List<File> imageFileList = awss3Dao.getImageFileList(estimateDto.getImagesPath());
        List<String> base64ImageList = new ArrayList<>();
        for(File image : imageFileList) {
          byte[] fileContent = Files.readAllBytes(image.toPath());
          String base64Image = Base64.getEncoder().encodeToString(fileContent);
          base64ImageList.add(base64Image);
        }
        requestEstimateDto.setImageList(base64ImageList);
      }
      requestEstimateDtoList.add(requestEstimateDto);
    }
    return requestEstimateDtoList; 
  }

  @Transactional
  public boolean updateEstimate(int userSeq, EstimateDto newEstimateDto) throws SQLException {
    EstimateDto oldEstimateDto = estimateDao.getEstimate(newEstimateDto.getEstimateSeq())
        .orElseThrow(() -> new NotFoundException());
    if (oldEstimateDto.getUserSeq() != userSeq) {
      throw new AccessDeniedException();
    }
    int result = estimateDao.updateEstimate(newEstimateDto);
    if (result != 1) {
      throw new SQLException("estimate 업데이트 실패 estimateSeq : " + newEstimateDto.getEstimateSeq());
    }
    return true;
  }

  @Transactional
  public boolean deleteEstimate(int userSeq, int estimateSeq) throws SQLException {
    EstimateDto estimateDto =
        estimateDao.getEstimate(estimateSeq).orElseThrow(() -> new NotFoundException());
    if (estimateDto.getUserSeq() != userSeq) {
      throw new AccessDeniedException();
    }
    int result = estimateDao.deleteEstimate(estimateSeq);
    if (result != 1) {
      throw new SQLException("estimate 삭제 실패 estimateSeq : " + estimateSeq);
    }
    return true;
  }
  
  public List<EstimateDto> getAllEstimate(int page) throws SQLException {
    return estimateDao.getAllEstimate(page);
  }

  public int getCountAll() throws SQLException {
    return estimateDao.getCountAll();
  }


}
