package domain.user;

import java.time.LocalDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// CREATE TABLE `user` (
// `user_seq` INT AUTO_INCREMENT PRIMARY KEY,
// `user_provider` ENUM ("LOCAL", "NAVER", "KAKAO") NOT NULL,
// `email` VARCHAR(50) NOT NULL,
// `password` VARCHAR(60),
// `name` VARCHAR(20) ,
// `birth` VARCHAR(10) ,
// `phone` VARCHAR(15) ,
// `user_status` ENUM("ACTIVE","STAY","STOP","DELETE") NOT NULL,
// `marketing_received_status` Boolean,
// `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
// `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
// );

@Entity
@Table(name = "user", uniqueConstraints = {
    @UniqueConstraint(name = "UK_user_email_user_provider", columnNames = {"email", "user_provider"}) })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_seq")
  private int userSeq;
  
  @Column(name = "user_provider", nullable=false)
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  private UserProvider userProvider;
  
  @Column(name = "email", length=50, nullable=false)
  private String email;
  
  @Column(name = "password", length=60)
  private String password;
  
  @Column(name = "name", length=20)
  private String name;
  
  @Column(name = "birth", length=10)
  private String birth;
  
  @Column(name = "phone", length=15)
  private String phone;
  
  @Column(name = "user_status", nullable=false)
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  private UserStatus userStatus;
  
  @Column(name = "marketing_received_status")
  private Boolean marketingReceivedStatus;
  
  @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  public static enum UserProvider {
    LOCAL, NAVER, KAKAO
  }

  public static enum UserStatus {
    ACTIVE, STAY, STOP, DELETE;
  }
  
}
