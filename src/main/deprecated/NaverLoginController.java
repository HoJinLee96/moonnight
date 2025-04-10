package deprecated;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.UserCreateRequestDto;
import dto.request.UserCreateRequestDto.Provider;
import entity.OAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;
import util.ApiResponse;
@Deprecated
@Controller
@RequestMapping("/naver")
public class NaverLoginController {

  private final UserService userServices;
  private final OAuthUserService oAuthService;
  private final NaverOAuthLoginService naverOAuthLoginService;
  private static final Logger logger = LoggerFactory.getLogger(NaverLoginController.class);
  private static final ObjectMapper mapper = new ObjectMapper();


  @Autowired
  public NaverLoginController(UserService userServices, OAuthUserService oAuthService,
      NaverOAuthLoginService naverOAuthLoginService) {
    super();
    this.userServices = userServices;
    this.oAuthService = oAuthService;
    this.naverOAuthLoginService = naverOAuthLoginService;
  }

  @GetMapping("/login/url")
  public void naverLogin(HttpServletRequest req, HttpServletResponse res)
      throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
    System.out.println("NaverLoginController.naverLogin() 실행");

    String url = naverOAuthLoginService.getNaverAuthorizeUrl("authorize");
    try {
      res.sendRedirect(url);
    } catch (Exception e) {
      e.printStackTrace();
      try {
        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().write(
            "<script>alert('현재 네이버 로그인을 이용 할 수 없습니다.'); window.location.href='/login';</script>");
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  @GetMapping("/login/callback")
  public ResponseEntity<?> loginNaverCallBack(HttpServletRequest req,
      HttpServletResponse res, Callback callback) {
    /**
     * 1. 결과 확인
     * 2. 인가 코드 통해 접근 토큰 얻기
     * 3. 접근토큰 통해 고객 정보 얻기
     * 4. oauth 테이블에서 데이터 확인
     * 4-1. 처음 이용자
     *  4-1-1. user 테이블에 같은 이메일로 가입된 계정 있는 경우
     *  4-1-2. user 테이블에 같은 이메일로 가입된 계정 없는 경우
     * 4-2. 기존 유저 이용자
     *  4-2-1. 기존계정과 연동된 sns 계정 이용자 경우
     *  4-2-2. 회원 탈퇴 했다가 다시 가입한 유저인 경우
     *  4-2-3. sns 전용 계정 이용자 경우
     **/
    System.out.println("NaverLoginController.loginNaverCallBack() 시작");

    HttpHeaders headers = new HttpHeaders();
    HttpSession session = req.getSession();

    // 리다이렉트할 이전 url 확보 및 검사
    String previousPageUrl = "/home"; 
    String referer = (String) session.getAttribute("previousPageUrl");
    if (referer != null && referer.startsWith(req.getScheme() + "://" + req.getServerName()) && !referer.contains("/login")) {
      previousPageUrl =  referer;
      } 

    // 1. 결과 확인
    if (callback.getError() != null) {
      logger.warn("네이버 로그인 오류: {}", callback.getError_description());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.createResponse(500, "현재 네이버 로그인을 이용 할 수 없습니다.", null));
    }

    try {
      // 2. 접근 토큰 얻기
      String responseToken = naverOAuthLoginService.getTokenUrl("token", callback.getCode());
      OAuthTokenDto oAuthToken = mapper.readValue(responseToken, OAuthTokenDto.class);

      // 3. 접근토큰 통해 고객 정보 얻기
      String responseUser = naverOAuthLoginService.getNaverUserByToken(oAuthToken);
      NaverUserInfoResponseDto naverUserInfoResponseDto = mapper.readValue(responseUser, NaverUserInfoResponseDto.class);
      
      // 4. oauth테이블 통해 처음 이용자 또는 기존 이용자 확인
      String oAuthid = naverUserInfoResponseDto.getResponse().getId(); // 계정 고유 id
      
      try {
        OAuth oAuthDto = oAuthService.getOAuthByOAuthId(Provider.NAVER, oAuthid);

        // 4-1. 기존 유저 이용자(oauth테이블에 데이터가 있는 경우)
        // 4-1-1. 유저 상태 확인
        if(!oAuthDto.getStatus().equals(OAuth.Status.NORMAL)) {
          // 탈퇴 계정
          if(oAuthDto.getStatus().equals(OAuth.Status.STOP)) {
            oAuthService.updateStatusByOAuthId(oAuthDto.getId());
          }
          // 정지 계정
          else if(oAuthDto.getStatus().equals(OAuth.Status.STAY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body((ApiResponse.createResponse(403, "정지된 계정입니다.", null)));
          }
        }
        
        // 4-1-2. 기존 계정과 연동된 sns 계정 이용자 경우
        if (oAuthDto.getUserSeq() != 0) {
          // 해당 데이터의 userSeq 추출 및 user 테이블 데이터 읽기
          UserCreateRequestDto userDto = userServices.getUserBySeq(oAuthDto.getUserSeq()); 
          // 세션에 user 등록
          session.setAttribute("userDto", userDto);
        }
        
        
        // 4-2-3. sns 전용 계정 이용자 경우
        // 세션에 토큰,OAuthDto 등록 (기존 회원에 연동된 계정이든 소셜 전용 계정이든 세션에 토큰 등록)
        session.setAttribute("oAuthDto", oAuthDto);
        session.setAttribute("oAuthToken", oAuthToken);
        session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
        headers.setLocation(URI.create(previousPageUrl));
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
      
      }catch (NoSuchElementException e) {
        // 4-1. 처음 이용자인 경우(oauth테이블에 데이터가 없는 경우)
        String email = naverUserInfoResponseDto.getResponse().getEmail();
        OAuth oAuthDto = new OAuth(naverUserInfoResponseDto);
        try {
          UserCreateRequestDto userDto = userServices.getUserByEmail(email);
          
          // 4-1-1. user 테이블에 같은 이메일로 가입된 계정 있는 경우
          headers.setLocation(URI.create("/join/sns/confirm"));
          session.setAttribute("confirmUserDto", userDto);
          session.setAttribute("confirmOAuthDto", oAuthDto);
          session.setAttribute("confirmOAuthToken", oAuthToken);
          session.setAttribute("confirmOAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
          return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
          
        }catch(NoSuchElementException e1) {
          
          // 4-1-2. user 테이블에 같은 이메일로 가입된 계정 없는 경우
          int oauthSeq = oAuthService.registerOAuth(oAuthDto);
          oAuthDto.setSequence(oauthSeq);
          session.setAttribute("oAuthDto", oAuthDto);
          session.setAttribute("oAuthToken", oAuthToken);
          session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
          headers.setLocation(URI.create(previousPageUrl));
          return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
        }
        
      }
      
       
    } catch (IOException | SQLException e) {
      e.printStackTrace();
      headers.setLocation(URI.create("/login"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers)
          .body("현재 네이버 로그인을 이용 할 수 없습니다.");
    }
  }

  // 토큰 갱신
  @GetMapping("/token/refresh")
  public ResponseEntity<String> naverTokenRefresh(HttpServletRequest request,HttpSession session) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
//    HttpSession session = request.getSession(false);

    ResponseEntity<String> sessionConfirmResult = sessionConfirm(session, headers);
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;

    OAuthTokenDto oAuthToken = (OAuthTokenDto) session.getAttribute("oAuthToken");

    // 리프레시 토큰으로 새로운 엑세스 토큰 요청
    OAuthTokenDto newToken = null;
    try {
      String responseToken =
          naverOAuthLoginService.updateTokenUrl("token", "refresh_token", oAuthToken);
      newToken = mapper.readValue(responseToken, OAuthTokenDto.class);
    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    }

    // 새로운 토큰을 세션에 저장
    session.setAttribute("oAuthToken", newToken);
    session.setAttribute("oAuthTokenExpiry",
        System.currentTimeMillis() + (Integer.parseInt(newToken.getExpires_in()) * 1000));
    System.out.println("갱신 완료");
    return ResponseEntity.ok("토큰 갱신 성공");
  }



  // 회원 탈퇴
  @GetMapping("/token/delete")
  public ResponseEntity<String> deleteToken(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
    HttpSession session = request.getSession();

    // 1. 세션에 저장되어있는 OAuthDto,OAuthToken 가지고 회원 탈퇴 진행.
    ResponseEntity<String> sessionConfirmResult = sessionConfirm(session, headers);
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;

    OAuth oAuthDto = (OAuth) session.getAttribute("oAuthDto");
    OAuthTokenDto oAuthToken = (OAuthTokenDto) session.getAttribute("oAuthToken");

    try {
      // 2. 토큰 갱신
      String updateToken = naverOAuthLoginService.updateTokenUrl("token", "refresh_token", oAuthToken);
      OAuthTokenDto newToken = mapper.readValue(updateToken, OAuthTokenDto.class);
      session.setAttribute("oAuthToken", newToken);
      session.setAttribute("oAuthTokenExpiry",System.currentTimeMillis() + (Integer.parseInt(newToken.getExpires_in()) * 1000));
      
      // 3. 회원 정보 얻기
      String responseUser = naverOAuthLoginService.getNaverUserByToken(newToken);
      NaverUserInfoResponseDto  naverUser = mapper.readValue(responseUser, NaverUserInfoResponseDto.class);
      
      // 4. 얻은 회원 정보와, OAuthDto 일치한지 확인(고유 id값)
      if (!(oAuthDto.getId().equals(naverUser.getResponse().getId()))) {
        session.invalidate();
        System.out.println("얻은 회원 정보와, OAuthDto 일치한지 확인(고유 id값) : 일치하지 않음.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body("재로그인 후 탈퇴 이용 부탁드립니다.");
      }
      
      // 5. DB 회원 탈퇴 적용(stats 값 stop으로 변경)
      oAuthService.stopOAuthDtoByOAuthId(oAuthDto.getId());
      session.invalidate();

      // 6. 네이버 서버 회원 탈퇴 진행
      String responseToken = naverOAuthLoginService.updateTokenUrl("token", "delete", newToken);
      OAuthTokenDto deleteToken = mapper.readValue(responseToken, OAuthTokenDto.class);
      String result = deleteToken.getResult();
      if (result.equals("success")) {
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("현재 탈퇴 완료.");
      } else {
        System.out.println(deleteToken.getError() + deleteToken.getError_description());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers)
            .body("현재 탈퇴가 불가능합니다. 잠시후 시도 부탁드립니다.");
      }
    } catch (IOException | SQLException e) {
      e.printStackTrace();
      headers.setLocation(URI.create("/join/sns/confirm"));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body("현재 탈퇴가 불가능합니다. 잠시후 시도 부탁드립니다.");
    }
    
  }
  
  @PostMapping("/register")
  public ResponseEntity<?> register(HttpSession session){
    
    ResponseEntity<?> sessionConfirmResult = sessionConfirm2(session);
    
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;
    
    OAuth oAuthDto = (OAuth)session.getAttribute("confirmOAuthDto");
    OAuthTokenDto oAuthToken = (OAuthTokenDto)session.getAttribute("confirmOAuthToken");
    
    try {
      int oAuthSeq = oAuthService.registerOAuth(oAuthDto);
      oAuthDto.setSequence(oAuthSeq);
      session.setAttribute("oAuthDto", oAuthDto);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
      
      // 컨펌세션 삭제
      session.removeAttribute("confirmUserDto");
      session.removeAttribute("confirmOAuthDto");
      session.removeAttribute("confirmOAuthToken");
      session.removeAttribute("confirmOAuthTokenExpiry");
      
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/register/connect")
  public ResponseEntity<?> registerConnect(HttpSession session){
    
    ResponseEntity<?> sessionConfirmResult = sessionConfirm2(session);
   
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;
    
    UserCreateRequestDto userDto = (UserCreateRequestDto)session.getAttribute("confirmUserDto");
    OAuth oAuthDto = (OAuth)session.getAttribute("confirmOAuthDto");
    OAuthTokenDto oAuthToken = (OAuthTokenDto)session.getAttribute("confirmOAuthToken");
    
    try {
      int oAuthSeq = oAuthService.registerOAuth(oAuthDto, userDto.getSequence());
      oAuthDto.setSequence(oAuthSeq);
      session.setAttribute("userDto", userDto);
      session.setAttribute("oAuthDto", oAuthDto);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
      
      // 컨펌세션 삭제
      session.removeAttribute("confirmUserDto");
      session.removeAttribute("confirmOAuthDto");
      session.removeAttribute("confirmOAuthToken");
      session.removeAttribute("confirmOAuthTokenExpiry");
      
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // 세션 검사
  private ResponseEntity<String> sessionConfirm(HttpSession session, HttpHeaders headers) {
    if (session == null) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("세션이 없습니다.");
    }
    OAuth oAuthDto = (OAuth) session.getAttribute("oAuthDto");
    if (oAuthDto == null) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("인증정보가 없습니다.");
    }
    OAuthTokenDto oAuthToken = (OAuthTokenDto) session.getAttribute("oAuthToken");
    if (oAuthToken == null) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("토큰이 없습니다.");
    }
    Long oAuthTokenExpiry = (Long) session.getAttribute("oAuthTokenExpiry");
    if (oAuthTokenExpiry == null || System.currentTimeMillis() > oAuthTokenExpiry) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("토큰 유효기간이 지났습니다.");
    }
    return ResponseEntity.ok("성공");
  }
  
  // 컨펌세션 검사
  private ResponseEntity<?> sessionConfirm2(HttpSession session) {

    UserCreateRequestDto userDto = (UserCreateRequestDto) session.getAttribute("confirmUserDto");
    OAuth oAuthDto = (OAuth) session.getAttribute("confirmOAuthDto");
    OAuthTokenDto oAuthToken = (OAuthTokenDto) session.getAttribute("confirmOAuthToken");
    Long oAuthTokenExpiry = (Long) session.getAttribute("confirmOAuthTokenExpiry");

    if(userDto==null || oAuthDto==null || oAuthToken==null || oAuthTokenExpiry == null || System.currentTimeMillis() > oAuthTokenExpiry)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    
    return ResponseEntity.ok("성공");
  }
  
//  // 컨펌세션 삭제
//  private void sessionDelete(HttpSession session) {
//    session.removeAttribute("confirmUserDto");
//    session.removeAttribute("confirmOAuthDto");
//    session.removeAttribute("confirmOAuthToken");
//    session.removeAttribute("confirmOAuthTokenExpiry");
//  }
  
  // 
//  private Optional<OAuthDto> getOAuthDtoByOAuthId(String oAuthid) throws SQLException{
//    try {
//      OAuthDto oAuthDto = oAuthService.getOAuthByOAuthId("NAVER", oAuthid);
//      return Optional.of(oAuthDto);
//    } catch (NotFoundException e) {
//      return Optional.empty();
//    }
//  }
//  
//  private Optional<UserDto> getUserDtoByEmail(String email) throws SQLException{
//    try {
//      UserDto userDto = userServices.getUserByEmail(email);
//      return Optional.of(userDto);
//    } catch (NotFoundException e) {
//      return Optional.empty();
//    } 
//  }
  
}
