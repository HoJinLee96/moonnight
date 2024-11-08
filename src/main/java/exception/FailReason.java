package exception;

public enum FailReason {
  
  //비밀번호 오류
  INVALID_PASSWORD,
  
  //이메일 없음
  USER_NOT_FOUND,
  
  //다양한 이유로 잠긴 계정, 비밀번호 다회 오류 등
  ACCOUNT_LOCKED,
  
  //비활성화 계정
  ACCOUNT_INACTIVE,
  
  //잘못된 OAUTH 토큰
  INVALID_OAUTH_TOKEN,
  
  //인증 실패
  AUTHENTICATION_FAILED,
  
  //IP 차단
  IP_BLOCKED,
  
  //서버 에러
  SERVER_ERROR;

}
