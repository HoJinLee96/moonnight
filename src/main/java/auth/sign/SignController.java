package auth.sign;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import auth.sign.token.CustomUserDetails;
import domain.user.UserCreateRequestDto;
import domain.user.UserResponseDto;
import global.exception.IllegalJwtException;
import global.util.ApiResponse;
import global.validator.annotaion.ClientSpecific;
import global.validator.annotaion.ValidEmail;
import global.validator.annotaion.ValidPassword;
import global.validator.annotaion.ValidPhone;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sign")
public class SignController {

  private final SignService signService;

  @PostMapping("/public/in/local")
  public ResponseEntity<?> signInLocal(
      @RequestHeader(required = false, value = "X-Client-Type") String userAgent,
      @Valid @RequestBody SignInRequestDto signInRequestDto,
      HttpServletRequest request) {
    System.out.println("로그인시작");
    String clientIp = (String) request.getAttribute("clientIp");
    Map<String,String> loginJwt = signService.signInLocal(signInRequestDto, clientIp);
    System.out.println("loginJwt: "+ loginJwt);
    boolean isMobileApp = userAgent != null && userAgent.contains("mobile");

    if (isMobileApp) {
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "로그인 성공.", loginJwt));
    } else {
      System.out.println("로그인완료");
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginJwt.get("refreshToken"))
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();
  
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginJwt.get("accessToken"))
            .header(HttpHeaders.LOCATION, "/home")
            .build();
    }
  }
  
  @PostMapping("/public/in/auth/sms")
  public ResponseEntity<?> signInAuthSms(
      @RequestHeader(required = false, value = "X-Client-Type") String userAgent,
      @ClientSpecific("X-Verification-Phone-Token") String verificationPhoneToken,
      @ValidPhone @RequestParam String phone,
      HttpServletRequest request) {
    
    String clientIp = (String) request.getAttribute("clientIp");
    
    String accessToken = signService.signInAuthSms(phone, verificationPhoneToken, clientIp);
    
    boolean isMobileApp = userAgent != null && userAgent.contains("mobile");

    if (isMobileApp) {
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "로그인 성공.", accessToken));
    } else {
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .header(HttpHeaders.LOCATION, "/home")
            .build();
    }
  }
  
  @PreAuthorize("hasRole('LOCAL')")
  @PostMapping("/private/password")
  public ResponseEntity<ApiResponse<Map<String,String>>> validPassword(
      @AuthenticationPrincipal CustomUserDetails user,
      @ValidPassword @RequestParam String password,
      HttpServletRequest request){
    
    String clientIp = (String) request.getAttribute("clientIp");

    String accessPaaswordToken = signService.validPassword(user.getEmail(), password, clientIp);
    return ResponseEntity.ok(ApiResponse.of(200, "비밀번호 검증 성공.", Map.of("X-Access-Paasword-Token",accessPaaswordToken)));
  }
  
  @GetMapping("/public/access")
  public ResponseEntity<ApiResponse<UserResponseDto>> accessToken(
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {
    String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    String clientIp = (String) request.getAttribute("clientIp");
    System.out.println("AccessToken 통해 로그인 여부 파악 시작 accessToken: "+ accessToken);
    
    UserResponseDto userResponseDto = signService.getUserByAccessToken(accessToken, clientIp);
    System.out.println(userResponseDto.toString());
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "로그인 성공.", userResponseDto));
  }
  
  @PostMapping("/public/refresh")
  public ResponseEntity<?> refresh(
      @RequestHeader(required = false, value = "X-Client-Type") String userAgent,
      @RequestHeader("Authorization") String bearerToken,
      @CookieValue(required = false, value = "refreshToken") String refreshToken,
      @RequestBody(required = false) Map<String, String> body,
      HttpServletRequest request) {
    
    boolean isMobileApp = userAgent != null && userAgent.contains("mobile");

    String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    refreshToken = isMobileApp ? body.get("refreshToken") : refreshToken;

    if (accessToken == null || refreshToken == null) {
      throw new IllegalJwtException("리프레쉬 요청 - 유효하지 않은 요청 입니다.");
    }
    
    String clientIp = (String) request.getAttribute("clientIp");
    Map<String,String> loginJwt = signService.refresh(accessToken, refreshToken, clientIp);
    
    if (isMobileApp) {
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "리프레쉬 성공.", loginJwt));
    } else {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginJwt.get("refreshToken"))
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();
  
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginJwt.get("accessToken"))
            .header(HttpHeaders.LOCATION, "/home")
            .build();
    }
  }

  @PostMapping("/public/out")
  public ResponseEntity<ApiResponse<Void>> signOut(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestHeader("Authorization") String bearerToken,
      @RequestHeader(required = false, value = "User-Agent") String userAgent,
      @CookieValue(required = false, value = "refreshToken") String refreshToken,
      @RequestBody(required = false) Map<String, String> body,
      HttpServletRequest request,
      HttpServletResponse response) {

    System.out.println("로그아웃 요청.");
    System.out.println("bearerToken: "+ bearerToken);
    System.out.println("refreshToken: "+ refreshToken);
    boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");

    String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    refreshToken = isMobileApp ? body.get("refreshToken") : refreshToken;

    if (accessToken == null || refreshToken == null) {
      throw new IllegalJwtException("유효하지 않은 요청 입니다.");
    }
    
    String clientIp = (String) request.getAttribute("clientIp");
    
    signService.signout(accessToken, refreshToken, clientIp);
    
    ResponseCookie deletedCookie = ResponseCookie.from("refreshToken", "")
        .path("/")
        .maxAge(0) // 즉시 삭제
        .httpOnly(true)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, deletedCookie.toString());

    return ResponseEntity.ok(ApiResponse.of(200, "로그아웃 성공.", null));
  }
  
  @PermitAll
  @PostMapping("/public/up/first")
  public ResponseEntity<ApiResponse<Map<String,String>>> signup1(
      @RequestHeader(required = false, value = "User-Agent") String userAgent,
      @ClientSpecific("X-Verification-Email-Token") String verificationEmailToken,
      @ValidEmail @RequestParam("email") String email,
      @ValidPassword @RequestParam("password") String password,
      @ValidPassword @RequestParam("confirmPassword") String confirmPassword
      ) {
    
    boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");
    
    if(!Objects.equals(password, confirmPassword)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(400, "비밀번호가 일치하지 않습니다.", null));
    }
    
    //이메일 인증 완료 이후 5분 지난 요청 401반환
    //입력한 이메일과 이메일인증한 이메일과 다른경우 401반환
    //이미 가입되어있는 이메일 409반환
    String accessSignUpToken = signService.createJoinToken(email, password, verificationEmailToken);
    
    if(isMobileApp) {
      return ResponseEntity.ok(ApiResponse.of(200, "회원가입 1차 성공", Map.of("X-Access-SignUp-Token",accessSignUpToken)));
    }else {
      ResponseCookie cookie = ResponseCookie.from("X-Access-SignUp-Token", accessSignUpToken)
          .httpOnly(true)
          .secure(true)
          .path("/")
          .maxAge(Duration.ofMinutes(20))
          .sameSite("Lax")
          .build();
      
      return ResponseEntity
          .status(HttpStatus.OK) 
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(ApiResponse.of(200, "회원가입 1차 성공", null));
    }
  }
    
  @PermitAll
  @PostMapping("/public/up/second")
  public ResponseEntity<ApiResponse<String>> signup2(
      @RequestHeader(required = false, value = "X-Client-Type") String userAgent,
      @ClientSpecific("X-Access-SignUp-Token") String accessSignUpToken,
      @ClientSpecific("X-Verification-Phone-Token") String verificationPhoneToken,
      @RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
    
    String name = signService.signUpLocalUser(userCreateRequestDto, accessSignUpToken, verificationPhoneToken);
    
    return ResponseEntity.ok(ApiResponse.of(200, "회원 가입 성공.", name));
  }
  

}
  
