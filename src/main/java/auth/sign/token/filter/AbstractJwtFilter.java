package auth.sign.token.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import auth.crypto.JwtTokenProvider;
import auth.redis.TokenStore;
import auth.sign.log.LoginLogService;
import auth.sign.log.LoginLog.LoginResult;
import domain.user.User.UserProvider;
import global.exception.ExpiredException;
import infra.naver.sms.GuidanceService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class AbstractJwtFilter <T extends UserDetails> extends OncePerRequestFilter{
  protected abstract T buildUserDetails(Map<String, Object> claims);
  
  @Autowired
  protected JwtTokenProvider jwtLoginTokenProvider;
  @Autowired
  protected TokenStore tokenStore;
  @Autowired
  protected LoginLogService loginLogService;
  @Autowired
  protected GuidanceService guidanceService;
  private static final Logger logger = LoggerFactory.getLogger(AbstractJwtFilter.class);

  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    String clientIp = (String) request.getAttribute("clientIp");
    String bearerToken = request.getHeader("Authorization");
    String token = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    
    if (token == null) {
      setErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 요청 입니다.");
      return;
    }
    // 복호화 대문에 Claims가 아니라 Map<String,Object>임
    Map<String,Object> claims = null;
    try {
      claims = jwtLoginTokenProvider.validateLoginToken(token);
    } catch (ExpiredException e) {
      setErrorResponse(response, "TOKEN_EXPIRED", "만료된 요청 입니다.");
      return;
    } catch (JwtException | IllegalArgumentException e) {
      setErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 요청 입니다.");
      return;
    }
    
    if (claims == null) {
      setErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 요청 입니다.");
      return;
    }
    
    if (tokenStore.isBlackList(token)) {
      logger.warn("[블랙리스트 토큰 접근] IP: {}, token: {}", clientIp, token);

      // optional: 로그인 로그 남기기
      loginLogService.registerLoginLog(
          UserProvider.LOCAL, null, request.getRemoteAddr(), LoginResult.BLACKLISTED_TOKEN);

      // optional: 관리자 알림 전송
      try {
        guidanceService.sendSecurityAlert("블랙리스트 토큰 접근 시도\nIP: " + clientIp + "\nToken: " + token);
      } catch (Exception e) {
        logger.warn("보안 알림 전송 실패: {}", e.getMessage());
      }

      setErrorResponse(response, "TOKEN_BLACKLIST", "유효하지 않은 요청 입니다.");
      return;
    }
    
    T customUserDetails = buildUserDetails(claims);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }
  
  private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
