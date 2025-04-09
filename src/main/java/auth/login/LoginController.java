package auth.login;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import auth.login.token.CustomUserDetails;
import global.exception.IllegalJwtException;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/sign")
public class LoginController {

  private final LoginService loginService;

  @PermitAll
  @PostMapping("/in/local")
  public ResponseEntity<?> loginLocal(@Valid @RequestBody LoginRequestDto loginRequestDto,
                                      HttpServletRequest request) {
    
    String clientIp = (String) request.getAttribute("clientIp");
    Map<String,String> loginJwt = loginService.loginLocal(loginRequestDto, clientIp);
    
    String userAgent = request.getHeader("User-Agent");
    boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");

    if (isMobileApp) {
      return ResponseEntity.ok(loginJwt);
    } else {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginJwt.get("refreshToken"))
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();
  
        return ResponseEntity
            .status(HttpStatus.FOUND) // 302
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginJwt.get("accessToken"))
            .header(HttpHeaders.LOCATION, "/home")
            .build();
    }
  }
  
  @PermitAll
  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(HttpServletRequest request, 
                                   HttpServletResponse response)
                                   throws JsonProcessingException, IOException {
    
    String bearerToken = request.getHeader("Authorization");
    String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    String refreshToken = (String) request.getAttribute("refreshToken");
    if (accessToken == null || refreshToken == null) {
      throw new IllegalJwtException("유효하지 않은 요청 입니다.");
    }
    
    String clientIp = (String) request.getAttribute("clientIp");
    Map<String,String> loginJwt = loginService.refresh(accessToken, refreshToken, clientIp);
    
    String userAgent = request.getHeader("User-Agent");
    boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");

    if (isMobileApp) {
      return ResponseEntity.ok(loginJwt);
    } else {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginJwt.get("refreshToken"))
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();
  
        return ResponseEntity
            .status(HttpStatus.FOUND) // 302
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginJwt.get("accessToken"))
            .header(HttpHeaders.LOCATION, "/home")
            .build();
    }
  }

  @PreAuthorize("hasRole('OAUTH') or hasRole('USER')")
  @PostMapping("/out")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails user,
                                  @RequestBody(required = false) Map<String, String> body,
                                  @RequestHeader(value = "Authorization", required = false) String bearerToken,
                                  HttpServletRequest request) {

    String userAgent = request.getHeader("User-Agent");
    boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");

    String accessToken = isMobileApp ? body.get("refreshToken") : bearerToken.replace("Bearer ", "");

    if (accessToken == null) {
        throw new IllegalJwtException("잘못된 요청이 아닙니다.");
    }
    
    String clientIp = (String) request.getAttribute("clientIp");
    
    loginService.logout(accessToken, user.getUserId()+"", clientIp);

    return ResponseEntity.ok().build();
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

