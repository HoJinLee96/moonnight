package domain.estimate;

import java.time.LocalDateTime;
import java.util.List;
import domain.user.User;
import global.util.StringListConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

// CREATE TABLE `estimate` (
// `estimate_seq` INT AUTO_INCREMENT PRIMARY KEY,
// `user_seq` INT,
// `name` VARCHAR(20) NOT NULL,
// `phone` VARCHAR(20) NOT NULL,
// `email` VARCHAR(50) NOT NULL,
// `email_agree` BOOLEAN DEFAULT FALSE,
// `sms_agree` BOOLEAN DEFAULT FALSE,
// `call_agree` BOOLEAN DEFAULT FALSE,
// `postcode` VARCHAR(10) NOT NULL,
// `main_address` VARCHAR(255) NOT NULL,
// `detail_address` VARCHAR(255) NOT NULL,
// `content` VARCHAR(5000),
// `images_path` VARCHAR(5000),
// `estimate_status` ENUM("RECEIVE", "IN_PROGRESS", "COMPLETE", "DELETE") NOT NULL,
// `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
// `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
// );
// ALTER TABLE `estimate` ADD CONSTRAINT `FK_user_TO_estimate_1`
// FOREIGN KEY ( `user_seq` )
// REFERENCES `user` ( `user_seq` );

@Entity
@Table(name = "estimate")
@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Estimate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "estimate_seq")
  private int estimateSeq;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_seq", foreignKey = @ForeignKey(name = "FK_user_TO_estimate_1"))
  private User user;

  @Column(name = "name", length = 20, nullable = false)
  private String name;

  @Column(name = "phone", length = 20, nullable = false)
  private String phone;

  @Column(name = "email", length = 50, nullable = false)
  private String email;
  
  @Column(name = "email_agree")
  private boolean emailAgree;

  @Column(name = "sms_agree")
  private boolean smsAgree;

  @Column(name = "call_agree")
  private boolean callAgree;

  @Column(name = "postcode", length = 10, nullable = false)
  private String postcode;

  @Column(name = "main_address", length = 255, nullable = false)
  private String mainAddress;

  @Column(name = "detail_address", length = 255, nullable = false)
  private String detailAddress;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "cleaning_service", nullable = false)
  private CleaningService cleaningService;

  @Column(name = "content", length = 5000)
  private String content;

  @Column(name = "images_path", length = 5000)
  @Convert(converter = StringListConverter.class) // List<String> 변환
  private List<String> imagesPath;

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

  public enum CleaningService {
    신축, 이사, 거주, 리모델링, 준공, 상가, 오피스, 기타
  }
  
  public enum EstimateStatus {
    RECEIVE, IN_PROGRESS, COMPLETE, DELETE
  }
  
}

