package intercepter;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.AddressDto;
import dto.OAuthDto;
import dto.UserDto;
import dtoNaverLogin.OAuthToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginAuthInterceptor implements HandlerInterceptor {
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object Handler)throws Exception {
    System.out.println("LoginAuthInterceptor.preHandle() 실행");
    
    HttpSession session = request.getSession();
    String uri = request.getRequestURI();

    // /login 요청시
    if ("/login".equals(uri)) {
      if (isUserDtoExist(session) || isOAuthDtoExist(session)) {
        response.sendRedirect("/home");
        return false;
      }
      return true;
    }

    // /my 요청시
    if (uri.startsWith("/my")) {
      if (!isUserDtoExist(session) && !isOAuthDtoExist(session)) {
        session.invalidate();
        response.sendRedirect("/login");
        return false;
      }
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
      
      if(session.getAttribute("userJson")==null) {
        UserDto userDto = (UserDto) session.getAttribute("userDto");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
          String json = mapper.writeValueAsString(userDto);
          session.setAttribute("userJson", json);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
          session.invalidate();
        }
      }
      
      if(session.getAttribute("addressListJson")==null) {
        List<AddressDto> addressList = (List<AddressDto>) session.getAttribute("addressList");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
          String json = mapper.writeValueAsString(addressList);
          session.setAttribute("addressListJson", json);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
          session.invalidate();
        }
      }

      return true;
    }
    
    
    return true;
  }
  
  
  
  private boolean isUserDtoExist(HttpSession session) {
    UserDto userDto = (UserDto) session.getAttribute("userDto");
    Long userDtoExpiry = (Long) session.getAttribute("userDtoExpiry");
    if(userDto == null || userDtoExpiry == null) {
      return false;
    }
    if((userDtoExpiry - System.currentTimeMillis()) <= 0){
      session.removeAttribute("userDto");
      session.removeAttribute("userDtoExpiry");
      return false;
    }

    return true;
  }
  
  private boolean isOAuthDtoExist(HttpSession session) {
    OAuthDto oAuthDto = (OAuthDto) session.getAttribute("oAuthDto");
    OAuthToken oAuthToken = (OAuthToken) session.getAttribute("oAuthToken");
    Long oAuthTokenExpiry = (Long) session.getAttribute("oAuthTokenExpiry");
    if(oAuthDto == null || oAuthToken == null || oAuthTokenExpiry == null) {
      return false;
    }
    if((oAuthTokenExpiry - System.currentTimeMillis()) <= 0){
      session.removeAttribute("oAuthDto");
      session.removeAttribute("oAuthToken");
      session.removeAttribute("oAuthTokenExpiry");
      return false;
    }

    return true;
  }

}
