package deprecated;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dto.request.UserCreateRequestDto.Provider;
import entity.OAuth;

@Deprecated
@Service
public class OAuthUserService {
  
  
  private OAuthDao oAuthDao;

  @Autowired
  public OAuthUserService(OAuthDao oAuthDao) {
    this.oAuthDao = oAuthDao;
  }

  public OAuth getOAuthByOAuthId(Provider provider,String oAuthId) throws SQLException, NoSuchElementException {
    return oAuthDao.getOAuthByOAuthId(provider, oAuthId).orElseThrow(()-> new NoSuchElementException("일치하는 회원이 없습니다."));
  }
  
  public OAuth getOAuthByOAuthSeq(int oAuthSeq) throws SQLException, NotFoundException {
    return oAuthDao.getOAuthByOAuthSeq(oAuthSeq).orElseThrow(()-> new IllegalArgumentException("일치하는 회원이 없습니다."));
  }
  
  // 계정 등록
  @Transactional
  public int registerOAuth(OAuth oAuthDto) throws SQLException {
    int result = oAuthDao.registerOAuth(oAuthDto);
    if (result == 0) throw new SQLException("정상적으로 등록되었으나 시퀀스값이 없음.");
    return result;
  }
  
  // 계정 등록 userSeq 포함
  @Transactional
  public int registerOAuth(OAuth oAuthDto,int userSeq) throws SQLException {
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
  public void stopOAuthDtoByOAuthId(String oAuthId) throws SQLException {
    int result = oAuthDao.stopOAuthDtoByOAuthId(oAuthId);
    if (result == 0) throw new NotFoundException();
  }
  
  // 회원 복구
  @Transactional
  public void updateStatusByOAuthId(String oAuthId) throws SQLException {
    int result = oAuthDao.updateStatusByOAuthId(oAuthId);
    if (result == 0) throw new NotFoundException();
  }
  
}
