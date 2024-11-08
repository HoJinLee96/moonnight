package dto;

import java.util.List;

public class RequestEstimateDto {
  
  private EstimateDto estimateDto;
  private List<String> ImageList;
  
  public EstimateDto getEstimateDto() {
    return estimateDto;
  }

  public List<String> getImageList() {
    return ImageList;
  }

  public void setEstimateDto(EstimateDto estimateDto) {
    this.estimateDto = estimateDto;
  }

  public void setImageList(List<String> imageList) {
    ImageList = imageList;
  }
  
  

}
