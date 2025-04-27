package domain.estimate.simple;

import java.time.LocalDateTime;
import auth.crypto.Obfuscator;
import domain.estimate.Estimate.CleaningService;
import domain.estimate.Estimate.EstimateStatus;
import domain.estimate.simple.SimpleEstimate.Region;
import lombok.Builder;

@Builder
public record SimpleEstimateResponseDto(
    int simpleEstimateSeq,
    String phone,
    CleaningService cleaningService,
    Region region,
    EstimateStatus estimateStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
    ) {

  public static SimpleEstimateResponseDto fromEntity(SimpleEstimate simpleEstimate, Obfuscator obfuscator) {
    return SimpleEstimateResponseDto.builder()
        .simpleEstimateSeq(obfuscator.encode(simpleEstimate.getSimpleEstimateSeq()))
        .phone(simpleEstimate.getPhone())
        .cleaningService(simpleEstimate.getCleaningService())
        .region(simpleEstimate.getRegion())
        .estimateStatus(simpleEstimate.getEstimateStatus())
        .createdAt(simpleEstimate.getCreatedAt())
        .updatedAt(simpleEstimate.getUpdatedAt())
        .build();
  }
}
