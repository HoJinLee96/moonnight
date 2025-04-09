package auth.verification;

import java.time.LocalDateTime;
import java.util.Arrays;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//  create table `verification`(
//      `verification_seq` INT AUTO_INCREMENT PRIMARY KEY ,
//      `request_ip` VARCHAR(50) NOT NULL,
//      `to` VARCHAR(50) NOT NULL,
//      `verification_code` VARCHAR(6) NOT NULL,
//      `send_status` INT NOT NULL,
//      `created_at` DATETIME DEFAULT NOW(),
//      `verify` BOOLEAN,
//      `verify_at` DATETIME
//      );

@Entity
@Table(name = "verification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Verification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "verification_seq")
  private int verificationSeq;

  @Column(name = "request_ip", length = 50, nullable = false)
  private String requestIp;

  @Column(name = "to", length = 50, nullable = false)
  private String to; 

  @Column(name = "verification_code", length = 6, nullable = false)
  private String verificationCode;

  @Column(name = "send_status", nullable = false)
  private int sendStatus;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;
  
  @Column(name = "verify")
  private Boolean verify;
  
  @Column(name = "verify_at")
  private LocalDateTime verifyAt; 
  
  public SendStatus getSendStatusEnum() {
    return SendStatus.fromCode(this.sendStatus);
}
    
  public enum SendStatus {
    Informational(1),
    SUCCESS(2),
    Redirection(3),
    FAILURE(4), 
    SERVER_ERROR(5),
    UNKNOWN(-1);  

    private final int category;

    SendStatus(int category) {
        this.category = category;
    }

    public static SendStatus fromCode(int code) {
        int firstDigit = code / 100; 

        return Arrays.stream(values())
                .filter(status -> status.category == firstDigit)
                .findFirst()
                .orElse(UNKNOWN); 
    }

    public int getCategory() {
        return category;
    }
  }

}