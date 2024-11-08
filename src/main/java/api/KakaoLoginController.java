package api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.OAuthDto;
import dto.UserDto;
import dtoKakaoLogin.KakaoUserInfoResponseDto;
import dtoNaverLogin.OAuthToken;
import exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.KakaoOAuthLoginService;
import service.OAuthService;
import service.UserService;

@Controller
@RequestMapping("/kakao")
public class KakaoLoginController {

  private UserService userServices;
  private OAuthService oAuthService;
  private KakaoOAuthLoginService kakaoOAuthLoginService;
  public ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public KakaoLoginController(UserService userServices, OAuthService oAuthService,
      KakaoOAuthLoginService kakaoOAuthLoginService) {
    super();
    this.userServices = userServices;
    this.oAuthService = oAuthService;
    this.kakaoOAuthLoginService = kakaoOAuthLoginService;
  }

  @GetMapping("/login/url")
  public void kakaoLoginUrl(HttpServletRequest req, HttpServletResponse res) {
    System.out.println("KakaoLoginController.kakaoLogin() 실행");

    String url;
    try {
      url = kakaoOAuthLoginService.getKakaoLoginUrl("authorize");
      res.sendRedirect(url);
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      try {
        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().write("<script>alert('현재 카카오 로그인을 이용 할 수 없습니다.'); window.location.href='/login';</script>");
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }


  @GetMapping("/login/callback")
  public ResponseEntity<String> kakaoLoginCallBack(HttpServletRequest req, HttpServletResponse res,
      @RequestParam(value = "code", defaultValue = "defaultCode") String code,
      @RequestParam(value = "error", defaultValue = "defaultValue") String error) throws IOException {
    /**
     * 1. 결과 확인
     * 2. 인가 코드 통해 접근 토큰 얻기 
     * 3. 접근토큰 통해 고객 정보 얻기 
     * 4. oauth 테이블에서 데이터 확인 
     *  4-1. 처음 이용자
     *      4-1-1. user 테이블에 같은 이메일로 가입된 계정 있는 경우
     *      4-1-2. user 테이블에 같은 이메일로 가입된 계정 없는 경우
     *  4-2. 기존 유저 이용자
     *      4-2-1. 기존 계정과 연동된 sns 계정 이용자 경우
     *      4-2-2. 회원 탈퇴 했다가 다시 가입한 유저인 경우
     *      4-2-3. sns 전용 계정 이용자 경우
     **/
    System.out.println("KakaoLoginController.loginKakaoCallBack() 실행");
    
    HttpHeaders headers = new HttpHeaders();
    HttpSession session = req.getSession();

    
    // 1. 결과 확인
    if (!error.equals("defaultValue")) {
      System.out.println(error);
      session.setAttribute("message", "현재 카카오 로그인을 이용 할 수 없습니다.");
      headers.setLocation(URI.create("/login"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }

    try {
      // 2. 인가 코드 통해 접근 토큰 얻기
      String responseBody = kakaoOAuthLoginService.getKakaoToken("token", code);
      OAuthToken oAuthToken = mapper.readValue(responseBody, OAuthToken.class);

      // 3. 접근토큰 통해 고객 정보 얻기
      KakaoUserInfoResponseDto kakaoUserInfoResponseDto = kakaoOAuthLoginService.getKakaoUserByToken(oAuthToken);

      // 4. oauth테이블 통해처음 이용자 인지 기존 이용자인지 확인
      String oAuthid = kakaoUserInfoResponseDto.getId() + ""; // 계정 고유 id
      Optional<OAuthDto> optionalOAuthDto = getOAuthDtoByoAuthid(oAuthid);
      
      // 리다이렉트할 이전 url 확보 및 검사
      String previousPageUrl = "/home"; 
      String referer = (String) session.getAttribute("previousPageUrl");
      if (referer != null && referer.startsWith(req.getScheme() + "://" + req.getServerName()) && !referer.contains("/login")) {
        previousPageUrl =  referer;
        } 

      // 4-1. 처음 이용자인 경우(oauth테이블에 데이터가 없는 경우)
      if (!optionalOAuthDto.isPresent()) {
        String email = kakaoUserInfoResponseDto.getKakao_account().getEmail();
        Optional<UserDto> optionalUserDto = getUserDtoByEmail(email);
        OAuthDto oAuthDto = new OAuthDto(kakaoUserInfoResponseDto);

        // 4-1-1. user 테이블에 같은 이메일로 가입된 계정 있는 경우
        if (optionalUserDto.isPresent()) {
          headers.setLocation(URI.create("/join/sns/confirm"));
          session.setAttribute("confirmUserDto", optionalUserDto.get());
          session.setAttribute("confirmOAuthDto", oAuthDto);
          session.setAttribute("confirmOAuthToken", oAuthToken);
          session.setAttribute("confirmOAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
          return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
        }
        
        // 4-1-2. user 테이블에 같은 이메일로 가입된 계정 없는 경우
        int oauthSeq = oAuthService.registerOAuth(oAuthDto);
        oAuthDto.setUserSeq(oauthSeq);
        
//        session.setAttribute("oAuthDto", oAuthDto);
//        session.setAttribute("oAuthToken", oAuthToken);
//        session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
//        headers.setLocation(URI.create(previousPageUrl));
//        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
      }
      
      // 4-2. 기존 유저 이용자(oauth테이블에 데이터가 있는 경우)
      OAuthDto oAuthDto = optionalOAuthDto.get();
      
      // 4-2-1. 기존 계정과 연동된 sns 계정 이용자 경우
      if (oAuthDto.getUserSeq() != 0) {
        // 해당 데이터의 userSeq 추출 및 user 테이블 데이터 읽기
        UserDto userDto = userServices.getUserBySeq(oAuthDto.getUserSeq()); 
        // 세션에 user 등록
        session.setAttribute("userDto", userDto);
      }
      
      // 4-2-2. 회원 탈퇴 했다가 다시 가입한 유저인 경우
      if(oAuthDto.getStatus().name().equals(OAuthDto.Status.STOP.name())) {
        oAuthService.updateStatusByOAuthId(oAuthDto.getId());
      }
      
      // 4-2-3. sns 전용 계정 이용자 경우
      // 세션에 토큰,OAuthDto 등록 (기존 회원에 연동된 계정이든 소셜 전용 계정이든 세션에 토큰 등록)
       session.setAttribute("oAuthDto", oAuthDto);
       session.setAttribute("oAuthToken", oAuthToken);
       session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
       headers.setLocation(URI.create(previousPageUrl));
       return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
       
    } catch ( NotFoundException | SQLException | IOException e) {
      e.printStackTrace();
      headers.setLocation(URI.create("/login"));
      session.setAttribute("message", "로그인 실패");
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }
  }

  // 토큰 갱신
  @GetMapping("/token/refresh")
  public ResponseEntity<String> kakaoTokenRefresh(HttpServletRequest req) {
    System.out.println("KakaoLoginController.kakaoRefreshToken() 실행");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");

    HttpSession session = req.getSession(false);
    ResponseEntity<String> sessionConfirmResult = sessionConfirm(session, headers);
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;

    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("oAuthToken");

    // 리프레시 토큰으로 새로운 엑세스 토큰 요청
    try {
      oAuthToken = updateToken(oAuthToken);
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body("소셜 로그인 연장 실패");
    }

    // 새로운 토큰을 세션에 저장
    session.setAttribute("oAuthToken", oAuthToken);
    session.setAttribute("oAuthTokenExpiry",System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
    System.out.println("갱신 완료");
    return ResponseEntity.status(HttpStatus.OK).headers(headers).body("소셜 로그인 연장 성공");
  }

  // 회원 탈퇴
  @GetMapping("/token/delete")
  public ResponseEntity<String> kakaoDeleteToken(HttpServletRequest request) {
    /**
    1. 세션 데이터 여부 확인.
    2. OAuthToken 갱신
    3. 회원 정보 얻기
    4. 얻은 회원 정보와, 세션에 있던 OAuthDto 일치한지 확인(고유 id값)
    5. DB 회원 탈퇴 적용(stats 값 stop으로 변경)
    6. 카카오 서버 회원 탈퇴 진행
    **/
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
    HttpSession session = request.getSession(false);

    // 1. 세션 데이터 여부 확인
    ResponseEntity<String> sessionConfirmResult = sessionConfirm(session, headers);
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;

    OAuthDto oAuthDto = (OAuthDto) session.getAttribute("oAuthDto");
    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("oAuthToken");

    System.out.println("카카오 회원 탈퇴 1단계 성공");

    try {
      // 2. OAuthToken 갱신
      oAuthToken = updateToken(oAuthToken);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));

      // 3. 회원 정보 얻기
      KakaoUserInfoResponseDto kakaoUserInfoResponseDto = kakaoOAuthLoginService.getKakaoUserByToken(oAuthToken);
      
      // 4. 얻은 회원 정보와, 세션에 있던 OAuthDto 일치한지 확인(고유 id값)
      if (!(oAuthDto.getId().equals(kakaoUserInfoResponseDto.getId() + ""))) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body("재로그인 후 탈퇴 이용 부탁드립니다.");
      }
      
      // 5. DB 회원 탈퇴 적용(stats 값 stop으로 변경)
      oAuthService.stopOAuthDtoByOAuthId(oAuthDto.getId());
      session.invalidate();
      
      // 6. 카카오 서버 회원 탈퇴 진행
      String deleteId = kakaoOAuthLoginService.executeKakaoUser("unlink",oAuthToken);
      System.out.println("카카오톡 서버 회원탈퇴 완료 deleteId = " + deleteId);
      headers.setLocation(URI.create("/"));
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body("회원 탈퇴 완료.");
      
    } catch (URISyntaxException | IOException | NotFoundException | SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body("현재 탈퇴가 불가능합니다. 잠시후 시도 부탁드립니다.");
    }
  }

  @GetMapping("/logout")
  public ResponseEntity<String> kakaoLogout(HttpServletRequest request,HttpSession session) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");

    try {
    // 1. 세션 데이터 여부 확인
    ResponseEntity<String> sessionConfirmResult = sessionConfirm(session, headers);
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;

    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("oAuthToken");
    kakaoOAuthLoginService.executeKakaoUser("logout",oAuthToken);
    session.invalidate();
    } catch (IOException e) {
      e.printStackTrace();
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).build();
    }
    return null;
  }
  
  @PostMapping("/register")
  public ResponseEntity<?> register(HttpSession session){
    
    ResponseEntity<?> sessionConfirmResult = sessionConfirm2(session);
    
    if (sessionConfirmResult.getStatusCode() != HttpStatus.OK)
      return sessionConfirmResult;
    
    OAuthDto oAuthDto = (OAuthDto)session.getAttribute("confirmOAuthDto");
    OAuthToken oAuthToken = (OAuthToken)session.getAttribute("confirmOAuthToken");
    
    try {
      int oAuthSeq = oAuthService.registerOAuth(oAuthDto);
      oAuthDto.setOauthSeq(oAuthSeq);
      session.setAttribute("oAuthDto", oAuthDto);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
      sessionDelete(session);
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
    
    UserDto userDto = (UserDto)session.getAttribute("confirmUserDto");
    OAuthDto oAuthDto = (OAuthDto)session.getAttribute("confirmOAuthDto");
    OAuthToken oAuthToken = (OAuthToken)session.getAttribute("confirmOAuthToken");
    
    try {
      int oAuthSeq = oAuthService.registerOAuth(oAuthDto, userDto.getUserSeq());
      oAuthDto.setOauthSeq(oAuthSeq);
      session.setAttribute("userDto", userDto);
      session.setAttribute("oAuthDto", oAuthDto);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
      sessionDelete(session);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // 세션 데이터 여부 확인
  private ResponseEntity<String> sessionConfirm(HttpSession session, HttpHeaders headers) {
    if (session == null) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("세션이 없습니다.");
    }
    OAuthDto oAuthDto = (OAuthDto) session.getAttribute("oAuthDto");
    if (oAuthDto == null) {
      headers.setLocation(URI.create("/logout"));
      return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body("인증정보가 없습니다.");
    }
    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("oAuthToken");
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

    UserDto userDto = (UserDto) session.getAttribute("confirmUserDto");
    OAuthDto oAuthDto = (OAuthDto) session.getAttribute("confirmOAuthDto");
    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("confirmOAuthToken");
    Long oAuthTokenExpiry = (Long) session.getAttribute("confirmOAuthTokenExpiry");

    if(userDto==null || oAuthDto==null || oAuthToken==null || oAuthTokenExpiry == null || System.currentTimeMillis() > oAuthTokenExpiry)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    
    return ResponseEntity.ok("성공");
  }
  
  // 컨펌세션 삭제
  private void sessionDelete(HttpSession session) {
    session.removeAttribute("confirmUserDto");
    session.removeAttribute("confirmOAuthDto");
    session.removeAttribute("confirmOAuthToken");
    session.removeAttribute("confirmOAuthTokenExpiry");
  }

  private Optional<OAuthDto> getOAuthDtoByoAuthid(String oAuthid) throws SQLException {
    try {
      OAuthDto oAuthDto = oAuthService.getOAuthByOAuthId("KAKAO", oAuthid);
      return Optional.of(oAuthDto);
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }

  private Optional<UserDto> getUserDtoByEmail(String email) throws SQLException {
    try {
      UserDto userDto = userServices.getUserByEmail(email);
      return Optional.of(userDto);
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }


  private OAuthToken updateToken(OAuthToken oAuthToken) throws URISyntaxException, IOException{
    
    //토큰 갱신
    String responseToken = kakaoOAuthLoginService.updateTokenUrl("token", "refresh_token", oAuthToken);
    
    //파싱
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(responseToken);
    String access_token = rootNode.get("access_token").asText();
    String id_token = rootNode.get("id_token").asText();
    
    //세터
    oAuthToken.setAccess_token(access_token);
    oAuthToken.setId_token(id_token);
    
    return oAuthToken;
  }


}
