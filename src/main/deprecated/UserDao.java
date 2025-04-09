package deprecated;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import entity.Verification;

@Repository
public class UserDao extends BaseDao<Verification>{

  @Autowired
  protected UserDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerNormalUser(UserDto userDto,String encodePassword)  {
    String sql = "INSERT INTO `user` (provider, email, password, name, birth, phone, marketing_received_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
    return register(sql, "NORMAL", userDto.getEmail(),encodePassword,userDto.getName(),userDto.getBirth(),userDto.getPhone(), userDto.isMarketingReceivedStatus());
  }
  
//  public UserDto getUserDtoByUserSeq(int userSeq)  {
//    return getDtoBySequence(userSeq);
//  }
//  
//  public List<UserDto> getAllUser()  {
//    return getAllDto();
//  }

  public Optional<UserDto> getUserDtoByEmail(String email){
    String sql = "SELECT * FROM `user` WHERE `email` = ?";
    return jdbcTemplate.query(sql, new UserDtoRowMapper(), email).stream().findFirst();
  }
  
  public Optional<UserDto> getUserDtoByPhone(String phone){
    String sql = "SELECT * FROM `user` WHERE `phone` = ?";
    return jdbcTemplate.query(sql, new UserDtoRowMapper(), phone).stream().findFirst();
  }
  
  public String getPasswordByUserSeq(int userSeq)  {
    String sql = "SELECT `password` FROM `user` WHERE `user_seq` = ?";
    return jdbcTemplate.queryForObject(sql, String.class, userSeq);
  }

  public Optional<String> getPasswordByEmail(String email)  {
    String sql = "SELECT `password` FROM `user` WHERE `email` = ? AND `provider` = 'NORMAL'";
    return jdbcTemplate.queryForList(sql,String.class, email).stream().findFirst();
  }

  public Optional<String> getEmailByPhone(String phone)  {
    String sql = "SELECT `email` FROM `user` WHERE `phone` = ? AND `provider` = 'NORMAL'";
    return jdbcTemplate.queryForList(sql,String.class, phone).stream().findFirst();
  }
  
//  public Optional<Status> getUserStatusByEmail(String email)  {
//    String sql = "SELECT `status` FROM `user` WHERE `email` = ?";
//    return getSingleField(sql, Status.class, email);
//  }
  
  public Optional<UserDto> getLoginInfoByEmail(String email)  {
    String sql = "SELECT `password`, `status` FROM `user` WHERE `email` = ?";
    return jdbcTemplate.query(sql, new LoginInfoRowMapper(), email).stream().findFirst();
  }
  
  // phone, status, marketing_received_status
  public void updateInfo(UserDto userDto) {
    String sql = "UPDATE `user` SET `phone` = ?, `status` = ?, `marketing_received_status` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    update(sql, userDto.getPhone(), userDto.getStatus().name(), userDto.isMarketingReceivedStatus(), userDto.getUserSeq());
  }
  
  public void updatePhone(int userSeq, String phone)  {
    String sql = "UPDATE `user` SET `phone` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    update(sql, phone, userSeq);
  }
  
  public void updatePassword(int userSeq, String encodePassword)  {
    String sql = "UPDATE `user` SET `password` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    update(sql, encodePassword, userSeq);
  }
  
  public void updateStatus(int userSeq, Status status)  {
    String sql = "UPDATE `user` SET `status` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    update(sql, status.name(), userSeq);
  }
  
  public boolean isEmailExists(String email){
    String sql = "SELECT EXISTS (SELECT 1 FROM `user` WHERE `email` = ?)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, email);
  }
  
  public boolean isPhoneExists(String phone){
    String sql = "SELECT EXISTS (SELECT 1 FROM `user` WHERE `phone` = ?)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, phone);
  }
  
  public boolean isEmailPhoneExists(String email, String phone) {
    String sql = "SELECT EXISTS (SELECT 1 FROM `user` WHERE `email` = ? AND `phone` = ?)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, email, phone);
  }
  
  public boolean isActive(int userSeq) {
    String sql = "SELECT EXISTS (SELECT 1 FROM `user` WHERE `user_seq` = ? AND `status` = 'ACTIVE')";
    return jdbcTemplate.queryForObject(sql, Boolean.class, userSeq);
  }
  
  private static class UserDtoRowMapper implements RowMapper<UserDto> {
    @Override
    public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException  {
      Timestamp updatedTs = rs.getTimestamp("updated_at");
      LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
      
      return new UserDto(
          rs.getInt("user_seq"),
          Provider.valueOf(rs.getString("provider")),
          rs.getString("email"),
          rs.getString("name"),
          rs.getString("birth"),
          rs.getString("phone"),
          Status.valueOf(rs.getString("status")),
          rs.getBoolean("marketing_received_status"),
          rs.getTimestamp("created_at").toLocalDateTime(),
          updatedAt
          );
    }
  }
  
