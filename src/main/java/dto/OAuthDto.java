package dto;

import java.time.LocalDateTime;
import dtoKakaoLogin.KakaoUserInfoResponseDto;
import dtoNaverLogin.NaverUserInfoResponseDto;

public class OAuthDto extends User {
  private int oauthSeq;
  private int userSeq;
  private String provider; // "NAVER" 또는 "KAKAO"
  private String id;
  private String email;
  private String name;
  private String birth;
  private String phone;
  private Status status;
  private LocalDateTime createdAt;
  
  public OAuthDto() {
  }

  public OAuthDto(NaverUserInfoResponseDto naverUser) {
    this.id = naverUser.getResponse().getId();
    this.email = naverUser.getResponse().getEmail();
    this.name = naverUser.getResponse().getName();
    this.birth = naverUser.getResponse().getBirthyear()+naverUser.getResponse().getBirthday();
    this.phone = naverUser.getResponse().getMobile();
    this.provider = "NAVER";
  }
  
  public OAuthDto(KakaoUserInfoResponseDto kakaoUser) {
    this.id = kakaoUser.getId()+"";
    this.email = kakaoUser.getKakao_account().getEmail();
    this.name = kakaoUser.getKakao_account().getProfile().getNickname();
    this.provider = "KAKAO";
  }

  
  public int getOauthSeq() {
    return oauthSeq;
  }
  public int getUserSeq() {
    return userSeq;
  }
  @Override
  public String getProvider() {
    return provider;
  }
  public String getId() {
    return id;
  }
  public String getEmail() {
    return email;
  }
  public String getName() {
    return name;
  }
  public String getBirth() {
    return birth;
  }
  public String getPhone() {
    return phone;
  }
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setOauthSeq(int oauthSeq) {
    this.oauthSeq = oauthSeq;
  }

  public void setUserSeq(int userSeq) {
    this.userSeq = userSeq;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setBirth(String birth) {
    this.birth = birth;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  
  
  
  
  
}
