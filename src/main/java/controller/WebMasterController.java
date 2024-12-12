package controller;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import dto.UserDto;
import exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;
import util.HttpUtil;

@Controller
@RequestMapping("/master")
public class WebMasterController {
  
  @Autowired
  private UserService userService;
  
  @GetMapping("/login")
  public String showMasterLogin(HttpServletRequest req, HttpServletResponse ress) {
    return "masterLogin";
  }
  
  @GetMapping("/home")
  public String showMasterHome(HttpServletRequest req, HttpServletResponse ress) {
    return "masterHome";
  }
  
  @GetMapping("/estimateView")
  public String showEstimateView(HttpServletRequest req, HttpServletResponse res) {
    return "masterEstimateView";
  }
  
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam("email") String reqEmail,
      @RequestParam("password") String reqPassword,
      HttpSession session, HttpServletRequest req){
    
    String ip = HttpUtil.getClientIp(req);
    
    try {
      if (userService.comparePasswordByEmail(reqEmail, reqPassword, ip)) {
        UserDto userDto = userService.getUserByEmail(reqEmail);
        if("MASTER".equals(userDto.getStatus().name())) {
          session.setAttribute("masterDto", userDto);
          return ResponseEntity.status(HttpStatus.OK).build();
        }
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

  }

}