  private static class LoginInfoRowMapper implements RowMapper<UserDto> {
    @Override
    public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new UserDto(
          rs.getInt("user_seq"),
          rs.getString("email"),
          rs.getString("password"),
          Status.valueOf(rs.getString("status"))
          );
    }
  }
  
  @Override
  protected RowMapper<UserDto> getRowMapper() {
    return new UserDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "user";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "user_seq";
  }


  public int registerUser(RegisterUserDto userDto,String encodePassword) throws SQLException {
    String sql =
        "INSERT INTO `user` (email, password, name, birth, phone, status, marketing_received_status) VALUES (?,?,?, ?, ?, ?, ?)";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      pst.setString(1, userDto.getEmail());
      pst.setString(2, encodePassword);
      pst.setString(3, userDto.getName());
      pst.setString(4, userDto.getBirth());
      pst.setString(5, userDto.getPhone());
      pst.setString(6, "NORMAL");
      pst.setBoolean(7, userDto.isMarketingReceivedStatus());;
      pst.executeUpdate();
      ResultSet generatedKeys = pst.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }
    }
    return 0;
  }
  
  public Optional<UserDto> getUserBySeq(int userSeq) throws SQLException, IllegalArgumentException {
    String sql = "SELECT * FROM `user` WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, userSeq);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapUserDto(rs));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<UserDto> getUserByEmail(String email) throws SQLException, IllegalArgumentException {
    String sql = "SELECT * FROM `user` WHERE `email` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, email);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapUserDto(rs));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<String> getPasswordBySeq(int seq) throws SQLException {
    String sql = "SELECT `password` FROM `user` WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, seq);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getString("password"));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<String> getPasswordByEmail(String email) throws SQLException {
    String sql = "SELECT `password` FROM `user` WHERE `email` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, email);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getString("password"));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<String> getEmailByPhone(String phone) throws SQLException {
    String sql = "SELECT `email` FROM user WHERE phone = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, phone);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getString("email"));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<Status> getUserStatusByEmail(String email) throws SQLException {
    String sql = "SELECT `status` FROM `user` WHERE `email` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setString(1, email);
      try(ResultSet rs = pst.executeQuery()){
        if(rs.next()) {
          String status = rs.getString("status");
          return Optional.of(Status.valueOf(status));
        }
      }
    }
    return Optional.empty();
  }
  
  public Optional<UserDto> getLoginInfoByEmail(String email) throws SQLException {
    String sql = "SELECT `password`, `status` FROM `user` WHERE `email` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setString(1, email);
      try(ResultSet rs = pst.executeQuery()){
        if(rs.next()) {
          UserDto userDto = new UserDto(
              email,
              rs.getString("password"),
              Status.valueOf(rs.getString("status"))
              ); 
          return Optional.of(userDto);
        }
      }
    }
    return Optional.empty();
  }
  
  public List<UserDto> getAllUsers() throws SQLException {
    String sql = "SELECT * FROM `user`";
    List<UserDto> list = new ArrayList<>();
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) {
        list.add(mapUserDto(rs));
      }
    }
    return list;
  }
  
  public int updateInfo(UserDto userDto) throws SQLException {
    String sql = "UPDATE `user` SET `phone` = ?, `address_seq` = ?, `status` = ?, `marketing_received_status` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, userDto.getPhone());
      pst.setInt(2, userDto.getAddressSeq());
      pst.setString(3, userDto.getStatus().name());
      pst.setBoolean(4, userDto.isMarketingReceivedStatus());
      pst.setInt(5, userDto.getSequence());
      return pst.executeUpdate();
    }
  }
  
  public int updatePassword(int userSeq, String encodePassword) throws SQLException {
    String sql = "UPDATE `user` SET `password` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, encodePassword);
      pst.setInt(2, userSeq);
      return pst.executeUpdate();
    }
  }
  
  public int updateAddressSeq(int userSeq, int addressSeq) throws SQLException {
    String sql = "UPDATE `user` SET `address_seq` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, addressSeq);
      pst.setInt(2, userSeq);
      return pst.executeUpdate();
    }
  }
  
  public int updateStatusByUserSeq(int userSeq, Status status) throws SQLException {
    String sql = "UPDATE `user` SET `status` = ?, `updated_at` = NOW() WHERE `user_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, status.name());
      pst.setInt(2, userSeq);
      return pst.executeUpdate();
    }
  }
  
  public int updateStatusByEmail(String email, Status status) throws SQLException {
    String sql = "UPDATE `user` SET `status` = ?, `updated_at` = NOW() WHERE `email` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, status.name());
      pst.setString(2, email);
      return pst.executeUpdate();
    }
  }

  public int stopUser(String email) throws SQLException {
    String sql = "update `user` set `status` = 'STOP' , updated_at = ? WHERE email = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pst = connection.prepareStatement(sql)) {
      pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
      pst.setString(2, email);
      return pst.executeUpdate();
    }
  }
  
  private UserDto mapUserDto(ResultSet rs) throws SQLException {
    Timestamp updatedTs = rs.getTimestamp("updated_at");
    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
    UserDto userDto = new UserDto(
        rs.getInt("user_seq"),
        rs.getString("email"),
        rs.getString("name"),
        rs.getString("birth"),
        rs.getString("phone"),
        Status.valueOf(rs.getString("status")),
        rs.getBoolean("marketing_received_status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        updatedAt
        );
    return userDto;
  }
  
}
