package deprecated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import entity.LoginLog;
import entity.LoginLog.Provider;
import entity.LoginLog.Reason;

@Repository
public class LoginSuccessLogDao extends BaseDao<LoginLog>{

  @Autowired
  public LoginSuccessLogDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }
  
  public int registerLoginSuccess(LoginLog loginResultDto) {
    String sql = "INSERT INTO `login_success_log` (provider,id,ip,reason) VALUES (?,?,?,?)";
    return register(sql, loginResultDto.getProvider().name(), loginResultDto.getId(), loginResultDto.getIp(), loginResultDto.getReason().name());
  }
  
  public LoginLog getLoginSuccessLogByLoginSuccessSeq(int loginSuccessSeq)  {
    return getDtoBySequence(loginSuccessSeq);
  }
  
  public List<LoginLog> getLoginSuccessLogById(String id)  {
    String sql = "SELECT * FROM `login_success_log` WHERE `id` = ? ORDER BY `created_at` DESC";
    return jdbcTemplate.query(sql, new LoginResultDtoRowMapper(), id);
  }
  
  private static class LoginResultDtoRowMapper implements RowMapper<LoginLog> {
    @Override
    public LoginLog mapRow(ResultSet rs, int rowNum) throws SQLException  {
      
      return new LoginLog(
          rs.getInt("login_success_seq"),
          Provider.valueOf(rs.getString("provider")),
          rs.getString("id"),
          rs.getString("ip"),
          Reason.valueOf(rs.getString("reason")),
          rs.getTimestamp("created_at").toLocalDateTime()
          );
    }
  }
    
  @Override
  protected RowMapper<LoginLog> getRowMapper() {
    return new LoginResultDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "login_success_log";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "login_success_seq";
  }
  
}
