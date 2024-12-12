package intercepter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import dto.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class MasterAuthInterceptor implements HandlerInterceptor{
  
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
      throws Exception {
    HttpSession session = req.getSession();
    User user = (User)session.getAttribute("masterDto");
    if(user==null) {
      res.sendRedirect("/master/login");
      return false;
    }
    return true;
    
  }

}
