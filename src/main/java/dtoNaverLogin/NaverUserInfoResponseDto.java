package dtoNaverLogin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfoResponseDto {

  private String resultcode;
  private String message;
  private Response response;

  // Getters and Setters
  public String getResultcode() {
      return resultcode;
  }

  public void setResultcode(String resultcode) {
      this.resultcode = resultcode;
  }

  public String getMessage() {
      return message;
  }

  public void setMessage(String message) {
      this.message = message;
  }

  public Response getResponse() {
      return response;
  }

  public void setResponse(Response response) {
      this.response = response;
  }

  // Inner class for Response
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Response {
      private String id;
      private String nickname;
      private String name;
      private String email;
      private String gender;
      private String age;
      private String birthday;
      private String profileImage;
      private String birthyear;
      private String mobile;

      // Getters and Setters
      public String getId() {
          return id;
      }

      public void setId(String id) {
          this.id = id;
      }

      public String getNickname() {
          return nickname;
      }

      public void setNickname(String nickname) {
          this.nickname = nickname;
      }

      public String getName() {
          return name;
      }

      public void setName(String name) {
          this.name = name;
      }

      public String getEmail() {
          return email;
      }

      public void setEmail(String email) {
          this.email = email;
      }

      public String getGender() {
          return gender;
      }

      public void setGender(String gender) {
          this.gender = gender;
      }

      public String getAge() {
          return age;
      }

      public void setAge(String age) {
          this.age = age;
      }

      public String getBirthday() {
          return birthday;
      }

      public void setBirthday(String birthday) {
          this.birthday = birthday;
      }

      public String getProfileImage() {
          return profileImage;
      }

      public void setProfileImage(String profileImage) {
          this.profileImage = profileImage;
      }

      public String getBirthyear() {
          return birthyear;
      }

      public void setBirthyear(String birthyear) {
          this.birthyear = birthyear;
      }

      public String getMobile() {
          return mobile;
      }

      public void setMobile(String mobile) {
          this.mobile = mobile;
      }

      @Override
      public String toString() {
        return "Response [id=" + id + ", nickname=" + nickname + ", name=" + name + ", email="
            + email + ", gender=" + gender + ", age=" + age + ", birthday=" + birthday
            + ", profileImage=" + profileImage + ", birthyear=" + birthyear + ", mobile=" + mobile
            + "]";
      }
      
  }

  @Override
  public String toString() {
    return "NaverRes [resultcode=" + resultcode + ", message=" + message + ", response=" + response
        + "]";
  }
  
}