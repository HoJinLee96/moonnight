package domain.user;

import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import auth.sign.token.CustomUserDetails;
import domain.user.User.UserProvider;
import global.exception.NoSuchDataException;
import global.util.ApiResponse;
import global.validator.annotaion.ClientSpecific;
import global.validator.annotaion.ValidEmail;
import global.validator.annotaion.ValidPassword;
import global.validator.annotaion.ValidPhone;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/local/user")
public class UserInfoController {
  
  private final UserService userService;

//  LOCAL 이메일 찾기 - VerificationPhoneToken 통해
  @PostMapping("/public/find/email/by/phone")
  public ResponseEntity<ApiResponse<String>> verifyPhoneAndGetEmail(
      @ClientSpecific("X-Verification-Phone-Token") String token,
      @ValidPhone @RequestParam("phone") String phone){
    
    try {
      User user = userService.getUserByVerifyPhone(UserProvider.LOCAL,phone,token);
      return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", user.getEmail()));
    }catch (NoSuchDataException e) {
      return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
    }
  }
  
//  LOCAL 비밀번호 찾기 - 이메일 입력 통해
  @PostMapping("/public/find/pw")
  public ResponseEntity<ApiResponse<String>> findPassword(
      @ValidEmail @RequestParam("email") String email){
    
    try {
      User user = userService.getUserByUserProviderAndEmail(UserProvider.LOCAL,email);
      return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", user.getEmail()));
    }catch (NoSuchDataException e) {
      return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
    }
  }
  
//  LOCAL 비밀번호 찾기 - VerificationPhoneToken 통해
  @PostMapping("/public/find/pw/by/phone")
  public ResponseEntity<ApiResponse<Map<String,String>>> verifyPhoneAndCreateFindPwToken(
      @RequestHeader(required = false, value = "X-Client-Type")String userAgent,
      @ClientSpecific("X-Verification-Phone-Token") String token,
      @ValidEmail @RequestParam("email") String email,
      @ValidPhone @RequestParam("phone") String phone){
    
    boolean isMobileApp = userAgent != null && userAgent.contains("mobile");

    try {
        String findPwToken = userService.verifyPhoneAndCreateFindPwToken(UserProvider.LOCAL,email,phone,token);
        if(isMobileApp) {
          return ResponseEntity.ok(ApiResponse.of(200, "인증 성공.", Map.of("X-Access-FindPw-Token",findPwToken)));
        }else {
          ResponseCookie cookie = ResponseCookie.from("X-Access-FindPw-Token", findPwToken)
              .httpOnly(true)
              .secure(true)
              .path("/")
              .maxAge(Duration.ofMinutes(10))
              .sameSite("Lax")
              .build();
          
          return ResponseEntity
              .status(HttpStatus.OK) 
              .header(HttpHeaders.SET_COOKIE, cookie.toString())
              .body(ApiResponse.of(200, "인증 성공", null));
        }    
    } catch (NoSuchDataException e) {
        return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
    }
  }
  
//  LOCAL 비밀번호 찾기 - VerificationEmailToken 통해
  @PostMapping("/public/find/pw/by/email")
  public ResponseEntity<ApiResponse<Map<String, String>>> verifyEmailAndCreateFindPwToken(
      @RequestHeader(required = false, value = "X-Client-Type")String userAgent,
      @ClientSpecific("X-Verification-Email-Token") String token,
      @ValidEmail @RequestParam("email") String email){
    
    boolean isMobileApp = userAgent != null && userAgent.contains("mobile");
    
    String findPwToken = userService.verifyEmailAndCreateFindPwToken(UserProvider.LOCAL,email,token);
    
    if(isMobileApp) {
      return ResponseEntity.ok(ApiResponse.of(200, "인증 성공.", Map.of("X-Access-FindPw-Token",findPwToken)));
    }else {
      ResponseCookie cookie = ResponseCookie.from("X-Access-FindPw-Token", findPwToken)
          .httpOnly(true)
          .secure(true)
          .path("/")
          .maxAge(Duration.ofMinutes(10))
          .sameSite("Lax")
          .build();
      
      return ResponseEntity
          .status(HttpStatus.OK) 
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(ApiResponse.of(200, "인증 성공", null));
    }
  }
  
