package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import dto.User;
import dto.UserDto;
import exception.NotFoundException;

@Repository
public class UserDao {
  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public UserDao(DataSource dataSource,JdbcTemplate jdbcTemplate) {
    this.dataSource = dataSource;
    this.jdbcTemplate = jdbcTemplate;
  }
  
  public int registerUser(UserDto userDto,String encodePassword) throws SQLException {
    String sql =
        "INSERT INTO user (email, password, name, birth, phone, status, marketing_received_status, created_at) VALUES (?,?,?, ?, ?, ?, ?, ?)";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      pst.setString(1, userDto.getEmail());
      pst.setString(2, encodePassword);
      pst.setString(3, userDto.getName());
      pst.setString(4, userDto.getBirth());
      pst.setString(5, userDto.getPhone());
      pst.setString(6, "NORMAL");
      pst.setBoolean(7, userDto.isMarketingReceivedStatus());;
      pst.setTimestamp(8,Timestamp.valueOf(LocalDateTime.now()));
      pst.executeUpdate();
      ResultSet generatedKeys = pst.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }else
        throw new SQLException("서버 장애 발생.");
    }
  }

  public Optional<UserDto> getUserBySeq(int userSeq) throws SQLException {
    String sql = "SELECT * FROM user WHERE user_seq = ?";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setInt(1, userSeq);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
              UserDto userDto = new UserDto(
                  userSeq,
                  resultSet.getString("email"),
                  resultSet.getString("name"),
                  resultSet.getString("birth"),
                  resultSet.getString("phone"),
                  resultSet.getInt("address_seq"),
                  User.Status.valueOf(resultSet.getString("status")),
                  resultSet.getBoolean("marketing_received_status"),
                  resultSet.getTimestamp("created_at").toLocalDateTime(),
                  resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime() : null
              );
                return Optional.ofNullable(userDto);
            }
        }
    }
    return Optional.empty();
}

  public Optional<UserDto> getUserByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM user WHERE email = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, email);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          UserDto userDto = new UserDto(
              resultSet.getInt("user_seq"),
              resultSet.getString("email"),
              resultSet.getString("name"),
              resultSet.getString("birth"),
              resultSet.getString("phone"),
              resultSet.getInt("address_seq"),
              User.Status.valueOf(resultSet.getString("status")),
              resultSet.getBoolean("marketing_received_status"),
              resultSet.getTimestamp("created_at").toLocalDateTime(),
              resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime() : null
          );

          return Optional.ofNullable(userDto);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> getPasswordBySeq(int seq) throws SQLException {
    String sql = "SELECT password FROM user WHERE user_seq = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, seq);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(resultSet.getString("user_password"));
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> getPasswordByEmail(String email) throws SQLException {
    String sql = "SELECT password FROM user WHERE email = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, email);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String password = resultSet.getString("password");
          return Optional.of(password);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> getEmailByPhone(String phone) throws SQLException {
    String sql = "SELECT email FROM user WHERE phone = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, phone);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String email = resultSet.getString("email");
          return Optional.of(email);
        }
      }
    }
    return Optional.empty();
  }
  
  public List<UserDto> getAllUsers() throws SQLException {
    List<UserDto> users = new ArrayList<>();
    String sql = "SELECT * FROM user";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        UserDto userDto = new UserDto(
            resultSet.getInt("user_seq"),
            resultSet.getString("email"),
            resultSet.getString("name"),
            resultSet.getString("birth"),
            resultSet.getString("phone"),
            resultSet.getInt("address_seq"),
            User.Status.valueOf(resultSet.getString("status")),
            resultSet.getBoolean("marketing_received_status"),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime() : null
        );

        users.add(userDto);
      }
    }
    return users;
  }
  
  public String getUserStatusByEmail(String email) throws NotFoundException{
    try {
    String sql = "SELECT `status` FROM `user` WHERE `email` =?";
    return jdbcTemplate.queryForObject(sql, String.class, email);
    }catch (EmptyResultDataAccessException e) {
      throw new NotFoundException();
    }
  }
  
  public Optional<Integer> updateInfo(UserDto userDto) throws SQLException {
    String sql = "UPDATE user SET phone = ?, address_seq = ?, status = ?, marketing_received_status = ?, updated_at = ? WHERE user_seq = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, userDto.getPhone());
      pst.setInt(2, userDto.getAddressSeq());
      pst.setString(3, userDto.getStatus().name());
      pst.setBoolean(4, userDto.isMarketingReceivedStatus());
      pst.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(6, userDto.getUserSeq());
      Integer result = pst.executeUpdate();
      return Optional.of(result);
    }
  }
  
  public Optional<Integer> updatePassword(int userSeq,String newEncodePassword) throws SQLException {
    String sql = "UPDATE user SET password = ?, updated_at = ? WHERE user_seq = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, newEncodePassword);
      pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(3, userSeq);
      Integer result = pst.executeUpdate();
      return Optional.of(result);
    }
  }
  
  public int updateAddressSeq(int userSeq, int addressSeq) throws SQLException {
    String sql = "UPDATE user SET address_seq = ?, updated_at = ? WHERE user_seq = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, addressSeq);
      pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(3, userSeq);
      return pst.executeUpdate();
    }
  }
  
  public Optional<Integer> updateStatus(String email,String status) throws SQLException {
    String sql = "UPDATE `user` SET `status` = ?, updated_at = ? WHERE email = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, status);
      pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
      pst.setString(3, email);
      Integer result = pst.executeUpdate();
      if(result == 0) {
        return Optional.empty();
      }
      return Optional.of(result);
    }
  }

  public void stopUser(String email) throws SQLException {
    String sql = "update `user` set `status` = 'STOP' WHERE email = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, email);
      statement.executeUpdate();
    }
  }
  
  public boolean isEmailExists(String email){
    String sql = "SELECT COUNT(*) FROM `user` WHERE `email` = ?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, email);
    return count > 0;
  }
  
  public boolean isPhoneExists(String phone){
    String sql = "SELECT COUNT(*) FROM `user` WHERE `phone` = ?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, phone);
    return count > 0;
  }
  
  public boolean isEmailPhoneExists(String email, String phone) {
    String sql = "SELECT COUNT(*) FROM `user` WHERE `email` = ? AND `phone` = ?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, email, phone);
    return count > 0;
  }
  
  public Integer countLoginFail(String id) {
    String sql = "SELECT COUNT(*) FROM `login_fail_log` WHERE `id` = ? AND `success_seq` IS NULL;";
    return jdbcTemplate.queryForObject(sql, Integer.class, id);
  }
  
}
