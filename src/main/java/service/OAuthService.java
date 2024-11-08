package service;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dao.OAuthDao;
import dto.OAuthDto;
import exception.NotFoundException;

@Service
public class OAuthService {
  
  
  private OAuthDao oAuthDao;

  @Autowired
  public OAuthService(OAuthDao oAuthDao) {
    this.oAuthDao = oAuthDao;
  }

  //OAuth
  //1. 가입 및 개인 계정이 연결되어 있을때 OAuth 반환
  //2. 가입되어 있지 않을때 NotFoundException 반환
  //3. sql 구문 및 DB기타 오류 발생시 SQLException 반환
  public OAuthDto getOAuthByOAuthId(String provider,String oAuthid) throws SQLException, NotFoundException {
    return oAuthDao.getOAuthByOAuthId(provider, oAuthid).orElseThrow(()-> new NotFoundException("일치하는 회원이 없습니다."));
  }
  
  public OAuthDto getOAuthByOAuthSeq(int oAuthSeq) throws SQLException, NotFoundException {
    return oAuthDao.getOAuthByOAuthSeq(oAuthSeq).orElseThrow(()-> new NotFoundException("일치하는 회원이 없습니다."));
  }
  
  // 계정 등록
  @Transactional
  public int registerOAuth(OAuthDto oAuthDto) throws SQLException {
    int result = oAuthDao.registerOAuth(oAuthDto);
    if (result == 0) throw new SQLException("정상적으로 등록되었으나 시퀀스값이 없음.");
    return result;
  }
  
  // 계정 등록 userSeq 포함
  @Transactional
  public int registerOAuth(OAuthDto oAuthDto,int userSeq) throws SQLException {
    int result = oAuthDao.registerOAuth(oAuthDto,userSeq);
    if (result == 0) throw new SQLException("정상적으로 등록되었으나 시퀀스값이 없음.");
    return result;
  }
  
  // 데이터 완전 삭제
//  @Transactional
//  public void deleteOAuthDtoByOAuthId(String oAuthId) throws SQLException, NotFoundException {
//    int result = oAuthDao.deleteOAuthDtoByOAuthId(oAuthId);
//    if (result == 0) throw new NotFoundException();
//  }
  
  // 회원 탈퇴
  @Transactional
  public void stopOAuthDtoByOAuthId(String oAuthId) throws SQLException, NotFoundException {
    int result = oAuthDao.stopOAuthDtoByOAuthId(oAuthId);
    if (result == 0) throw new NotFoundException();
  }
  
  // 회원 복구
  @Transactional
  public void updateStatusByOAuthId(String oAuthId) throws SQLException, NotFoundException {
    int result = oAuthDao.updateStatusByOAuthId(oAuthId);
    if (result == 0) throw new NotFoundException();
  }
  
}
