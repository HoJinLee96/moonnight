package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import dto.User;

@Repository
public class LoginLogDao {

  private final DataSource dataSource;

  @Autowired
  public LoginLogDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  public void loginSuccess(User user,String ip) throws SQLException {

    String sql = "insert into login_success_log (provider,id,ip,create_at) values (?,?,?,?)";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      pst.setString(1, user.getProvider());
      pst.setString(2, user.getEmail());
      pst.setString(3, ip);
      pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
      pst.executeUpdate();
      ResultSet generatedKeys = pst.getGeneratedKeys();
      if (generatedKeys.next()) {
        sql = "update `login_fail_log` set `success_seq` = ? where `id` = ? AND `success_seq` is null";
        int loginSuccessSeq = generatedKeys.getInt(1);
        try(PreparedStatement updatePst = con.prepareStatement(sql);){
          updatePst.setInt(1, loginSuccessSeq);
          updatePst.setString(2, user.getEmail());
          updatePst.executeUpdate();
        }
      }
      }
    }
  
  public int loginFail(User user, String ip, String reason) throws SQLException {
    String sql = "insert into login_fail_log (provider,id,ip,reason,create_at) values (?,?,?,?,?)";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      pst.setString(1, user.getProvider());
      pst.setString(2, user.getEmail());
      pst.setString(3, ip);
      pst.setString(4, reason);
      pst.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
      pst.executeUpdate();
      ResultSet generatedKeys = pst.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }else
        throw new SQLException("서버 장애 발생.");
      }
  }
  
  public void failLogInit(String id, int reason) throws SQLException {
    String sql = "update `login_fail_log` set `success_seq`=  ? where `id` = ? AND `success_seq` is null";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      pst.setInt(1, reason);
      pst.setString(2, id);
      pst.executeUpdate();
      }
  }
  
}
