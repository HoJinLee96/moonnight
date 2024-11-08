package api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.AddressDto;
import dto.OAuthDto;
import dto.RegisterUserDto;
import dto.User.Status;
import dto.UserDto;
import dtoNaverLogin.OAuthToken;
import exception.NotFoundException;
import exception.NotUpdateException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.AddressService;
import service.LoginLogService;
import service.OAuthService;
import service.UserService;
import util.HttpUtil;

@RestController
@RequestMapping("/user")
public class UserInfoController {

  private UserService userService;
  private OAuthService oAuthService;
  private LoginLogService loginLogService;
  private AddressService addressService;
  private HttpUtil httpUtil;

  @Autowired
  public UserInfoController(UserService userService,OAuthService oAuthService,LoginLogService loginLogService, AddressService addressService, HttpUtil httpUtil) {
    this.userService = userService;
    this.oAuthService = oAuthService;
    this.loginLogService = loginLogService;
    this.addressService = addressService;
    this.httpUtil = httpUtil;
  }
  
  @PostMapping("/login/email")
  public ResponseEntity<?> loginByEmail(
      @RequestParam("email") String reqEmail,
      @RequestParam("password") String reqPassword,
      HttpSession session, HttpServletRequest req) {
    
    System.out.println("LoginController.loginByEmail() 시작");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json; charset=UTF-8");
    String ip = HttpUtil.getClientIp(req);
    
    //계정 상태 얻기
    String status = "";
    try {
      status = userService.getUserStatusByEmail(reqEmail);
    } catch (NotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
    }
    
    //계정 상태 검사
      if ("STAY".equals(status)) {
        session.setAttribute("stayEmail", reqEmail);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      } else if ("STOP".equals(status)) {
        return ResponseEntity.status(HttpStatus.GONE).build();
      }
    
    // 계정 비밀번호 확인
    try {
      if (userService.comparePasswordByEmail(reqEmail, reqPassword, ip)) {
        UserDto userDto = userService.getUserByEmail(reqEmail);
        List<AddressDto> addressList = addressService.getSortedListByUserSeq(userDto);
        session.setAttribute("userDto", userDto);
        session.setAttribute("addressList", addressList);
        session.setAttribute("userDtoExpiry", System.currentTimeMillis() + (30 * 60 * 1000));

        // 이전 페이지의 도메인 확인
        String referer = (String) session.getAttribute("previousPageUrl");
        if (referer == null || !referer.startsWith(req.getScheme() + "://" + req.getServerName())
            || referer.contains("/login") || referer.contains("/join")) {
          referer = "/home";
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("redirectUrl", referer);

        return ResponseEntity.status(HttpStatus.OK).body(response);

      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
    }
  }
  
  // simple은 기존 로그인과 다르게 세션에 아무것도 저장 안함.
  @PostMapping("/login/email/simple")
  public ResponseEntity<?> loginByEmailSimple(
      @RequestParam("email") String reqEmail,
      @RequestParam("password") String reqPassword,
      HttpServletRequest req) {
    
    System.out.println("LoginController.loginByEmail() 시작");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json; charset=UTF-8");
    String ip = HttpUtil.getClientIp(req);
    
    //계정 상태 얻기
    String status = "";
    try {
      status = userService.getUserStatusByEmail(reqEmail);
    } catch (NotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
    }
    
    //계정 상태 검사
      if ("STAY".equals(status)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      } else if ("STOP".equals(status)) {
        return ResponseEntity.status(HttpStatus.GONE).build();
      }

    //계정 비밀번호 확인
    try {
      if (userService.comparePasswordByEmail(reqEmail, reqPassword, ip)) {
        return ResponseEntity.status(HttpStatus.OK).build();
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
    }
  }
  
  @PostMapping("/get/emailByPhone")
  public ResponseEntity<String> getEmailByPhone(@RequestParam("phone") String reqPhone) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
    
    try {
      String email = userService.getEmailByPhone(reqPhone);
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(email);

    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    }
  }    
  
  @PostMapping("/update/password")
  public ResponseEntity<?> updatePassword(
      @RequestParam("email") String reqEmail,
      @RequestParam("password") String reqPassword,
      @RequestParam("confirmPassword") String reqConfirmPassword) {
    
    //두 비밀번호 값 불일치
    if(!reqPassword.equals(reqConfirmPassword))
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    
      try {
        //user 테이블 데이터 get
        UserDto userDto = userService.getUserByEmail(reqEmail);
        
        //비밀번호 변경
        userService.updatePassword(userDto.getUserSeq(),reqPassword);
        
        //상태 변경
        userDto.setStatus(Status.NORMAL);
        userService.updateStatus(userDto);
        
        //로그인 실패 로그에 비밀번호 변경 했다고 기록, "-1" 로 비밀번호 변경을 의미
        loginLogService.failLogInit(reqEmail, -1);
        
        return ResponseEntity.status(HttpStatus.OK).build();
        
      } catch (NotFoundException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      } catch (SQLException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }
  
  @PostMapping("/update/info")
  public ResponseEntity<?> updateInfo(@RequestBody UserDto userDto,HttpSession session) {
    try {
      userService.updateInfo(userDto);
      List<AddressDto> list = addressService.getSortedListByUserSeq(userDto);
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      session.setAttribute("userDto", userDto);
      session.setAttribute("userJson", mapper.writeValueAsString(userDto));
      session.setAttribute("addressList", list);
      session.setAttribute("addressListJson", mapper.writeValueAsString(list));
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/update/addressSeq")
  public ResponseEntity<?> updateAddressSeq(@RequestBody Map<String,Integer> requestBody,HttpSession session){
    int userSeq = requestBody.get("userSeq");
    int addressSeq = requestBody.get("addressSeq");
    try {
      userService.updateAddressSeq(userSeq,addressSeq);
      
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      
      UserDto userDto = (UserDto)session.getAttribute("userDto");
      userDto.setAddressSeq(addressSeq);
      session.setAttribute("userDto", userDto);
      session.setAttribute("userJson", mapper.writeValueAsString(userDto));
      
      List<AddressDto> list = (List)session.getAttribute("addressList");
      
      AddressDto target = null;
      
      for (AddressDto content : list) {
          if (content.getAddressSeq() == addressSeq) {
              target = content;
              break;
          }
      }
      
      if (target != null) {
        list.remove(target);
        list.add(0, target);
      }else {
        throw new NotFoundException();
      }
      
      session.setAttribute("addressList", list);
      session.setAttribute("addressListJson", mapper.writeValueAsString(list));
      
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotUpdateException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/ouath/connect")
  public ResponseEntity<?> registerConnect(HttpSession session){
    
    UserDto userDto = (UserDto)session.getAttribute("confirmUserDto");
    if(userDto==null)
      userDto = (UserDto)session.getAttribute("userDto");
    
    OAuthDto oAuthDto = (OAuthDto)session.getAttribute("confirmOAuthDto");
      if(oAuthDto==null)
        oAuthDto = (OAuthDto)session.getAttribute("oAuthDto");
      
    OAuthToken oAuthToken = (OAuthToken)session.getAttribute("confirmOAuthToken");
    
    Long oAuthTokenExpiry = (Long) session.getAttribute("confirmOAuthTokenExpiry");
    
    if(userDto==null || oAuthDto==null || oAuthToken==null || oAuthTokenExpiry == null || System.currentTimeMillis() > oAuthTokenExpiry)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    
    try {
      int oAuthSeq = oAuthService.registerOAuth(oAuthDto, userDto.getUserSeq());
      oAuthDto.setOauthSeq(oAuthSeq);
      session.setAttribute("userDto", userDto);
      session.setAttribute("oAuthDto", oAuthDto);
      session.setAttribute("oAuthToken", oAuthToken);
      session.setAttribute("oAuthTokenExpiry", System.currentTimeMillis() + (Integer.parseInt(oAuthToken.getExpires_in()) * 1000));
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/exist/email")
  public ResponseEntity<String> isEmailExists(@RequestParam String reqEmail) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");

    try {
      if (userService.isEmailExists(reqEmail)) {
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
      } 
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    }
  }
  
  @PostMapping("/exist/phone")
  public ResponseEntity<?> isPhoneExists(@RequestParam String reqPhone) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
      
    try {
      if(userService.isPhoneExists(reqPhone)) {
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
      }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    }
  }
  
  @PostMapping("/exist/emailPhone")
  public ResponseEntity<?> isEmailPhoneExist(@RequestParam("email") String reqEmail,@RequestParam("phone") String reqPhone) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
    try {
      if(userService.isEmailPhoneExists(reqEmail, reqPhone)) {
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
    }
  }
  
  @PostMapping("/join/once")
  public ResponseEntity<String> public1(@RequestBody Map<String, Object> request, HttpServletRequest httpServletRequest) {
    
    ObjectMapper objectMapper = new ObjectMapper();
    RegisterUserDto registerUserDto = objectMapper.convertValue(request.get("registerUserDto"), RegisterUserDto.class);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
      
    HttpSession session = httpServletRequest.getSession();
    session.setAttribute("registerUserDto", registerUserDto);
    
    return ResponseEntity.status(HttpStatus.OK).headers(headers).body("계성 생성 완료.");

  }

  @PostMapping("/join/second")
  public ResponseEntity<String> public2(@RequestBody Map<String, Object> reqData, HttpServletRequest req) {
    ObjectMapper objectMapper = new ObjectMapper();
    UserDto userDto = objectMapper.convertValue(reqData.get("userDto"), UserDto.class);
    AddressDto addressDto = objectMapper.convertValue(reqData.get("addressDto"), AddressDto.class);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "text/plain; charset=UTF-8");
      
    try {
      userService.registerUser(userDto, addressDto);
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body("회원가입 완료.");
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body("가입이 불가능 합니다.");
      }
    }
  
  @PostMapping("/stop")
  public ResponseEntity<String> stopUser(@RequestParam("email") String reqEmail,@RequestParam("password") String reqPassword,HttpSession session, HttpServletRequest req){
    
    String ip = HttpUtil.getClientIp(req);
    
    try {
      
        userService.stopUser(reqEmail,reqPassword,ip);
        return ResponseEntity.status(HttpStatus.OK).build();
        
      }catch (SQLException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }catch(NotFoundException e){
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
    
  }
  
  private boolean verifyLoginUser(String email) throws SQLException,NotFoundException {
    if(!userService.isEmailExists(email)) {
      throw new NotFoundException();
    }
    if(userService.countLoginFail(email)>4)
      return false;
    else
      return true;
  }
  
}
