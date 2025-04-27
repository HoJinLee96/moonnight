package global.util;

public class RequestUtils {

  public static String extractToken(boolean isMobileApp, String mobileToken, String webToken) {
    if(isMobileApp) {
      if(mobileToken==null || mobileToken.isEmpty()) {
        throw new IllegalArgumentException("잘못된 요청입니다.");
      }
      return mobileToken;
    }else {
      if(webToken==null || webToken.isEmpty()) {
        throw new IllegalArgumentException("잘못된 요청입니다.");
      }
      return webToken;
    }
  }
  
}
