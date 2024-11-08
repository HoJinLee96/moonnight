package dtoKakaoLogin;

public class KakaoUserInfoResponseDto {
    private long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    public KakaoUserInfoResponseDto() {}
    
    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getConnected_at() {
      return connected_at;
    }

    public void setConnected_at(String connected_at) {
      this.connected_at = connected_at;
    }

    public Properties getProperties() {
      return properties;
    }

    public void setProperties(Properties properties) {
      this.properties = properties;
    }

    public KakaoAccount getKakao_account() {
      return kakao_account;
    }

    public void setKakao_account(KakaoAccount kakao_account) {
      this.kakao_account = kakao_account;
    }



    public static class Properties {
        private String nickname;

        public Properties() {}

        public String getNickname() {
          return nickname;
        }

        public void setNickname(String nickname) {
          this.nickname = nickname;
        }

        @Override
        public String toString() {
          return "Properties [nickname=" + nickname + "]";
        }
    }

    public static class KakaoAccount {
        private boolean profile_nickname_needs_agreement;
        private Profile profile;
        private boolean has_email;
        private boolean email_needs_agreement;
        private boolean is_email_valid;
        private boolean is_email_verified;
        private String email;

        public KakaoAccount() {}
        
        public boolean isProfile_nickname_needs_agreement() {
          return profile_nickname_needs_agreement;
        }

        public void setProfile_nickname_needs_agreement(boolean profile_nickname_needs_agreement) {
          this.profile_nickname_needs_agreement = profile_nickname_needs_agreement;
        }

        public Profile getProfile() {
          return profile;
        }

        public void setProfile(Profile profile) {
          this.profile = profile;
        }

        public boolean isHas_email() {
          return has_email;
        }

        public void setHas_email(boolean has_email) {
          this.has_email = has_email;
        }

        public boolean isEmail_needs_agreement() {
          return email_needs_agreement;
        }

        public void setEmail_needs_agreement(boolean email_needs_agreement) {
          this.email_needs_agreement = email_needs_agreement;
        }

        public boolean isIs_email_valid() {
          return is_email_valid;
        }

        public void setIs_email_valid(boolean is_email_valid) {
          this.is_email_valid = is_email_valid;
        }

        public boolean isIs_email_verified() {
          return is_email_verified;
        }

        public void setIs_email_verified(boolean is_email_verified) {
          this.is_email_verified = is_email_verified;
        }

        public String getEmail() {
          return email;
        }

        public void setEmail(String email) {
          this.email = email;
        }

        public static class Profile {
            private String nickname;
            private boolean is_default_nickname;
            public Profile() {}
            public String getNickname() {
              return nickname;
            }
            public void setNickname(String nickname) {
              this.nickname = nickname;
            }
            public boolean isIs_default_nickname() {
              return is_default_nickname;
            }
            public void setIs_default_nickname(boolean is_default_nickname) {
              this.is_default_nickname = is_default_nickname;
            }
            @Override
            public String toString() {
              return "Profile [nickname=" + nickname + ", is_default_nickname="
                  + is_default_nickname + "]";
            }
        }

        @Override
        public String toString() {
          return "KakaoAccount [profile_nickname_needs_agreement="
              + profile_nickname_needs_agreement + ", profile=" + profile + ", has_email="
              + has_email + ", email_needs_agreement=" + email_needs_agreement + ", is_email_valid="
              + is_email_valid + ", is_email_verified=" + is_email_verified + ", email=" + email
              + "]";
        }
        
    }

    @Override
    public String toString() {
      return "KakaoUserInfoResponseDto [id=" + id + ", connected_at=" + connected_at
          + ", properties=" + properties + ", kakao_account=" + kakao_account + "]";
    }

    
}
