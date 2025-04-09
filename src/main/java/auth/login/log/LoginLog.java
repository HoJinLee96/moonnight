package auth.login.log;

import java.time.LocalDateTime;
import domain.user.User.UserProvider;
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

//  create table `login_log`(
//      `login_log_seq` INT AUTO_INCREMENT PRIMARY KEY,
//      `user_provider` ENUM("NORMAL", "NAVER", "KAKAO") NOT NULL,
//      `email` VARCHAR(255) NOT NULL,
//      `ip` VARCHAR(45) NOT NULL,
//      `login_result` ENUM("SUCCESS","UPDATE_PASSWORD","INVALID_EMAIL", "INVALID_PASSWORD", "INVALID_OAUTH_TOKEN", "AUTHENTICATION_FAILED", "IP_BLOCKED", "SERVER_ERROR") NOT NULL,
//      `resolve_by` INT,
//      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
//      `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
//  );
//  ALTER TABLE `login_log` 
//  ADD CONSTRAINT `fk_login_log_resolve_by` 
//  FOREIGN KEY (`resolve_by`) 
//  REFERENCES `login_log` (`login_log_seq`) 
//  ON DELETE SET NULL;

@Entity
@Table(name="login_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name ="login_log_seq")
  private int loginLogSeq;

  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  @Column(name ="user_provider", nullable = false)
  private UserProvider userProvider;
  
  @Column(name ="email", length=100, nullable=false)
  private String email;
  
  @Column(name = "request_ip", length = 50, nullable = false)
  private String requestIp;
  
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "login_result", nullable = false)
  private LoginResult loginResult;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resolve_by", referencedColumnName = "login_log_seq", foreignKey = @ForeignKey(name = "fk_login_log_resolve_by"))
  private LoginLog resolveBy;
  
  @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
  
  public enum LoginResult {
    //로그인 성공
    SUCCESS,
    
    //비밀번호 업데이트
    UPDATE_PASSWORD,
    
    //일치하는 이메일 없음
    INVALID_EMAIL,
    
    //비밀번호 불일치
    INVALID_PASSWORD,
    
    //잘못된 OAUTH 토큰
//    INVALID_OAUTH_TOKEN,
    
    //IP 차단
    IP_BLOCKED,
    
    //서버 에러
//    SERVER_ERROR,
    
    // 계정이 STAY 상태 (이메일/휴대폰 인증 필요)
    ACCOUNT_LOCKED,
    
    // 계정이 STOP 상태 (정지됨)
    ACCOUNT_SUSPENDED,
    
    // 계정이 DELETE 상태 (탈퇴됨)
    ACCOUNT_DELETED,
    
    
    BLACKLISTED_TOKEN;
  }
  
}