package deprecated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.OAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Deprecated

//@Component
public class OAuthRefreshInterceptor implements HandlerInterceptor {

  KakaoOAuthLoginService kakaoOAuthLoginService;
  NaverOAuthLoginService naverOAuthLoginService;

  @Autowired
  public OAuthRefreshInterceptor(KakaoOAuthLoginService kakaoOAuthLoginService,
      NaverOAuthLoginService naverOAuthLoginService) {
    super();
    this.kakaoOAuthLoginService = kakaoOAuthLoginService;
    this.naverOAuthLoginService = naverOAuthLoginService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    System.out.println("OAuthRefreshInterceptor.preHandle() 실행");

    
    HttpSession session = request.getSession();

    if (session == null) return true;

    UserRequestDto user = (UserRequestDto) session.getAttribute("userDto");
    Long userDtoExpiry = (Long) session.getAttribute("userDtoExpiry");

    if (user != null && userDtoExpiry!=null) {
      long time = userDtoExpiry - System.currentTimeMillis();

      if(time<0) {
        session.removeAttribute("userDto");
        session.removeAttribute("userDtoExpiry");
      }
      else if(0 < time && time <= 5 * 60 *1000) {
        session.setAttribute("userDtoExpiry", System.currentTimeMillis()* 30 * 60 * 1000);
      }
      
    }

    OAuth oAuthDto = (OAuth) session.getAttribute("oAuthDto");
    OAuthTokenDto oAuthToken = (OAuthTokenDto) session.getAttribute("oAuthToken");
    Long oAuthTokenExpiry = (Long) session.getAttribute("oAuthTokenExpiry");

    if (oAuthDto != null && oAuthToken != null && oAuthTokenExpiry != null) {

      long time = oAuthTokenExpiry - System.currentTimeMillis();
      
      if (time<0) {
        session.removeAttribute("oAuthDto");
        session.removeAttribute("oAuthToken");
        session.removeAttribute("oAuthTokenExpiry");
      }
      else if (0 < time && time <= 5 * 60 * 1000) {

        String provider = oAuthDto.getProvider().name();
        ObjectMapper mapper = new ObjectMapper();
        if (provider.equals("NAVER")) {
          String responseToken =
              naverOAuthLoginService.updateTokenUrl("token", "refresh_token", oAuthToken);
          oAuthToken = mapper.readValue(responseToken, OAuthTokenDto.class);
          System.out.println("네이버 갱신 완료");
        } else if (provider.equals("KAKAO")) {
          String responseToken =
              kakaoOAuthLoginService.updateTokenUrl("token", "refresh_token", oAuthToken);
          JsonNode rootNode = mapper.readTree(responseToken);
          String access_token = rootNode.get("access_token").asText();
          String id_token = rootNode.get("id_token").asText();
          oAuthToken.setAccess_token(access_token);
          oAuthToken.setId_token(id_token);
          System.out.println("카카오 갱신 완료");

        }
        session.setAttribute("oAuthToken", oAuthToken);
        session.setAttribute("oAuthTokenExpiry",
            System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));

      }
    }
    return true;
  }

}
