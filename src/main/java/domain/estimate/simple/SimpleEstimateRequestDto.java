package domain.estimate.simple;

import domain.estimate.Estimate;
import domain.estimate.Estimate.CleaningService;
import domain.estimate.Estimate.EstimateStatus;
import domain.estimate.simple.SimpleEstimate.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SimpleEstimateRequestDto(
    Integer estimateSeq,
    
    @NotBlank(message = "{validation.user.phone.required}")
    @Pattern(regexp = "^\\d{3,4}-\\d{3,4}-\\d{4}$", message = "{validation.user.phone.invalid}")
    @Size(max = 20, message = "{validation.user.phone.length}")
    String phone,
    
    @NotBlank(message = "{validation.estimate.cleaning_service.required}")
    CleaningService cleaningService,
    
    @NotBlank(message = "{validation.estimate.region.required}")
    Region region
    
    ) {
  
  public SimpleEstimate toEntity() {
    return SimpleEstimate.builder()
        .phone(phone)
        .cleaningService(cleaningService)
        .region(region)
        .estimateStatus(EstimateStatus.RECEIVE)
        .build();
  }

}
