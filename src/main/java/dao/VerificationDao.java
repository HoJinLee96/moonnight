package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import dto.VerifyResponseDto;

@Repository
public class VerificationDao {

  private final DataSource dataSource;

  @Autowired
  public VerificationDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public VerifyResponseDto register(VerifyResponseDto responseDto) throws SQLException {
    String sql = "INSERT INTO verification (`to`,`verification_code`,`status`,`create_at`) VALUES (?,?,?,?)";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, responseDto.getTo());
      statement.setString(2, responseDto.getVerificationCode());
      statement.setString(3, responseDto.getStatusCode());
      statement.setTimestamp(4, Timestamp.valueOf(responseDto.getRequestTime()));
      int affectedRows = statement.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("테이블에 저장했으나 바뀐 행 없음.");
      }
      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          int verificationSeq = generatedKeys.getInt(1);
          return new VerifyResponseDto.Builder()
              .verificationSeq(verificationSeq)
              .to(responseDto.getTo())
              .statusCode(responseDto.getStatusCode())
              .requestTime(responseDto.getRequestTime())
              .build();
          } else {
          throw new SQLException("테이블에 저장했으나 시퀀스값 없음");
        }
      }
    }
  }

  public Optional<String> getVerificationCode(int verSeq) throws SQLException {
    String sql = "SELECT `verification_code` FROM verification where `verification_seq`=?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement state = con.prepareStatement(sql);) {
      state.setInt(1, verSeq);
      try (ResultSet rs = state.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getString("verification_code"));
        }
      }
    }
    return Optional.empty();

  }
}
