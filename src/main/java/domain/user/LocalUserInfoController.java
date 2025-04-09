package domain.user;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import auth.login.token.CustomUserDetails;
import domain.user.User.UserProvider;
import global.annotation.ValidEmail;
import global.annotation.ValidPassword;
import global.annotation.ValidPhone;
import global.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/private/local/user")
@RequiredArgsConstructor
public class LocalUserInfoController {
  
  private final UserService userService;

  @PostMapping("/join/1")
  public ResponseEntity<ApiResponse<String>> createJoinToken(
      @RequestHeader("X-Verification-Email-Token") String verificationEmailToken,
      @RequestParam @ValidEmail String email,
      @RequestParam @ValidPassword String password) {

    String joinToken = userService.createJoinToken(email, password, verificationEmailToken);

    return ResponseEntity.ok(ApiResponse.of(200, "회원가입 1차 성공", joinToken));
  }
    
  @PostMapping("/join/2")
  public ResponseEntity<ApiResponse<String>> join2(
      @RequestHeader("X-Access-Join-Token") String accessJoinToken,
      @RequestHeader("X-Verification-Phone-Token") String verificationPhoneToken,
      @RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
  
    String name = userService.joinLocalUser(userCreateRequestDto, accessJoinToken, verificationPhoneToken);
    
    return ResponseEntity.ok(ApiResponse.of(200, "회원 가입 성공.", name));
  }
  
  
//  휴대폰 인증 통해 이메일 찾기 
    @PostMapping("/find/email/by/phone")
    public ResponseEntity<?> verifyPhoneAndGetEmail(
      @RequestHeader("X-Verification-Phone-Token") String token,
      @RequestBody Map<String,String> requestBody){
      
        String reqPhone = requestBody.get("phone");
        
        String findPwToken = userService.verifyPhoneForEmail(UserProvider.LOCAL,reqPhone,token);
        
        return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", findPwToken));
    }
  
//  휴대폰 인증 통해 비밀번호 찾기 
  @PostMapping("/find/pw/by/phone")
  public ResponseEntity<?> verifyPhoneAndCreateFindPwToken(
      @RequestHeader("X-Verification-Phone-Token") String token,
      @RequestBody Map<String,String> requestBody){
    
    String reqEmail = requestBody.get("email");
    String reqPhone = requestBody.get("phone");

    String findPwToken = userService.verifyPhoneAndCreateFindPwToken(UserProvider.LOCAL,reqEmail,reqPhone,token);

    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", findPwToken));
  }
  
//  이메일 인증 통해 비밀번호 찾기 
  @PostMapping("/find/pw/by/email")
  public ResponseEntity<?> verifyEmailAndCreateFindPwToken(
      @RequestHeader("X-Verification-Email-Token") String token,
      @RequestBody Map<String,String> requestBody){
    
    String reqEmail = requestBody.get("email");
    
    String findPwToken = userService.verifyEmailAndCreateFindPwToken(UserProvider.LOCAL,reqEmail,token);
    
    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", findPwToken));
  }
  
  // (휴대폰,이메일) 인증 통해 비밀번호 변경
  @PostMapping("/update/pw")
  public ResponseEntity<?> updatePasswordByFindPwToken(
      @RequestHeader("X-Access-Findpw-Token") String token,
      @RequestBody Map<String,String> requestBody,
      HttpServletRequest request){
    
    String clientIp = (String) request.getAttribute("clientIp");
      
    String reqNewPassword = requestBody.get("newPassword");
    String reqNewConfirmPassword = requestBody.get("newConfirmPassword");
    if(reqNewPassword!=reqNewConfirmPassword) throw new IllegalArgumentException("새로운 비밀번호 부적합.");
    
    userService.updatePasswordByFindPwToken(UserProvider.LOCAL, reqNewPassword, token, clientIp);
    
    return ResponseEntity.ok(ApiResponse.of(200, "비밀번호 변경 성공", null));
  }
  
  // 비밀번호 검사 통해 비밀번호 변경
  @PostMapping("/delete")
  public ResponseEntity<ApiResponse<?>> stopUser(
      @RequestHeader("X-Access-Password-Token") String accessPasswordToken,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    userService.deleteUser(userDetails.getUserProvider(), userDetails.getEmail(), accessPasswordToken);
    
    return ResponseEntity.ok(ApiResponse.of(200, "회원 탈퇴 성공.", null));
  }
  
  @GetMapping("/exist/email")
  public ResponseEntity<ApiResponse<Void>> isEmailExists(@RequestParam @ValidEmail String reqEmail) {
  
    userService.isEmailExists(UserProvider.LOCAL, reqEmail);
  
    return ResponseEntity.ok(ApiResponse.of(200, "조회 성공", null));
  }
  
  @PostMapping("/exist/phone")
  public ResponseEntity<ApiResponse<Void>> isPhoneExists(@RequestParam @ValidPhone String reqPhone) {

    userService.isPhoneExists(UserProvider.LOCAL, reqPhone);
    
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
