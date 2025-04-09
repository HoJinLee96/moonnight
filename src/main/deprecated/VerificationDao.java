package deprecated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import entity.Verification;

@Repository
public class VerificationDao extends BaseDao<Verification> {

  @Autowired
  public VerificationDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerVerificationDto(Verification verificationDto) {
    String sql =
        "INSERT INTO `verification` (`request_ip`, `to`, `verification_code`, `sendStatus`) VALUES (?,?,?,?)";
    return register(sql, verificationDto.getRequestIp(), verificationDto.getTo(), verificationDto.getVerificationCode(),
        verificationDto.getSendStatus());
  }

  // public VerificationDto getVerificationDtoByVerSeq(int verSeq) {
  // return getDtoBySequence(verSeq);
  // }

  public Optional<Verification> getVerificationDtoByTo(String to) {
    String sql = "SELECT * FROM `verification` WHERE `to`=? ORDER BY `created_at` DESC LIMIT 1";
    return jdbcTemplate.query(sql, new VerificationDtoRowMapper(), to).stream().findFirst();
  }

  public void updateVerify(int verSeq, boolean result) {
    String sql =
        "UPDATE `verification` SET `verify` = ?, `verify_at` = NOW() WHERE `verification_seq` = ?";
    update(sql, result, verSeq);
  }
  
  public boolean isVerifyByVerSeq(int verSeq) {
    String sql =
        "SELECT EXISTS (SELECT 1 FROM `verification` WHERE `verification_seq` = ? AND `verify` = TRUE)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, verSeq);
  }

  public boolean isWithinVerificationTime(int verSeq) {
    String sql =
        "SELECT EXISTS (SELECT 1 FROM `verification` WHERE `verification_seq` = ? AND `created_at` >= NOW() - INTERVAL 5 MINUTE)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, verSeq);
  }

  class VerificationDtoRowMapper implements RowMapper<Verification> {
    @Override
    public Verification mapRow(ResultSet rs, int rowNum) throws SQLException {

      Timestamp verifyTs = rs.getTimestamp("verify_at");
      LocalDateTime verifyAt = verifyTs != null ? verifyTs.toLocalDateTime() : null;

//      return new Verification(
//          rs.getInt("verification_seq"), 
//          rs.getString("request_ip"),
//          rs.getString("to"),
//          rs.getString("verification_code"), 
//          rs.getString("send_status"),
//          rs.getTimestamp("created_at").toLocalDateTime(), 
//          rs.getObject("verify", Boolean.class),
//          verifyAt);
    }
  }

  @Override
  protected RowMapper<Verification> getRowMapper() {
    return new VerificationDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "verification";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "verification_seq";
  }


  // public int registerVerificationDto(VerificationDto responseDto) throws SQLException {
  // String sql = "INSERT INTO `verification` (`to`,`verification_code`,`sendStatus`) VALUES
  // (?,?,?)";
  // try (Connection connection = dataSource.getConnection();
  // PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
  // pst.setString(1, responseDto.getTo());
  // pst.setString(2, responseDto.getVerificationCode());
  // pst.setString(3, responseDto.getSendStatus());
  // pst.executeUpdate();
  // ResultSet generatedKeys = pst.getGeneratedKeys();
  // if (generatedKeys.next()) {
  // return generatedKeys.getInt(1);
  // }
  // }
  // return 0;
  // }
  //
  // public Optional<VerificationDto> getVerificationDtoByVerSeq(int verSeq) throws SQLException {
  // String sql = "SELECT * FROM `verification` WHERE `verification_seq` = ?";
  // try (Connection con = dataSource.getConnection();
  // PreparedStatement pst = con.prepareStatement(sql);) {
  // pst.setInt(1, verSeq);
  // try (ResultSet rs = pst.executeQuery()) {
  // if (rs.next()) {
  // return Optional.of(mapVerificationDto(rs));
  // }
  // }
  // }
  // return Optional.empty();
  // }
  //
  // public Optional<VerificationDto> getVerificationDtoByTo(String to) throws SQLException {
  // String sql = "SELECT * FROM verification where `to`=? ORDER BY created_at DESC LIMIT 1";
  //
  // try (Connection con = dataSource.getConnection();
  // PreparedStatement pst = con.prepareStatement(sql);) {
  // pst.setString(1, to);
  // try (ResultSet rs = pst.executeQuery()) {
  // if(rs.next()) {
  // return Optional.of(mapVerificationDto(rs));
  // }
  // }
  // }
  // return Optional.empty();
  // }
  //
  // public int updateVerify(int verSeq, boolean result) throws SQLException {
  // String sql = "UPDATE verification SET `verified` = ?, `verified_at` = NOW() WHERE
  // `verification_seq` = ?";
  // try (Connection con = dataSource.getConnection();
  // PreparedStatement pst = con.prepareStatement(sql)) {
  // pst.setBoolean(1, result);
  // pst.setInt(2, verSeq);
  // return pst.executeUpdate();
  // }
  // }
  //
  // public boolean isWithinVerificationTime(int verSeq) throws SQLException {
  // String sql = "SELECT COUNT(*) FROM `verification` WHERE `verification_seq` = ? AND `created_at`
  // >= NOW() - INTERVAL 5 MINUTE";
  // try (Connection con = dataSource.getConnection();
  // PreparedStatement pst = con.prepareStatement(sql)) {
  // pst.setInt(1, verSeq);
  // try (ResultSet rs = pst.executeQuery()) {
  // if (rs.next()) {
  // return rs.getInt(1) > 0;
  // }
  // }
  // }
  // return false;
  // }
  //
  // private VerificationDto mapVerificationDto(ResultSet rs) throws SQLException {
  // Timestamp createdTs = rs.getTimestamp("created_at");
  // Timestamp verifiedTs = rs.getTimestamp("verified_at");
  // LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
  // LocalDateTime verifiedAt = verifiedTs != null ? verifiedTs.toLocalDateTime() : null;
  // VerificationDto verificationDto = new VerificationDto(
  // rs.getInt("verification_seq"),
  // rs.getString("to"),
  // rs.getString("verification_code"),
  // rs.getString("sendStatus"),
  // createdAt,
  // rs.getBoolean("verified"),
  // verifiedAt
  // );
  // return verificationDto;
  // }



}