//  1. 사용자가 email, password를 입력하여 로그인 요청
//  2. UsernamePasswordAuthenticationToken을 생성하여 authenticationManager.authenticate() 호출
//  3. AuthenticationManager → AuthenticationProvider 내부 DaoAuthenticationProvider -> UserDetailsService 호출하여 DB에서 사용자 조회
//  4. 입력된 비밀번호와 DB의 해시된 비밀번호를 비교 (PasswordEncoder.matches())
//  5. 인증 성공 시 Authentication 객체 반환
//  6. 인증 실패 시 예외 발생 (BadCredentialsException)
//  @PostMapping("/normal")
//  public ResponseEntity<?> loginByEmail(@RequestParam("email") String reqEmail,
//      @RequestParam("password") String reqPassword, HttpSession session, HttpServletRequest req) {
//
//    String ip = ValidateUtil.validateIP(req);
//    LoginLog loginResult = loginService.login(reqEmail, reqPassword, ip);
//
//    try {
//      if (!loginResult.getReason().equals(Reason.SUCCESS)) {
//        loginLogService.loginFail(loginResult);
//        throw new BadCredentialsException("로그인 실패 : " + loginResult.toString());
//      }
//      loginLogService.loginSuccess(loginResult);
//
//    } catch (IllegalStateException e) {
//      logger.error("로그인 로그 기록 실패: {}", e.getMessage());
//    }
//
//    Status status = loginResult.getStatus();
//    UserRequestDto userDto = userService.getUserByEmail(reqEmail);
//    if (!status.equals(Status.NORMAL)) {
//      if (status.equals(Status.STAY)) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//            .body((ApiResponse.createResponse(403, "정지된 계정입니다.", null)));
//      } else if (status.equals(Status.STOP)) {
//        return ResponseEntity.status(HttpStatus.GONE)
//            .body((ApiResponse.createResponse(410, "탈퇴한 계정입니다.", null)));
//      }
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//          (ApiResponse.createResponse(500, "죄송합니다 현재 서버에서 문제가 발생했습니다.\\n잠시 후 다시 시도해 주세요.", null)));
//    }
//
//    List<AddressDto> addressList = addressService.getSortedListByUserSeq(userDto);
//    session.setAttribute("userDto", userDto);
//    session.setAttribute("addressList", addressList);
//    session.setAttribute("userDtoExpiry", System.currentTimeMillis() + (30 * 60 * 1000));
//
//    // 이전 페이지의 도메인 확인
//    String referer = (String) session.getAttribute("previousPageUrl");
//    if (referer == null || !referer.startsWith(req.getScheme() + "://" + req.getServerName())
//        || referer.contains("/login") || referer.contains("/join")) {
//      referer = "/home";
//    }
//
//    Map<String, Object> response = ApiResponse.createResponse(200, "로그인 성공", null);
//    response.put("redirectUrl", referer);
//
//    return ResponseEntity.ok(response);
//  }


  // // simple은 기존 로그인과 다르게 세션에 아무것도 저장 안함.
  // @PostMapping("/email/simple")
  // public ResponseEntity<?> loginByEmailSimple(
  // @RequestParam("email") String reqEmail,
  // @RequestParam("password") String reqPassword,
  // HttpServletRequest req) {
  //
  // String ip = HttpUtil.getClientIp(req);
  //
  // //계정 상태 얻기
  // String status = "";
  // try {
  // status = userService.getUserStatusByEmail(reqEmail);
  // } catch (NotFoundException e) {
  // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
  // }
  //
  // //계정 상태 검사
  // if ("STAY".equals(status)) {
  // return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  // } else if ("STOP".equals(status)) {
  // return ResponseEntity.status(HttpStatus.GONE).build();
  // }
  //
  // //계정 비밀번호 확인
  // try {
  // if (userService.comparePasswordByEmail(reqEmail, reqPassword, ip)) {
  // return ResponseEntity.status(HttpStatus.OK).build();
  // } else {
  // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
  // }
  // } catch (SQLException e) {
  // e.printStackTrace();
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
  // } catch (NotFoundException e) {
  // e.printStackTrace();
  // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
  // }
  // }

