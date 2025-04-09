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
import entity.OAuth;
import entity.OAuth.Provider;
import entity.OAuth.Status;


@Repository
public class OAuthDao extends BaseDao<OAuth>{

  @Autowired
  public OAuthDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerOAuth(OAuth oAuthDto) {
    String sql = "INSERT INTO `oauth` (user_seq, provider, id) VALUES(?,?,?)";
    return register(sql, oAuthDto.getUserSeq(), oAuthDto.getProvider().name(), oAuthDto.getId());
  }

  public OAuth getOAuthByOAuthSeq(int oAuthSeq) {
    return getDtoBySequence(oAuthSeq);
  }
  
  public Optional<OAuth> getOAuthByOAuthId(String id) {
    String sql = "SELECT * FROM `oauth` WHERE `id` = ?";
    return jdbcTemplate.query(sql, new OAuthDtoRowMapper(), id).stream().findFirst();
  }
  
  public void updateStatusByOAuthSeq(int oAuthSeq, Status status) {
    String sql = "UPDATE `oauth` SET `status` = ?, `updated_at` = NOW() WHERE `oauth_seq` = ?";
    update(sql, status.name(), oAuthSeq);
  }
  
  public void updateUserSeqByOAuthSeq(int oAuthSeq, int userSeq) {
    String sql = "UPDATE `oauth` SET `user_seq` = ?, `updated_at` = NOW() WHERE `oauth_seq` = ?";
    update(sql, userSeq, oAuthSeq);
  }
  
  public boolean isIdExists(String id){
    String sql = "SELECT EXISTS (SELECT 1 FROM `oauth` WHERE `id` = ?)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, id);
  }
  
  private static class OAuthDtoRowMapper implements RowMapper<OAuth> {
    @Override
    public OAuth mapRow(ResultSet rs, int rowNum) throws SQLException  {
      Timestamp updatedTs = rs.getTimestamp("updated_at");
      LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
      
      return new OAuth(
        rs.getInt("oauth_seq"),
        rs.getInt("user_seq"),
        Provider.valueOf(rs.getString("provider")),
        rs.getString("id"),
        Status.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toLocalDateTime(),
        updatedAt
        );
    }
  }

  @Override
  protected RowMapper<OAuth> getRowMapper() {
    return new OAuthDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "oauth";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "oauth_seq";
  }
  
//  public int registerOAuth(OAuthUserDto oAuthUserDto) throws SQLException {
//    String sql = "INSERT INTO `oauth_user` (provider,id,email,name,birth,phone,status) VALUES(?,?,?,?,?,?,?)";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
//      pst.setString(1, oAuthUserDto.getProvider().name());
//      pst.setString(2, oAuthUserDto.getId());
//      pst.setString(3, oAuthUserDto.getEmail());
//      pst.setString(4, oAuthUserDto.getName());
//      pst.setString(5, oAuthUserDto.getBirth());
//      pst.setString(6, oAuthUserDto.getPhone());
//      pst.setString(7, Status.NORMAL.name());
//      pst.executeUpdate();
//      ResultSet generatedKeys = pst.getGeneratedKeys();
//      if (generatedKeys.next()) {
//        return generatedKeys.getInt(1);
//      }
//    }
//    return 0;
//  }
//  
//  public Optional<OAuthUserDto> getOAuthByOAuthId(Provider provider, String oAuthId) throws SQLException {
//    String sql = "SELECT * FROM `oauth_user` WHERE `provider` = ? AND `id` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setString(1, provider.name());
//      pst.setString(2, oAuthId);
//      try (ResultSet rs = pst.executeQuery();) {
//        if (rs.next()) {
//          return Optional.of(mapOAuthUserDto(rs));
//        }
//      }
//    }
//    return Optional.empty();
//  }
//  
//  public Optional<OAuthUserDto> getOAuthByOAuthSeq(int oAuthSeq) throws SQLException {
//    String sql = "SELECT * FROM `oauth_user` WHERE `oauth_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, oAuthSeq);
//      try (ResultSet rs = pst.executeQuery();) {
//        if (rs.next()) {
//          return Optional.of(mapOAuthUserDto(rs));
//        }
//      }
//    }
//    return Optional.empty();
//  }
//  
//  public int updateStatusByOAuthSeq(int oAuthSeq, Status status) throws SQLException{
//    String sql = "UPDATE `oauth_user` SET `status` = ?, `updated_at` = NOW() WHERE `oauth_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setString(1, status.name());
//      pst.setInt(2, oAuthSeq);
//      return pst.executeUpdate();
//    }
//  }
//  
//  public int updateUserSeqByOAuthSeq(int oAuthSeq, int userSeq) throws SQLException {
//    String sql = "UPDATE `oauth_user` SET `user_seq` = ?, `updated_at` = NOW() WHERE `oauth_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, userSeq);
//      pst.setInt(2, oAuthSeq);
//      return pst.executeUpdate();
//    }
//  }
//  
//  private OAuthUserDto mapOAuthUserDto(ResultSet rs) throws SQLException {
//    Timestamp createdTs = rs.getTimestamp("created_at");
//    Timestamp updatedTs = rs.getTimestamp("updated_at");
//    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
//    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
//    OAuthUserDto oAuthUserDto = new OAuthUserDto(
//        rs.getInt("oauth_seq"),
//        rs.getInt("user_seq"),
//        Provider.valueOf(rs.getString("provider")),
//        rs.getString("id"),
//        rs.getString("email"),
//        rs.getString("name"),
//        rs.getString("birth"),
//        rs.getString("phone"),
//        Status.valueOf(rs.getString("status")),
//        createdAt,
//        updatedAt
//        );
//    return oAuthUserDto;
//  }


}
