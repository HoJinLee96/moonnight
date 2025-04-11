package auth.oauth;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import auth.crypto.JwtTokenProvider;
import auth.oauth.OAuth.OAuthProvider;
import auth.oauth.OAuth.OAuthStatus;
import auth.sign.log.LoginLog;
import auth.sign.log.LoginLogRepository;
import auth.sign.log.LoginLog.LoginResult;
import domain.user.User;
import domain.user.User.UserProvider;
import domain.user.User.UserStatus;
import domain.user.UserRepository;
import global.exception.StatusDeleteException;
import global.exception.StatusStayException;
import global.exception.StatusStopException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginLogRepository loginLogRepository;
    private final RedisTemplate<String, String> redisTemplate;


    @SuppressWarnings("incomplete-switch")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
      
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        
        String provider = extractProvider(authentication).toUpperCase(); // registrationId 추출
        String oauthId = oAuth2User.getAttribute("id").toString();
        String email = null;
        String name = null;
        if(Objects.equals(provider, "NAVER")){
          email = oAuth2User.getAttribute("email").toString();
          name = oAuth2User.getAttribute("name").toString();
        }else if(Objects.equals(provider, "KAKAO")) {
          Map<String,Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
          email = kakaoAccount.get("email").toString();
          Map<String,Object> properties = oAuth2User.getAttribute("properties");
          name = properties.get("nickname").toString();
        }

        String clientIp = getClientIp(request);

        // 기존 oauth id로 조회
        Optional<OAuth> existingOauth = oauthRepository.findByOauthProviderAndId(OAuthProvider.valueOf(provider), oauthId);

        User user;
        if (existingOauth.isPresent()) {
            user = existingOauth.get().getUser();
            switch (user.getUserStatus()) {
              case STAY -> {
                  saveLoginLog(email, UserProvider.LOCAL, clientIp, LoginResult.ACCOUNT_LOCKED);
                  throw new StatusStayException("인증이 필요한 계정입니다.");
              }
              case STOP -> {
                  saveLoginLog(email, UserProvider.LOCAL, clientIp, LoginResult.ACCOUNT_SUSPENDED);
                  throw new StatusStopException("정지된 계정입니다. 고객센터에 문의해주세요.");
              }
              case DELETE -> {
                  saveLoginLog(email, UserProvider.LOCAL, clientIp, LoginResult.ACCOUNT_DELETED);
                  throw new StatusDeleteException("탈퇴한 계정입니다.");
              }
            }
        } else {
            // user 저장
            user = User.builder()
                    .email(email)
                    .userProvider(UserProvider.valueOf(provider))
                    .userStatus(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);

            // oauth 저장
            OAuth oauth = OAuth.builder()
                    .oauthProvider(OAuthProvider.valueOf(provider))
                    .id(oauthId)
                    .user(user)
                    .oauthStatus(OAuthStatus.ACTIVE)
                    .build();
            oauthRepository.save(oauth);
        }
        
        int userId = user.getUserSeq();
        // JWT accessToken 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, List.of("ROLE_OAUTH"),
            Map.of(
                "provider", provider,
                "email", email,
                "name", name
                ));
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        
        redisTemplate.opsForValue().set("jwt:refresh:" + user.getUserSeq(), refreshToken, Duration.ofDays(14));

        saveLoginLog(email, UserProvider.valueOf(provider), clientIp, LoginResult.SUCCESS);
        
        // User-Agent 분기
        String userAgent = request.getHeader("User-Agent");
        boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");

        if (isMobileApp) {
            // 앱용 응답: JSON body로 토큰 전달
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
            )));
        } else {
            // 웹용 응답: accessToken은 헤더, refreshToken은 HttpOnly 쿠키
            response.setHeader("Authorization", "Bearer " + accessToken);

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

            response.addHeader("Set-Cookie", refreshCookie.toString());
            response.sendRedirect("/home"); // or redirectUri
        }
    }

    private String extractProvider(Authentication auth) {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;
        return token.getAuthorizedClientRegistrationId(); // "naver" or "kakao"
    }
    
    private void saveLoginLog(String email, UserProvider userProvider, String ip, LoginResult loginResult) {
      loginLogRepository.save(LoginLog.builder()
          .userProvider(userProvider)
          .email(email)
          .requestIp(ip)
          .loginResult(loginResult)
          .build());
    }
    
    private String getClientIp(HttpServletRequest request) {
      String ip = request.getHeader("X-Forwarded-For");
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
          return ip.split(",")[0]; // 여러 개일 경우 첫 번째 IP
      }

      ip = request.getHeader("Proxy-Client-IP");
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
          return ip;
      }

      ip = request.getHeader("WL-Proxy-Client-IP");
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
          return ip;
      }

      return request.getRemoteAddr(); // fallback
  }
    
}