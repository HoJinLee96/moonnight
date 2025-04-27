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
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import auth.crypto.JwtTokenProvider;
import auth.oauth.OAuth.OAuthProvider;
import auth.oauth.OAuth.OAuthStatus;
import auth.sign.log.LoginLog;
import auth.sign.log.LoginLog.LoginResult;
import auth.sign.log.LoginLogRepository;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginLogRepository loginLogRepository;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy(); // 추가

    private final RedisTemplate<String, String> redisTemplate;


    @SuppressWarnings("incomplete-switch")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
      try {
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
                    .name(name)
                    .userProvider(UserProvider.valueOf(provider))
                    .userStatus(UserStatus.ACTIVE)
                    .marketingReceivedStatus(false)
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
        String userAgent = request.getHeader("X-Client-Type");
        boolean isMobileApp = userAgent != null && userAgent.contains("mobile");

        if (isMobileApp) {
            // 앱용 응답: JSON body로 토큰 전달
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
            )));
        } else {
          // 1. 리다이렉션할 중간 페이지 URL에 Access Token을 쿼리 파라미터로 추가
          String targetUrl = UriComponentsBuilder.fromPath("/oauth-redirect") // 중간 페이지 경로
                  .queryParam("token", accessToken) // Access Token 추가
                  .build().toUriString();

          // 2. Refresh Token은 HttpOnly 쿠키로 설정 (이전과 동일)
          ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
              .httpOnly(true)
              .secure(true) // HTTPS 필수
              .path("/")
              .maxAge(Duration.ofDays(14))
              .sameSite("Lax") // 또는 "Strict"
              .build();
          response.addHeader("Set-Cookie", refreshCookie.toString());

          // 3. 중간 페이지로 리다이렉션
          redirectStrategy.sendRedirect(request, response, targetUrl);
          // === 수정 끝 ===
        }
      }catch(Exception e) {
        log.error("OAuth 로그인 처리 중 예외 발생: {}", e.getMessage(), e); // 스택 트레이스 포함 로깅

        // 이미 응답이 커밋되었는지 확인 (리다이렉션 등이 이미 호출된 경우)
        if (!response.isCommitted()) {
            // 1. Refresh Token 쿠키 삭제 시도
            try {
                ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "") // 빈 값으로 설정
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0) // 즉시 만료
                    .sameSite("Lax")
                    .build();
                response.addHeader("Set-Cookie", deleteCookie.toString());
                log.info("OAuth 처리 실패: Refresh Token 쿠키 삭제 시도 완료.");
            } catch (Exception cookieEx) {
                log.warn("OAuth 처리 실패: Refresh Token 쿠키 삭제 중 오류 발생: {}", cookieEx.getMessage());
            }

            // 2. /home으로 리다이렉션
            try {
                // 에러 메시지를 쿼리 파라미터로 전달할 수도 있습니다 (선택 사항).
                // String errorRedirectUrl = UriComponentsBuilder.fromPath("/home").queryParam("error", "oauth_failed").build().toUriString();
                 String errorRedirectUrl = "/error"; // 에러 페이지로 보내는 것이 더 일반적일 수 있습니다. 또는 /home
                redirectStrategy.sendRedirect(request, response, errorRedirectUrl);
                log.info("OAuth 처리 실패: '{}'로 리다이렉션 시도 완료.", errorRedirectUrl);
            } catch (IOException redirectEx) {
                log.error("OAuth 처리 실패: 리다이렉션 중 IOException 발생: {}", redirectEx.getMessage());
                // 리다이렉션 실패 시 기본 에러 응답 처리 (필요한 경우)
                // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "로그인 처리 중 오류가 발생했습니다.");
            }
        } else {
            log.warn("OAuth 처리 실패: 응답이 이미 커밋되어 추가 처리를 할 수 없습니다.");
        }
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