package deprecated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import entity.LoginLog;
import entity.LoginLog.Provider;
import entity.LoginLog.Reason;

public class LoginFailLogDao extends BaseDao<LoginLog> {

  @Autowired
  public LoginFailLogDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerLoginFail(LoginLog loginResultDto) {
    String sql =
        "INSERT INTO `login_fail_log` (provider,id,ip,reason,resolve_by) VALUES (?,?,?,?,?)";
    return register(sql,
        loginResultDto.getProvider().name(),
        loginResultDto.getId(),
        loginResultDto.getIp(),
        loginResultDto.getReason().name(),
        loginResultDto.getResolvedBy() != null ? loginResultDto.getResolvedBy() : null
        );
  }
  
  public LoginLog getLoginFailLogByLoginFailSeq(int loginFailSeq)  {
    return getDtoBySequence(loginFailSeq);
  }
  
  public List<LoginLog> getLoginFailLogListById(String id)  {
    String sql = "SELECT * FROM `login_fail_log` WHERE `id` = ?";
    return jdbcTemplate.query(sql, new LoginResultDtoRowMapper(), id);
  }
  
  public void updateResolveByLoginResultDto(LoginLog successLoginResultDto) {
    String sql =
        "UPDATE `login_fail_log` SET `resolve_by`=  ?, `updated_at` = NOW() WHERE `id` = ? AND `resolve_by` IS NULL";
    jdbcTemplate.update(sql, successLoginResultDto.getLoginResultSeq(),successLoginResultDto.getId());
  }

  public int countLoginFailByEmail(String email) {
    String sql = "SELECT COUNT(*) FROM `login_fail_log` WHERE `id` = ? AND `resolve_by` IS NULL;";
    return jdbcTemplate.queryForObject(sql, Integer.class, email);
  }

  public int countLoginFailByIp(String ip) {
    String sql = "SELECT COUNT(*) FROM `login_fail_log` WHERE `ip` = ? AND `resolve_by` IS NULL;";
    return jdbcTemplate.queryForObject(sql, Integer.class, ip);
  }

  private static class LoginResultDtoRowMapper implements RowMapper<LoginLog> {
    @Override
    public LoginLog mapRow(ResultSet rs, int rowNum) throws SQLException {
      Timestamp updatedTs = rs.getTimestamp("updated_at");
      LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;

      return new LoginLog(
          rs.getInt("login_fail_seq"),
          Provider.valueOf(rs.getString("provider")),
          rs.getString("id"),
          rs.getString("ip"),
          Reason.valueOf(rs.getString("reason")),
          rs.getObject("resolve_by",Integer.class),
          rs.getTimestamp("created_at").toLocalDateTime(),
          updatedAt);
    }
  }

  @Override
  protected RowMapper<LoginLog> getRowMapper() {
    return new LoginResultDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "login_fail_log";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "login_fail_seq";
  }



}
