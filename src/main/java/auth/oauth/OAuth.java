package auth.oauth;

import java.time.LocalDateTime;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//  CREATE TABLE `oauth` (
//  `oauth_seq` INT AUTO_INCREMENT PRIMARY KEY,
//  `user_seq`  INT NOT NULL,
//  `oauth_provider`  ENUM ("NAVER", "KAKAO") NOT NULL,
//  `id`    VARCHAR(255) UNIQUE NOT NULL,
//  `oauth_status`    ENUM("ACTIVE","STAY","STOP","DELETE") DEFAULT "ACTIVE",
//  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
//  `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
//  );
//ALTER TABLE `oauth` ADD CONSTRAINT `FK_user_TO_oauth_1`
//FOREIGN KEY ( `user_seq` )
//REFERENCES `user` ( `user_seq` );

@Entity
@Table(name = "oauth")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OAuth {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "oauth_seq") 
  private int oauthSeq;
  
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_seq", nullable = false, foreignKey = @ForeignKey(name = "FK_user_TO_address_1"))
  private User user;

  @Column(name = "oauth_provider", nullable=false)
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  private OAuthProvider oauthProvider;
  
  @Column(name = "id", length=255, nullable=false)
  private String id;
  
  @Column(name = "oauth_status", nullable=false)
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  private OAuthStatus oauthStatus;
  
  @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
  
  public enum OAuthProvider {
    NAVER, KAKAO
  }
  
  public enum OAuthStatus {
    ACTIVE, STAY, STOP, DELETE
  }
  
}
