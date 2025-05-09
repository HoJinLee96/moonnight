package web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class WebMainController {
  

  @GetMapping("/swagger-ui/")
  public String redirectToSwagger() {
      return "forward:/swagger-ui/index.html";
  }
  
  @GetMapping({"/", "/home"})
  public String showHome(HttpServletRequest req, HttpServletResponse res) {
    System.out.println("----------WebMainController.showHome() 실행----------");
    return "home";
  }


  @GetMapping("/signin")
  public String showLogin(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
    System.out.println("----------WebMainController.showLogin() 실행----------");
    // 이전 페이지의 도메인 확인
   String referer = req.getHeader("Referer");
   if (referer != null && referer.startsWith(req.getScheme() + "://" + req.getServerName()) && !referer.contains("/signin") && !referer.contains("/join")) {
     session.setAttribute("previousPageUrl", referer);
   }
    return "signin/signin";
  }
  
  @GetMapping("/signinBlank")
  public String showLoginBlank(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
    System.out.println("----------WebMainController.showLoginBlank() 실행----------");
    return "signin/signinBlank";
  }
  
  @GetMapping("/oauth-redirect")
  public String oauthRedirect() {
      System.out.println("---------- WebMainController.oauthRedirect() 실행 ----------");
      return "signin/oauthRedirect";
  }
  
  @GetMapping("/error")
  public String showError() {
      System.out.println("---------- WebMainController.oauthRedirect() 실행 ----------");
      return "error";
  }
  
//  @GetMapping("/clearLogin")
//  public String showClearLogin(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
//    System.out.println("----------WebMainController.showClearLogin() 실행----------");
//    
//    // 기존 세션이 존재하면 무효화
//    if (session != null) {
//        session.invalidate();  // 기존 세션 무효화
//        System.out.println("기존 세션 무효화");
//    }
//
//    return "signin";
//  }
  
//  @GetMapping("/logout")
//  public String logout(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
//    System.out.println("----------WebMainController.logout() 실행----------");
//    // 세션에서 사용자 정보를 제거하여 로그아웃 처리
//
//    if (session != null) {
//      session.removeAttribute("userDto");
//      session.removeAttribute("userJson");
//      session.removeAttribute("userDtoExpiry");
//      session.removeAttribute("addressList");
//      session.removeAttribute("addressListJson");
//      session.removeAttribute("oAuthDto");
//      session.removeAttribute("oAuthToken");
//      session.removeAttribute("oAuthTokenExpiry");
//    }
//    return "redirect:/home";
//  }

  @GetMapping("/my")
  public String showMy(HttpSession session) {
    System.out.println("----------WebMainController.showMy() 실행----------");
    return "my/my";
  }

  @GetMapping("/signup1")
  public String showJoin(
      HttpServletRequest req,
      HttpServletResponse res) {
    System.out.println("----------WebMainController.showJoin() 실행----------");

    return "signup/signup1";
  }

   @GetMapping("/signup2")
   public String showJoin2(
       @RequestHeader(required = false, value = "User-Agent") String userAgent,
       @CookieValue(required = false, value = "X-Access-SignUp-Token") String webAccessSignUpToken,
       HttpServletRequest req,
       HttpServletResponse res) {
   System.out.println("----------WebMainController.joinDetail() 실행----------");
     boolean isMobileApp = userAgent != null && userAgent.contains("MyMobileApp");
     if(!isMobileApp) {
       if(webAccessSignUpToken==null || webAccessSignUpToken.isEmpty()) {
         return "signup/signup1";
       }
     }
   return "signup/signup2";
   }
   
   @GetMapping("/estimate")
   public String showEstimate(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.showEstimate() 실행----------");
     return "estimate/estimate";
   }
   
   @GetMapping("/review")
   public String showReview(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.showReview() 실행----------");
     return "review";
   }
   
   @GetMapping("/join/sns/confirm")
   public String showJoinSnsConfirm(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.showJoinSnsConfirm() 실행----------");
     return "joinSnsConfirm";
   }
   
   @GetMapping("/find/email")
   public String showFindEmail(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.showFindEmail() 실행----------");
     return "findEmailBlank";
   }
   
   @GetMapping("/find/password")
   public String showFindPassword(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.showFindId() 실행----------");
     return "findPasswordBlank";
   }
   
   @GetMapping("/verifyUser")
   public String showLoginVerify(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.loginVerify() 실행----------");
     res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
     res.setHeader("Pragma", "no-cache");
     res.setHeader("Expires", "0");
     return "verifyUser";
   }

   @GetMapping("/update/password/blank")
   public String showUpdatePasswordBlank(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.verifyPhoneBlank() 실행----------");
     return "updatePasswordBlank";
   }
   
   @GetMapping("/verify/phone/blank")
   public String showUpdatePhoneBlank(HttpServletRequest req, HttpServletResponse res) {
     System.out.println("----------WebMainController.verifyPhoneBlank() 실행----------");
     return "verifyPhoneBlank";
   }
   
   @GetMapping("/my/loginInfo")
   public String showMyLoginInfo(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showMyLoginInfo() 실행----------");
     return "myLoginInfo";
   }
   
   @GetMapping("/my/withdrawal")
   public String showWithdrawal(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showWithdrawal() 실행----------");
     return "withdrawal";
   }
   @GetMapping("/my/withdrawalOAuth")
   public String showWithdrawalOAuth(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showWithdrawalOAuth() 실행----------");
     return "withdrawalOAuth";
   }
   @GetMapping("/my/addressBook")
   public String showMyAddressBook(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showMyAddress() 실행----------");
     return "myAddressBook";
   }
   @GetMapping("/my/addressBook/Blank")
   public String showMyAddressBookBlank(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showMyAddress() 실행----------");
     return "myAddressBookBlank";
   }
   @GetMapping("/my/profile")
   public String showMyProfile(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showMyProfile() 실행----------");
     return "myProfile";
   }
   @GetMapping("/my/estimate")
   public String showMyEstimate(HttpServletRequest req, HttpServletResponse res,HttpSession session) {
     System.out.println("----------WebMainController.showMyProfile() 실행----------");
     return "myEstimate";
   }
   
}
