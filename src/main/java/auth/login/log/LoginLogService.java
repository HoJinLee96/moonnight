package auth.login.log;

import java.util.List;
import org.springframework.stereotype.Service;
import auth.login.log.LoginLog.LoginResult;
import domain.user.User.UserProvider;
import global.exception.TooManyLoginFailuresException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginLogService {
  
  LoginLogRepository loginLogRepository;
  
  @Transactional
  public void registerLoginLog(UserProvider userProvider, String email, String ip, LoginResult result) {
    loginLogRepository.save(LoginLog.builder()
        .userProvider(userProvider)
        .email(email)
        .requestIp(ip)
        .loginResult(result)
        .build());
  }
  
  // ======= 로그인 실패 횟수 검사 =======
  public void countloginFail(UserProvider userProvider, String email) {
    
    int loginFailCount = loginLogRepository.countUnresolvedWithResults(userProvider, email, List.of(LoginResult.INVALID_EMAIL));
    if (loginFailCount >= 10) {
      throw new TooManyLoginFailuresException("로그인 실패 10회 이상하였습니다. 인증을 진행 하세요.");
    }
  }
  
  @Transactional
  public void loginFailLogResolveByUpdatePassword(UserProvider userProvider, String email, String ip) {
    LoginLog loginLog = LoginLog.builder()
        .userProvider(userProvider)
        .email(email)
        .requestIp(ip)
        .loginResult(LoginResult.UPDATE_PASSWORD)
        .build();
    loginLogRepository.save(loginLog);
    loginLogRepository.resolveUnresolvedLogs(userProvider, email, loginLog.getLoginLogSeq());
  }
  
//  @Autowired
//  LoginSuccessLogDao loginSuccessLogDao;
//  LoginFailLogDao loginFailLogDao;
//  
//  @Transactional
//  public int loginSuccess(LoginLog loginResultDto) {
//    return loginSuccessLogDao.registerLoginSuccess(loginResultDto);
//  }
//  
//  @Transactional
//  public void loginFail(LoginLog loginResultDto) {
//    loginFailLogDao.registerLoginFail(loginResultDto);
//  }
//  
//  @Transactional
//  public void loginFailLogResolveByLoginResultDto(LoginLog loginResultDto) {
//    loginFailLogDao.updateResolveByLoginResultDto(loginResultDto);
//  }
//  
//  @Transactional
//  public void loginFailLogResolveByUpdatePassword(Provider provider, String email, String ip) {
//    LoginLog loginResultDto = new LoginLog();
//    loginResultDto.setProvider(provider);
//    loginResultDto.setId(email);
//    loginResultDto.setIp(ip);
//    loginResultDto.setReason(Reason.UPDATE_PASSWORD);
//    int loginSuccessLogSeq = loginSuccessLogDao.registerLoginSuccess(loginResultDto);
//    loginResultDto.setLoginResultSeq(loginSuccessLogSeq);
//    loginFailLogDao.updateResolveByLoginResultDto(loginResultDto);
//  }
  
}
