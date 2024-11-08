package service;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dao.VerificationDao;
import dto.VerifyResponseDto;

@Service
public class VerificationServices {
  
  VerificationDao verificationDao;
  
  @Autowired
  VerificationServices(VerificationDao verificationDao){
    this.verificationDao = verificationDao;
  }
  
  public boolean compareCode(VerifyResponseDto verifyResponseDto,String reqCode) throws SQLException{
    String verificationCode = verificationDao.getVerificationCode(verifyResponseDto.getVerificationSeq()).orElseThrow(()->new SQLException("현재 이용할 수 없습니다."));
    return verificationCode.equals(reqCode);
  }
  
  public VerifyResponseDto register(VerifyResponseDto responseDto) throws SQLException {
    return verificationDao.register(responseDto);
  }
}
