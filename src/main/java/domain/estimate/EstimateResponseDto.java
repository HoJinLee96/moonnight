package domain.estimate;

import java.time.LocalDateTime;
import java.util.List;
import auth.crypto.Obfuscator;
import domain.estimate.Estimate.CleaningService;
import domain.estimate.Estimate.EstimateStatus;
import lombok.Builder;

@Builder
public record EstimateResponseDto(
     int estimateSeq,
     String name,
     String phone,
     String email,
     boolean emailAgree,
     boolean smsAgree,
     boolean callAgree,
     String postcode,
     String mainAddress,
     String detailAddress,
     CleaningService cleaningService,
     String content,
     List<String> images,
     EstimateStatus estimateStatus,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
    ) {
  
  public static EstimateResponseDto fromEntity(Estimate estimate, Obfuscator obfuscator) {
    return EstimateResponseDto.builder()
    .estimateSeq(obfuscator.encode(estimate.getEstimateSeq()))
    .name(estimate.getName())
    .phone(estimate.getPhone())
    .email(estimate.getEmail())
    .emailAgree(estimate.isEmailAgree())
    .smsAgree(estimate.isSmsAgree())
    .callAgree(estimate.isCallAgree())
    .postcode(estimate.getPostcode())
    .mainAddress(estimate.getMainAddress())
    .detailAddress(estimate.getDetailAddress())
    .cleaningService(estimate.getCleaningService())
    .content(estimate.getContent())
    .images(estimate.getImagesPath())
    .estimateStatus(estimate.getEstimateStatus())
    .createdAt(estimate.getCreatedAt())
    .updatedAt(estimate.getUpdatedAt())
    .build();
    
  }

}
