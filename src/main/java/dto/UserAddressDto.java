package dto;

import java.time.LocalDateTime;

public class UserAddressDto {
  private int addressSeq;
  private int userSeq;
  private String userPostcode;
  private String userMainAddress;
  private String userDetailAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  public UserAddressDto(int userSeq, String userPostcode, String userMainAddress,
      String userDetailAddress) {
    this.userSeq = userSeq;
    this.userPostcode = userPostcode;
    this.userMainAddress = userMainAddress;
    this.userDetailAddress = userDetailAddress;
  }
  
  public UserAddressDto(int addressSeq, int userSeq, String userPostcode, String userMainAddress,
      String userDetailAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.addressSeq = addressSeq;
    this.userSeq = userSeq;
    this.userPostcode = userPostcode;
    this.userMainAddress = userMainAddress;
    this.userDetailAddress = userDetailAddress;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }


  public int getAddressSeq() {
    return addressSeq;
  }

  public int getUserSeq() {
    return userSeq;
  }

  public String getUserPostcode() {
    return userPostcode;
  }

  public String getUserMainAddress() {
    return userMainAddress;
  }

  public String getUserDetailAddress() {
    return userDetailAddress;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
  
  
  
  
}
