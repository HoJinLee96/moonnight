package auth.sign.token.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import auth.crypto.JwtTokenProvider;
import auth.redis.TokenStore;
import auth.sign.SignService;
import auth.sign.log.LoginLog.LoginResult;
import auth.sign.log.LoginLogService;
import domain.user.User.UserProvider;
import global.exception.ExpiredException;
import global.exception.IllegalJwtException;
import infra.naver.sms.GuidanceService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJwtFilter <T extends UserDetails> extends OncePerRequestFilter{
  protected abstract T buildUserDetails(Map<String, Object> claims);
  
  @Autowired
  protected JwtTokenProvider jwtTokenProvider;
  @Autowired
  protected TokenStore tokenStore;
  @Autowired
  protected LoginLogService loginLogService;
  @Autowired
  protected GuidanceService guidanceService;
  @Autowired
  protected SignService signService; 
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    String clientIp = (String) request.getAttribute("clientIp");
    String bearerToken = request.getHeader("Authorization");
    String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    
    if (accessToken == null) {
      setErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 요청 입니다.");
      return;
    }
    
    if (tokenStore.isBlackList(accessToken)) {
      log.warn("[블랙리스트 토큰 접근] IP: {}, token: {}", clientIp, accessToken);
      
      // 로그인 로그 남기기
      loginLogService.registerLoginLog(
          UserProvider.LOCAL, null, request.getRemoteAddr(), LoginResult.BLACKLISTED_TOKEN);
      
      // 관리자 알림 전송
      try {
          guidanceService.sendSecurityAlert("블랙리스트 토큰 접근 시도\nIP: " + clientIp + "\naccessToken: " + accessToken);
      } catch (Exception e) {
          log.warn("보안 알림 전송 실패: {}", e.getMessage());
      }
      
      setErrorResponse(response, "TOKEN_BLACKLIST", "유효하지 않은 요청 입니다.");
      return;
    }
    
    // 복호화 대문에 Claims가 아니라 Map<String,Object>임
    Map<String,Object> claims = null;
    try {
      claims = jwtTokenProvider.validateAccessToken(accessToken);
      
      T customUserDetails = buildUserDetails(claims);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
      
    } catch (ExpiredException e) {
        log.info("JWT Access Token 기한 만료. Attempting refresh. IP: {}", clientIp);
        try {
            // 1. Refresh Token 가져오기 (모바일용 방법 고려)
            String refreshToken = getRefreshTokenFromCookie(request);
            
            if (refreshToken == null) {
               log.info("Refresh Token을 찾을 수 없음. IP: {}", clientIp);
               throw new IllegalJwtException("비정상적인 접근입니다.");
            }
            
            // 2. SignService.refresh 호출
            Map<String, String> newTokens = signService.refresh(accessToken, refreshToken, clientIp);
            String newAccessToken = newTokens.get("accessToken");
            String newRefreshToken = newTokens.get("refreshToken"); // 갱신된 리프레시 토큰
            
            // 3. 새 토큰으로 응답 헤더/쿠키 설정
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true) // request.isSecure()로 HTTPS 여부 판단
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict") // 또는 Strict
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
             
            // 4. 새로 발급된 AccessToken으로 다시 인증 처리
            Map<String, Object> newClaims = jwtTokenProvider.validateAccessToken(newAccessToken);
            T newUserDetails = buildUserDetails(newClaims);
            UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(newUserDetails, null, newUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

            log.info("Refresh Token 통해 Refresh 성공. IP: {}", clientIp);
            filterChain.doFilter(request, response);
            
        } catch (IllegalJwtException | JwtException | IllegalArgumentException refreshEx) {
            // Refresh 실패 시 (Refresh Token 만료, 변조 등)
            log.info("Failed to refresh token. IP: {}, Reason: {}", clientIp, refreshEx.getMessage());
            setErrorResponse(response, "TOKEN_REFRESH_FAILED", "로그인이 만료되었습니다. 다시 로그인해주세요.");
            return;
        }
    } catch (JwtException | IllegalArgumentException e) {
      log.info("JWT Access Token 찾을 수 없음. IP: {}, Reason: {}", clientIp, e.getMessage());
      setErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 요청 입니다.");
      return;
    }
  }
  
  private String getRefreshTokenFromCookie(HttpServletRequest request) {
    Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");
    return (refreshTokenCookie != null) ? refreshTokenCookie.getValue() : null;
  }
  
  private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = Map.of(
        "code", code,
        "message", message,
        "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );
    response.getWriter().write(new ObjectMapper().writeValueAsString(body));
  }
  
}