  // LOCAL 비밀번호 변경 - AccessFindpwToken 통해
  @PatchMapping("/public/update/pw")
  public ResponseEntity<ApiResponse<Void>> updatePasswordByFindPwToken(
      @ClientSpecific("X-Access-FindPw-Token") String accessFindPwToken,
      @ValidPassword @RequestParam("password") String password,
      @ValidPassword @RequestParam("confirmPassword") String confirmPassword,
      HttpServletRequest request){
    
    if(password!=confirmPassword) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(400, "비밀번호가 일치하지 않습니다.", null));
    }
    
    String clientIp = (String) request.getAttribute("clientIp");
    
    userService.updatePasswordByFindPwToken(UserProvider.LOCAL, password, accessFindPwToken, clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "비밀번호 변경 성공", null));
  }
  
  // LOCAL 회원 탈퇴
  @PreAuthorize("hasRole('LOCAL')")
  @PostMapping("/private/delete")
  public ResponseEntity<ApiResponse<Void>> stopUser(
      @ClientSpecific("X-Access-Password-Token") String accessPasswordToken,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    userService.deleteUser(userDetails.getUserProvider(), userDetails.getEmail(), accessPasswordToken);
    
    return ResponseEntity.ok(ApiResponse.of(200, "회원 탈퇴 성공.", null));
  }
  
  // LOCAL 이메일 중복 검사
  @PostMapping("/public/exist/email")
  public ResponseEntity<ApiResponse<Void>> isEmailExists(
      @ValidEmail @RequestParam("email") String email) {
    userService.isEmailExists(UserProvider.LOCAL, email);
    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
  }
  
  // LOCAL 휴대폰 중복 검사
  @PostMapping("/public/exist/phone")
  public ResponseEntity<ApiResponse<Void>> isPhoneExists(
      @ValidPhone @RequestParam("phone") String phone) {

    userService.isPhoneExists(UserProvider.LOCAL, phone);
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
  }
  
