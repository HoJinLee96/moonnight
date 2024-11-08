package dto;

import java.time.LocalDateTime;

public class VerifyResponseDto {
    

    private int verificationSeq;
    private String to; //수신자
    private String verificationCode; //인증 코드
    private String statusCode; //결과 ex) 네이버 문자경우 202: 성공 그 외 다 실패 
    private LocalDateTime requestTime; //외부 서버에 요청한 시간 

    private String requestId; //외부 서버에 내 서버임을 알리는 id
    private String statusName; //결과
    

    public VerifyResponseDto() {
  }
    
    public static class Builder {
      private int verificationSeq;
      private String requestId; //외부 서버에 내 서버임을 알리는 id
      private LocalDateTime requestTime; //외부 서버에 요청한 시간 
      private String statusCode; //결과
      private String statusName; //결과
      
      private String verificationCode; //인증 코드
      private String to; //수신자

      public Builder verificationSeq(int verificationSeq) {
          this.verificationSeq = verificationSeq;
          return this;
      }

      public Builder requestId(String requestId) {
          this.requestId = requestId;
          return this;
      }

      public Builder requestTime(LocalDateTime requestTime) {
          this.requestTime = requestTime;
          return this;
      }

      public Builder statusCode(String statusCode) {
          this.statusCode = statusCode;
          return this;
      }

      public Builder statusName(String statusName) {
          this.statusName = statusName;
          return this;
      }

      public Builder verificationCode(String verificationCode) {
          this.verificationCode = verificationCode;
          return this;
      }

      public Builder to(String to) {
          this.to = to;
          return this;
      }

      public VerifyResponseDto build() {
          return new VerifyResponseDto(this);
      }
  }
    private VerifyResponseDto(Builder builder) {
      this.verificationSeq = builder.verificationSeq;
      this.requestId = builder.requestId;
      this.requestTime = builder.requestTime;
      this.statusCode = builder.statusCode;
      this.statusName = builder.statusName;
      this.verificationCode = builder.verificationCode;
      this.to = builder.to;
  }
    public int getVerificationSeq() {
      return verificationSeq;
    }

    public String getRequestId() {
      return requestId;
    }

    public LocalDateTime getRequestTime() {
      return requestTime;
    }

    public String getStatusCode() {
      return statusCode;
    }

    public String getStatusName() {
      return statusName;
    }

    public String getVerificationCode() {
      return verificationCode;
    }

    public String getTo() {
      return to;
    }
    @Override
    public String toString() {
      return "VerifyResponseDto [verificationSeq=" + verificationSeq + ", to=" + to
          + ", verificationCode=" + verificationCode + ", statusCode=" + statusCode
          + ", requestTime=" + requestTime + ", requestId=" + requestId + ", statusName="
          + statusName + "]";
    }

    
}