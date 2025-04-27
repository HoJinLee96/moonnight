package auth.sign;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import auth.crypto.JwtTokenProvider;
import auth.redis.TokenStore;
import auth.redis.TokenStore.TokenType;
import auth.sign.log.LoginLog.LoginResult;
import auth.sign.log.LoginLogService;
import auth.verification.Verification;
import auth.verification.VerificationService;
import domain.address.Address;
import domain.address.AddressRepository;
import domain.user.User;
import domain.user.User.UserProvider;
import domain.user.User.UserStatus;
import domain.user.UserCreateRequestDto;
import domain.user.UserRepository;
import domain.user.UserResponseDto;
import domain.user.UserService;
import global.exception.IllegalJwtException;
import global.exception.IllegalUuidException;
import global.exception.NoSuchDataException;
import global.exception.StatusDeleteException;
import global.exception.StatusStayException;
import global.exception.StatusStopException;
import global.exception.TooManyLoginFailuresException;
import infra.naver.sms.GuidanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignService {

  private final UserService userService;
  private final AddressRepository addressRepository;
  private final VerificationService verificationService;
  private final UserRepository userRepository;
  private final LoginLogService loginLogService ;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenStore tokenStore;
  private final GuidanceService guidanceService;
  private static final Logger logger = LoggerFactory.getLogger(SignService.class);
  private final Map<UserProvider,String> roles = Map.of(UserProvider.LOCAL,"ROLE_LOCAL",UserProvider.KAKAO,"ROLE_OAUTH",UserProvider.NAVER,"ROLE_OAUTH");
  
  @Transactional
  public String createJoinToken(String email, String password, String valificationEmailToken) {

    String emailRedis = tokenStore.getVerificationEmail(valificationEmailToken);
    
    try {
      validateByReidsValue(email, emailRedis); //throw new IllegalArgumentException
      
      userService.isEmailExists(UserProvider.LOCAL, email); //throw new DuplicationException("해당 이메일의 계정이 이미 존재합니다.");
      
      return tokenStore.createMapToken(TokenType.ACCESS_SIGNUP, Map.of("email",email,"password",password));
    } finally {
      tokenStore.removeToken(TokenType.VERIFICATION_EMAIL, valificationEmailToken);
    }
  }
  
  @Transactional
  public String signUpLocalUser(UserCreateRequestDto userCreateRequestDto, String accessJoinToken, String verificationPhoneToken) {
    
    Map<String,String> mapValue = tokenStore.getMapTokenData(TokenType.ACCESS_SIGNUP, accessJoinToken);
    String phoneRedis = tokenStore.getVerificationPhone(verificationPhoneToken);
    
    try {
      validateByReidsValue(userCreateRequestDto.phone(), phoneRedis);  //throw new IllegalArgumentException
      
      userService.isPhoneExists(UserProvider.LOCAL, phoneRedis); //throw new DuplicationException("해당 휴대폰의 계정이 이미 존재합니다.");
      
      // 비밀번호 인코딩 후 저장
      String encodePassoword = passwordEncoder.encode(mapValue.get("password"));
      
      User user = userCreateRequestDto.toEntity();
      user.setEmail(mapValue.get("email"));
      user.setPassword(encodePassoword);
      userRepository.save(user);
      
      Address address = userCreateRequestDto.toAddressEntity();
      address.setUser(user);
      addressRepository.save(address);
      
      return userCreateRequestDto.name();
    } finally {
      tokenStore.removeToken(TokenType.ACCESS_SIGNUP, accessJoinToken);
      tokenStore.removeToken(TokenType.VERIFICATION_PHONE, verificationPhoneToken);
    }
  }
  
  @Transactional
  public Map<String,String> signInLocal(SignInRequestDto loginRequestDto, String ip) {
    
    String email = loginRequestDto.email();
    String password = loginRequestDto.password();
    System.out.println("email: "+email+ ", password: "+password);
    User user = findValidUser(email, ip);
    System.out.println("user: "+user);
    validatePassword(user, password, ip);
    return handleJwt(user);
  }
  
  @Transactional
  public String signInAuthSms(String phone, String token, String ip) {
    
    String value = tokenStore.getVerificationPhone(token);
    if(!Objects.equals(value, phone)) {
      throw new IllegalUuidException("잘못된 요청입니다.\n다시 시도해 주세요.");
    }
    
    // ======= 수신자에 일치하는 DB 찾기 및 해당 데이터가 3분 이내인지 검사 =======
    Verification ver = verificationService.findVerification(phone, ip);
    
    // AccessToken만 발급 30분 
    return jwtTokenProvider.createVerifyPhoneToken(ver.getVerificationSeq(),Map.of("phone",phone));
  }
  
  @Transactional
  public String validPassword(String email, String password, String ip) {
    
    User user = findValidUser(email, ip);
    validatePassword(user, password, ip);
    return tokenStore.createAccessPaaswordToken(email);
  }

  @Transactional
  public UserResponseDto getUserByAccessToken(String accessToken, String clientIp){
    
    // ======= 엑세스 토큰 검증 =======
    Map<String,Object> claims = jwtTokenProvider.validateAccessToken(accessToken);
    System.out.println("claims: "+claims);
    Object subjectRaw = claims.get("subject");
    System.out.println("subjectRaw: "+ subjectRaw);
    if (subjectRaw == null) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - subject");
    }
    int userId = Integer.parseInt(subjectRaw.toString());
    if(userId==0) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - subject");
    }
    
    User user = userService.getUserByUserId(userId);
    
    return UserResponseDto.fromEntity(user);
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
  public void signout(String accessToken ,String refreshToken, String clientIp) {
    // 1. accessToken 블랙리스트 등록
      try {
          long ttl = jwtTokenProvider.getLoginJwtRemainingTime(accessToken);
          tokenStore.addJwtBlacklist(accessToken, ttl, "logout");
          logger.info("로그아웃 - accessToken 블랙리스트 등록. accessToken: {}, ip: {}", accessToken, clientIp);
      } catch (Exception e) {
          logger.warn("로그아웃 - accessToken 블랙리스트 등록 실패. accessToken: {}, ip: {}, {}", accessToken, clientIp, e.getCause());
          throw e;
      }
      
      // 2. refreshToken 삭제
      String userId = jwtTokenProvider.validateRefreshToken(refreshToken);
      if(tokenStore.removeToken(TokenType.JWT_REFRESH, userId)) {
        logger.info("로그아웃 - refreshToken 삭제. refreshToken: {}, ip: {}", refreshToken, clientIp);
      }else {
        logger.warn("로그아웃 - refreshToken 삭제 실패. 도용된 RefreshToken. refreshToken: {}, ip: {}", refreshToken, clientIp);
        throw new IllegalJwtException("로그아웃 - refreshToken 삭제 실패. 도용된 RefreshToken.");
      }
  }
  
  // ======= email 통해 Local.User 찾고 Status익셉션별 로그 기록 및 반환 =======
  private User findValidUser(String email, String ip) {
    try {
      return userService.getUserByUserProviderAndEmail(UserProvider.LOCAL, email);
    } catch (NoSuchDataException e) {
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
  
  // ======= 비밀번호 검증 및 결과 `LoginLog` 기록 =======
  private void validatePassword(User user, String reqPassword, String ip) {
    System.out.println("passwordEncoder.matches(reqPassword, user.getPassword(): "+ passwordEncoder.matches(reqPassword, user.getPassword()));
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
          throw new BadCredentialsException("아이디와 비밀번호를 확인해 주세요.");
      }
    loginLogService.registerLoginLog(UserProvider.LOCAL, user.getEmail(), ip, LoginResult.SUCCESS);
  }
  
  // ======= User 객체 통해 토큰 발급 및 RefreshToken은 redis 저장 =======
  private Map<String, String> handleJwt(User user) {
    System.out.println("handleJwt 시작 user.getUserSeq: "+user.getUserSeq());
      Map<String, String> loginToken = jwtTokenProvider.createLoginToken(
          user.getUserSeq(), List.of(roles.get(user.getUserProvider())),
          Map.of("provider", user.getUserProvider().toString(), "email", user.getEmail(), "name", user.getName()));
  
      tokenStore.addRefreshJwt(user.getUserSeq(), loginToken.get("refreshToken"));
      System.out.println("loginToken: "+loginToken);
      return loginToken;
  }
  
  // ======= RefreshToken 검증 =======
  private int validateRefreshToken(String accessToken, String refreshToken, String clientIp) {
    
    String userId = jwtTokenProvider.validateRefreshToken(refreshToken);
    String value = tokenStore.getRefreshJwt(userId);
    
    // 토큰 탈취 검증
    if(!Objects.equals(value, refreshToken)) {
      handleRefreshTokenHijack(accessToken, userId, clientIp);
      throw new IllegalJwtException("잘못된 요청입니다.");
    }
    
    return Integer.valueOf(userId);
  }
  
  // ======= AccessToekn 블랙리스트 등록, RefreshToken 삭제 , 운영자에게 안내 =======
  private void handleRefreshTokenHijack(String accessToken, String userId, String clientIp){
    
    logger.warn("[탈취 의심] 잘못된 refresh 요청 - IP: {}, userId: {}, accessToken: {}", clientIp, userId, accessToken);

    long ttl = jwtTokenProvider.getLoginJwtRemainingTime(accessToken); // 남은 시간 (ms)
    tokenStore.addJwtBlacklist(accessToken, ttl, "hijack");

    try {
      guidanceService.sendSecurityAlert("[탈취 의심] 잘못된 refresh 요청\n" +
          "userId: " + userId + "\nIP: " + clientIp + "\nAccessToken: " + accessToken);
    } catch (Exception e) {
      logger.warn("탈취 의심 문자 알림 실패: {}", e.getMessage());
    }

    tokenStore.removeToken(TokenType.JWT_REFRESH, userId);
  }
  
  private void validateByReidsValue(String a, String b) {
    if(!Objects.equals(a,b)) {
      logger.info("입력값과 Redis 값이 다름");
      throw new IllegalArgumentException("잘못된 요청입니다.");
    }
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
