package domain.address;

import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;
import domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

//CREATE TABLE address (
//address_seq   INT AUTO_INCREMENT PRIMARY KEY,
//user_seq  INT NOT NULL,
//name VARCHAR(20),
//postcode  VARCHAR(10) NOT NULL,
//main_address  VARCHAR(255)    NOT NULL,
//detail_address    VARCHAR(255)    NOT NULL,
//is_primary BOOLEAN DEFAULT FALSE,
//created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
//updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
//);
//ALTER TABLE `address` ADD CONSTRAINT `FK_user_TO_address_1` FOREIGN KEY (`user_seq`) REFERENCES `user` (`user_seq` );
//ALTER TABLE `address` ADD CONSTRAINT `unique_primary_address` UNIQUE (user_seq, is_primary);


@Entity
@Table(name = "address")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "address_seq")
  private int addressSeq;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_seq", nullable = false, foreignKey = @ForeignKey(name = "FK_user_TO_address_1"))
  private User user;
  
  @Column(name = "name", length = 20)
  private String name;

  @Column(name = "postcode", length = 10, nullable = false)
  private String postcode;

  @Column(name = "main_address", length = 250, nullable = false)
  private String mainAddress;

  @Column(name = "detail_address", length = 250, nullable = false)
  private String detailAddress;

  @Column(name = "is_primary")
  @ColumnDefault("false") 
  private boolean isPrimary;
  
  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "DATETIME NULL ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
  
  public void update(AddressRequestDto addressRequestDto) {
  this.name=addressRequestDto.name();
  this.postcode=addressRequestDto.postcode();
  this.mainAddress=addressRequestDto.mainAddress();
  this.detailAddress=addressRequestDto.detailAddress();
  }
}
