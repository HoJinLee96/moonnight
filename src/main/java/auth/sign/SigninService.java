package auth.sign;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import auth.crypto.JwtTokenProvider;
import auth.redis.TokenStore;
import auth.redis.TokenStore.TokenType;
import auth.sign.log.LoginLogService;
import auth.sign.log.LoginLog.LoginResult;
import domain.user.User;
import domain.user.User.UserProvider;
import domain.user.User.UserStatus;
import domain.user.UserRepository;
import domain.user.UserService;
import global.exception.IllegalJwtException;
import global.exception.StatusDeleteException;
import global.exception.StatusStayException;
import global.exception.StatusStopException;
import global.exception.TooManyLoginFailuresException;
import infra.naver.sms.GuidanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SigninService {

  private final UserService userService;
  private final UserRepository userRepository;
  private final LoginLogService loginLogService ;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenStore tokenStore;
  private final GuidanceService guidanceService;
  private static final Logger logger = LoggerFactory.getLogger(SigninService.class);
  private final Map<UserProvider,String> roles = Map.of(UserProvider.LOCAL,"ROLE_USER",UserProvider.KAKAO,"ROLE_OAUTH",UserProvider.NAVER,"ROLE_OAUTH");
  
  @Transactional
  public Map<String,String> loginLocal(SigninRequestDto loginRequestDto, String ip) {
    
    String email = loginRequestDto.email();
    String password = loginRequestDto.password();

    User user = findValidUser(email, ip);
    validatePassword(user, password, ip);
    return handleJwt(user);
  }
  
  @Transactional
  public Map<String,String> refresh(String accessToken, String refreshToken, String clientIp){
    
    // ======= 리프레쉬 토큰 검증 =======
    int userId = validateRefreshToken(accessToken, refreshToken, clientIp);
    
    User user = userService.getUserByUserId(userId);
    
    loginLogService.registerLoginLog(user.getUserProvider(),user.getEmail(),clientIp,LoginResult.SUCCESS);
    
    return handleJwt(user);
  }
  
  @Transactional
  public void logout(String accessToken, String userId, String clientIp) {
      // 1. accessToken 블랙리스트 등록
      try {
          long ttl = jwtTokenProvider.getLoginJwtRemainingTime(accessToken);
          tokenStore.addJwtBlacklist(accessToken, ttl, "logout");
          logger.info("로그아웃 accessToken 블랙리스트 등록 - userId: {}, ip: {}", userId, clientIp);
      } catch (Exception e) {
          logger.warn("로그아웃 accessToken 만료 또는 파싱 실패 - 블랙리스트 등록 실패: {}", e.getMessage());
      }

      // 2. refreshToken 삭제
      tokenStore.removeToken(TokenType.JWT_REFRESH, userId);
      logger.info("refreshToken 삭제 완료 - userId: {}, ip: {}", userId, clientIp);

  }
  
  
  
  private User findValidUser(String email, String ip) {
    try {
      return userService.getUserByUserProviderAndEmail(UserProvider.LOCAL, email);
    } catch (NoSuchElementException e) {
        loginLogService.registerLoginLog(UserProvider.LOCAL, email, ip, LoginResult.INVALID_EMAIL);
        throw new NoSuchElementException("아이디와 비밀번호를 확인해 주세요.");
    } catch (StatusStayException e) {
        loginLogService.registerLoginLog(UserProvider.LOCAL, email, ip, LoginResult.ACCOUNT_LOCKED);
        throw e;
    } catch (StatusStopException e) {
        loginLogService.registerLoginLog(UserProvider.LOCAL, email, ip, LoginResult.ACCOUNT_SUSPENDED);
        throw e;
    } catch (StatusDeleteException e) {
        loginLogService.registerLoginLog(UserProvider.LOCAL, email, ip, LoginResult.ACCOUNT_DELETED);
        throw e;
    }
  }
  
  private void validatePassword(User user, String reqPassword, String ip) {
      if (!passwordEncoder.matches(reqPassword, user.getPassword())) {
          loginLogService.registerLoginLog(UserProvider.LOCAL, user.getEmail(), ip, LoginResult.INVALID_PASSWORD);
          try {
              loginLogService.countloginFail(UserProvider.LOCAL, user.getEmail());
          } catch (TooManyLoginFailuresException e) {
              user.setUserStatus(UserStatus.STAY);
              userRepository.flush();
              logger.warn("로그인 실패 초과로 계정 정지: email={}, ip={}", user.getEmail(), ip);
              throw e;
          }
          throw new NoSuchElementException("아이디와 비밀번호를 확인해 주세요.");
      }
    loginLogService.registerLoginLog(UserProvider.LOCAL, user.getEmail(), ip, LoginResult.SUCCESS);
  }
  
  private Map<String, String> handleJwt(User user) {
  
      Map<String, String> loginToken = jwtTokenProvider.createLoginToken(
          user.getUserSeq(), List.of(roles.get(user.getUserProvider())),
          Map.of("provider", user.getUserProvider(), "email", user.getEmail(), "name", user.getName()));
  
      tokenStore.addRefreshJwt(user.getUserSeq(), loginToken.get("refreshToken"));
      return loginToken;
  }
  
  private int validateRefreshToken(String accessToken, String refreshToken, String clientIp) {
    String userId = jwtTokenProvider.validateRefreshToken(refreshToken);
    String value = tokenStore.getRefreshJwt(userId);
    if(!Objects.equals(value, refreshToken)) {
      handleRefreshTokenHijack(accessToken, userId, clientIp);
      throw new IllegalJwtException("잘못된 요청입니다.");
    }
    return Integer.valueOf(userId);
  }
  
  private void handleRefreshTokenHijack(String accessToken, String userId, String clientIp){
    
    long ttl = jwtTokenProvider.getLoginJwtRemainingTime(accessToken); // 남은 시간 (ms)
    tokenStore.addJwtBlacklist(accessToken, ttl, "hijack");

    logger.warn("[탈취 의심] 잘못된 refresh 요청 - IP: {}, userId: {}, accessToken: {}", clientIp, userId, accessToken);

    try {
      guidanceService.sendSecurityAlert("[탈취 의심] 잘못된 refresh 요청\n" +
          "userId: " + userId + "\nIP: " + clientIp + "\nAccessToken: " + accessToken);
    } catch (Exception e) {
      logger.warn("탈취 의심 문자 알림 실패: {}", e.getMessage());
    }

    tokenStore.removeToken(TokenType.JWT_REFRESH, userId);
  }
  
}
  
  
  // private final UserDao userDao;
  // private final LoginLogDao loginLogDao;
  // private final PasswordEncoder passwordEncoder;
  //
  // @Autowired
  // public LoginService(UserDao userDao, LoginLogDao loginLogDao, PasswordEncoder passwordEncoder)
  // {
  // this.userDao = userDao;
  // this.loginLogDao = loginLogDao;
  // this.passwordEncoder = passwordEncoder;
  // }
  //
  // @Transactional
  // public LoginResult login(String email, String reqPassword, String ip) throws SQLException {
  //
  // LoginResult loginResult = new LoginResult(Provider.NORMAL, email, reqPassword, ip,
  // LocalDateTime.now());
  //
  // // 유저 정보 조회
  // Optional<UserRequestDto> OptionalUserDto = userDao.getLoginInfoByEmail(email);
  //
  // // 일치하는 이메일 유저 없음
  // if (OptionalUserDto.isEmpty()) {
  // loginResult.setReason(Reason.INVALID_EMAIL);
  // return loginResult;
  // }
  //
  // UserRequestDto userDto = OptionalUserDto.get();
  //
  // // 비밀번호 확인
  // if (!passwordEncoder.matches(reqPassword, userDto.getPassword())) {
  // // 로그인 시도 횟수에 따라 계정 상태 변경
  // int loginFailCount = loginLogDao.countLoginFailByEmail(email);
  // if (loginFailCount >= 9) {
  // userDao.updateStatusByEmail(email, Status.STAY);
  // }
  // loginResult.setReason(Reason.INVALID_PASSWORD);
  // return loginResult;
  // }
  //
  // loginResult.setReason(Reason.SUCCESS);
  // loginResult.setStatus(userDto.getStatus());
  // return loginResult;
  // }
