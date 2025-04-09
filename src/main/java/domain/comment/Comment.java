package domain.comment;

import java.time.LocalDateTime;
import domain.estimate.Estimate;
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

//create table `comment`(
//    `comment_seq` INT AUTO_INCREMENT PRIMARY KEY,
//    `user_seq` INT NOT NULL,
//    `estimate_seq` INT NOT NULL,
//    `comment_text` VARCHAR(250) NOT NULL,
//    `status`    ENUM("ACTIVE","DELETE") DEFAULT "ACTIVE",
//    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
//    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
//);
//ALTER TABLE `comment` ADD CONSTRAINT `FK_estimate_TO_comment_1` 
//FOREIGN KEY ( `estimate_seq` )
//REFERENCES `estimate` ( `estimate_seq` );
//
//ALTER TABLE `comment` ADD CONSTRAINT `FK_user_TO_comment_1`
//FOREIGN KEY ( `user_seq` )
//REFERENCES `user` ( `user_seq` );

@Entity
@Table(name="comment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_seq") 
  private int commentSeq;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_seq", nullable = false, foreignKey = @ForeignKey(name = "FK_user_TO_comment_1"))
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "estimate_seq", nullable = false, foreignKey = @ForeignKey(name = "FK_estimate_TO_comment_1"))
  private Estimate estimate;
  
  @Column(name = "comment_text", length = 250, nullable = false)
  private String commentText;
  
  @Enumerated(EnumType.STRING)
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "comment_status", nullable = false)
  private CommentStatus commentStatus;
  
  @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
  
  public enum CommentStatus{
    ACTIVE, DELETE 
  }
  
}