//  @PatchMapping("/update/passwordByPhone")
//  public ResponseEntity<?> updatePassword(
//      @RequestParam("email") String reqEmail,
//      @RequestParam("phone") String reqPhone,
//      @RequestParam String newPassword,
//      @RequestParam String newConfirmPassword) throws SQLException {
//    
//    ValidateUtil.validateEmail(reqEmail);
//    ValidateUtil.validatePhone(reqPhone);
//    ValidateUtil.validatePassword(newPassword);
//    ValidateUtil.validatePassword(newConfirmPassword);
//    
//    //두 비밀번호 값 불일치
//    if(!newPassword.equals(newConfirmPassword))
//      throw new IllegalArgumentException("비밀번호 변경 실패 : 새로운 두 비밀번호 값 불일치.");
//    
//    //최근 인증 정보 확인
//    verificationService.existVerfivation(reqPhone);
//    
//    //회원 정보 확인
//    String email = userService.getEmailByPhone(reqPhone);
//    if(!reqEmail.equals(email)) 
//      throw new AccessDeniedException("비밀번호 변경 실패 : 사용자는 존재하지만, 입력한 이메일이 등록된 이메일과 다름");
//    
//    //비밀번호 변경
//    userService.updatePassword(reqEmail, newPassword);
//    
//    //로그인 실패 로그에 비밀번호 변경 했다고 기록, "-1" 로 비밀번호 변경을 의미
//    loginLogService.failLogInit(reqEmail, -1);
//    
//    return ResponseEntity.ok(ResponseUtil.createResponse(200, "비밀번호 변경 성공", reqEmail));
//  }
//  
//  @PatchMapping("/update/addressSeq")
//  public ResponseEntity<?> updateAddressSeq(
//      @RequestParam int addressSeq) {
//    
//    UserCreateRequestDto userDto = (UserCreateRequestDto) session.getAttribute("userDto");
//    if (userDto == null) {
//      throw new HttpSessionRequiredException("세션의 유저정보가 존재하지 않습니다.");
//    }
//    userDto.validateUserDto();
//    
//    if (addressSeq <= 0)
//      throw new IllegalArgumentException("데이터 형식 부적합 [주소키] addressSeq : " + addressSeq);
//  
//    UserCreateRequestDto updatedUserDto = userService.updateAddressSeq(userDto.getSequence(), addressSeq);
//    session.setAttribute("userDto", updatedUserDto);
//  
//    List<AddressRequestDto> addressList = addressService.getSortedListByUserSeq(updatedUserDto);
//    session.setAttribute("addressList", addressList);
//  
//    return ResponseEntity.ok(ApiResponse.createResponse(200, "대표 주소 변경 성공", addressSeq));
//  }
//  
//  @PatchMapping("/ouath/connect")
//  public ResponseEntity<?> registerConnect(HttpSession session) {
//
//    UserDto userDto = (UserDto) session.getAttribute("confirmUserDto");
//    if (userDto == null) {
//      userDto = (UserDto) session.getAttribute("userDto");
//    }
//    
//    OAuthDto oAuthDto = (OAuthDto) session.getAttribute("confirmOAuthDto");
//    if (oAuthDto == null) {
//      oAuthDto = (OAuthDto) session.getAttribute("oAuthDto");
//    }
//    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("confirmOAuthToken");
//
//    Long oAuthTokenExpiry = (Long) session.getAttribute("confirmOAuthTokenExpiry");
//
//    if (userDto == null || oAuthDto == null || oAuthToken == null || oAuthTokenExpiry == null
//        || System.currentTimeMillis() > oAuthTokenExpiry)
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//
//    try {
//      int oAuthSeq = oAuthService.registerOAuth(oAuthDto, userDto.getSequence());
//      oAuthDto.setSequence(oAuthSeq);
//      session.setAttribute("userDto", userDto);
//      session.setAttribute("oAuthDto", oAuthDto);
//      session.setAttribute("oAuthToken", oAuthToken);
//      session.setAttribute("oAuthTokenExpiry",
//          System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
//      return ResponseEntity.status(HttpStatus.OK).build();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//  }
  
//  @PostMapping("/exist/emailAndPhone")
//  public ResponseEntity<ApiResponse<Boolean>> isEmailPhoneExist(@RequestParam("email") String reqEmail,@RequestParam("phone") String reqPhone) {
//    
//    ValidateUtil.validateEmail(reqEmail);
//    ValidateUtil.validatePhone(reqPhone);
//    
//    boolean result = userService.isEmailPhoneExists(reqEmail, reqPhone);
//    
//    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", result));
//  }
  
//  @PostMapping("/join")
//  public ResponseEntity<?> joinUser(@RequestParam("email") String reqEmail, @RequestParam("password") String reqPassword) {
//    
//    RegisterUserDto registerUserDto = new RegisterUserDto(reqEmail, reqPassword);
//    session.setAttribute("registerUserDto", registerUserDto);
//    
//    return ResponseEntity.ok(ApiResponse.createResponse(200, "회원가입 1차 성공", registerUserDto));
//  }
//
//  @PostMapping("/join/second")
//  public ResponseEntity<?> registerNormalJoinSecond(@RequestBody Map<String, Object> reqData, HttpServletRequest req) throws SQLException {
//
//      ObjectMapper objectMapper = new ObjectMapper();
//      RegisterUserDto registerUserDto = objectMapper.convertValue(reqData.get("registerUserDto"), RegisterUserDto.class);
//      registerUserDto.validateUserDto();
//  
//      AddressRequestDto addressDto = objectMapper.convertValue(reqData.get("addressDto"), AddressRequestDto.class);
//      addressDto.validateAddressDto();
//        
//      UserCreateRequestDto userDto = userService.registerUser(registerUserDto, addressDto);
//      
//      return ResponseEntity.ok(ApiResponse.createResponse(200, "회원 가입 성공.", userDto));
//  }

}
