package auth.sign.log;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import auth.sign.log.LoginLog.LoginResult;
import domain.user.User.UserProvider;
import jakarta.transaction.Transactional;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Integer>{
  

  @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.email = :email AND l.userProvider = :userProvider AND l.resolveBy IS NULL AND l.loginResult NOT IN :excludedResults")
  int countUnresolvedFailed(@Param("userProvider") UserProvider userProvider, 
                            @Param("email") String email, 
                            @Param("excludedResults") List<LoginResult> excludedResults);

  @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.email = :email AND l.userProvider = :userProvider AND l.resolveBy IS NULL AND l.loginResult IN :includedResults")
  int countUnresolvedWithResults(@Param("userProvider") UserProvider userProvider, 
                                 @Param("email") String email, 
                                 @Param("includedResults") List<LoginResult> includedResults);
  
  @Transactional @Modifying
  @Query("UPDATE LoginLog l SET l.resolveBy = :loginLogSeq WHERE l.userProvider = :userProvider AND l.email = :email AND l.resolveBy IS NULL")
  int resolveUnresolvedLogs(
      @Param("userProvider") UserProvider userProvider,
      @Param("email") String email,
      @Param("loginLogSeq") int loginLogSeq
  );
  
}
