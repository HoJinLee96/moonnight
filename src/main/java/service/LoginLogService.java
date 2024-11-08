package service;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dao.LoginLogDao;
import dto.User;

@Service
public class LoginLogService {

  LoginLogDao loginLogDao;
  
  @Autowired
  public LoginLogService(LoginLogDao loginLogDao) {
    this.loginLogDao = loginLogDao;
  }
  
  public void loginSuccess(User user,String ip) throws SQLException {
    loginLogDao.loginSuccess(user, ip);
  }
  
  public int loginFail(User user, String ip, String reason) throws SQLException {
    return loginLogDao.loginFail(user, ip, reason);
  }
  
  public void failLogInit(String id, int reason) throws SQLException {
    loginLogDao.failLogInit(id,reason);
  }
  
}
