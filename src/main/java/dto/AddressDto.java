package dto;

import java.time.LocalDateTime;

public class AddressDto {
  private int addressSeq;
  private int userSeq;
  private String name;
  private String postcode;
  private String mainAddress;
  private String detailAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  public AddressDto() {}
  
  public AddressDto(String postcode, String mainAddress, String detailAddress) {
    super();
    this.postcode = postcode;
    this.mainAddress = mainAddress;
    this.detailAddress = detailAddress;
  }



  public AddressDto(int userSeq, String name, String postcode, String mainAddress, String detailAddress) {
    this.userSeq = userSeq;
    this.name = name;
    this.postcode = postcode;
    this.mainAddress = mainAddress;
    this.detailAddress = detailAddress;
  }
  
  public AddressDto(int addressSeq, int userSeq, String name, String postcode, String mainAddress,
      String detailAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {
    super();
    this.addressSeq = addressSeq;
    this.userSeq = userSeq;
    this.name = name;
    this.postcode = postcode;
    this.mainAddress = mainAddress;
    this.detailAddress = detailAddress;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public int getAddressSeq() {
    return addressSeq;
  }

  public int getUserSeq() {
    return userSeq;
  }

  public String getPostcode() {
    return postcode;
  }

  public String getMainAddress() {
    return mainAddress;
  }

  public String getDetailAddress() {
    return detailAddress;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getName() {
    return name;
  }
  
  public void setAddressSeq(int addressSeq) {
    this.addressSeq = addressSeq;
  }

  public void setUserSeq(int userSeq) {
    this.userSeq = userSeq;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public void setMainAddress(String mainAddress) {
    this.mainAddress = mainAddress;
  }

  public void setDetailAddress(String detailAddress) {
    this.detailAddress = detailAddress;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
  
  @Override
  public String toString() {
    return "AddressDto [addressSeq=" + addressSeq + ", userSeq=" + userSeq + ", postcode="
        + postcode + ", mainAddress=" + mainAddress + ", detailAddress=" + detailAddress
        + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
  }
  
  
  
  
}
