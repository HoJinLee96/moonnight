package domain.estimate.simple;

import java.time.LocalDateTime;
import domain.estimate.Estimate;
import domain.estimate.Estimate.CleaningService;
import domain.estimate.Estimate.EstimateStatus;
import domain.user.User;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//CREATE TABLE `simple_estimate` (
//    `simple_estimate_seq` INT AUTO_INCREMENT PRIMARY KEY,
//    `user_seq` Int,
//    `phone` VARCHAR(20) NOT NULL, 
//    `cleaning_service` ENUM('신축', '이사', '거주', '리모델링', '준공', '상가', '오피스', '기타') NOT NULL,
//    `region` ENUM('서울', '부산', '대구', '인천', '광주', '대전', '울산', '세종', '경기', '강원', '충북', '충남', '전북', '전남', '경북', '경남', '제주') NOT NULL,
//    `status` ENUM("RECEIVE", "IN_PROGRESS", "COMPLETE", "DELETE") NOT NULL,
//    `ip` VARCHAR(45) NOT NULL,
//    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
//    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
//);

@Entity
@Table(name = "simple_estimate")
@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SimpleEstimate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "simple_estimate_seq")
  private int simpleEstimateSeq;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_seq", foreignKey = @ForeignKey(name = "FK_user_TO_simple_estimate_1"))
  private User user;
  
  @Column(name = "phone", length = 20, nullable = false)
  private String phone;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "cleaning_service", nullable = false)
  private CleaningService cleaningService;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "region", nullable = false)
  private Region region;
  
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "estimate_status", nullable = false)
  private EstimateStatus estimateStatus;
  
  @Column(name = "request_ip", length = 50, nullable = false)
  private String requestIp;
  
  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
  
  public enum Region{
    서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주
  }
  
}
