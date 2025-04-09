package deprecated;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDao extends BaseDao<Comment>
{
  
  @Autowired
  public CommentDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerCommentDto(Comment commentDto) {
    String sql = "INSERT INTO `comment` (user_seq, estimate_seq, comment_text) VALUES (?, ?, ?)";
    return register(sql, commentDto.getUserSeq(), commentDto.getEstimateSeq(), commentDto.getCommentText());
  }
  
  public Comment getCommentDtoByCommentSeq(int commentSeq) {
    return getDtoBySequence(commentSeq);
  }
  
  public List<Comment> getCommentListByEstimateSeq(int estimateSeq) {
    String sql = "SELECT * FROM `comment` WHERE `estimate_seq` = ? ORDER BY `created_at` ASC";
    return jdbcTemplate.query(sql, new CommentDtoRowMapper(), estimateSeq);
  }

  public void updateComment(Comment commentDto) {
    String sql = "UPDATE `comment` SET `comment_text` = ?, `updated_at` = NOW() WHERE `comment_seq` = ?";
    update(sql, commentDto.getCommentText(), commentDto.getCommentSeq());
  }

  public void updateCommentStatus(int commentSeq, Status status) {
    String sql = "UPDATE `comment` SET `status` = ?, `updated_at` = NOW() WHERE `comment_seq` = ?";
    update(sql, status.name(), commentSeq);
  }
  
  private static class CommentDtoRowMapper implements RowMapper<Comment> {
    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
      Timestamp updatedTs = rs.getTimestamp("updated_at");
      LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
      
      return new Comment(
          rs.getInt("comment_seq"),
          rs.getInt("user_seq"),
          rs.getInt("estimate_seq"),
          rs.getString("comment_text"),
          Status.valueOf(rs.getString("status")),
          rs.getTimestamp("created_at").toLocalDateTime(),
          updatedAt
      );
    }
  }

  @Override
  protected RowMapper<Comment> getRowMapper() {
    return new CommentDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "comment";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "comment_seq";
  }
  
  public int registerComment(CommentDto commentDto) throws SQLException {
    String sql = "INSERT INTO `comment` (estimate_seq, user_seq, commentText) VALUES (?,?,?)";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);){
      pst.setInt(1, commentDto.getEstimateSeq());
      pst.setInt(2, commentDto.getUserSeq());
      pst.setString(3, commentDto.getCommentText());
      pst.executeUpdate();
      try(ResultSet rs = pst.getGeneratedKeys();){
        if(rs.next()) {
          return rs.getInt(1);
        }
      }
    }
    return 0;
  }
  
  public List<CommentDto> getCommentList(int estimateSeq) throws SQLException {
    String sql = "SELECT * FROM `comment` WHERE `estimate_seq` = ? ORDER BY `created_at` ASC";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);) {
      pst.setInt(1, estimateSeq);
      try (ResultSet rs = pst.executeQuery();) {
        List<CommentDto> list = new ArrayList<>();
        while (rs.next()) {
          list.add(mapCommentDto(rs));
        }
        return list;
      }
    }
  }
  
  public Optional<CommentDto> getComment(int commentSeq) throws SQLException {
    String sql = "SELECT * FROM `comment` where `comment_seq` = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);) {
      pst.setInt(1, commentSeq);
      try (ResultSet rs = pst.executeQuery();) {
        if (rs.next()) {
          CommentDto commentDto = mapCommentDto(rs);
          return Optional.of(commentDto);
        }
      }
    }
    return Optional.empty();
  }
  
  public int updateComment(CommentDto commentDto) throws SQLException {
    String sql = "UPDATE `comment` SET `commentText` = ?, `updated_at` = NOW() WHERE `comment_seq` = ?";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setString(1, commentDto.getCommentText());
      pst.setInt(2, commentDto.getCommentSeq());
      return pst.executeUpdate();
      }
    }
  
  public int deleteComment(int commentSeq) throws SQLException {
    String sql = "DELETE FROM `comment` WHERE `comment_seq` = ?";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      return pst.executeUpdate();
      }
  }
  
  private CommentDto mapCommentDto(ResultSet rs) throws SQLException {
    Timestamp createdTs = rs.getTimestamp("created_at");
    Timestamp updatedTs = rs.getTimestamp("updated_at");
    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
    CommentDto comment = new CommentDto(
        rs.getInt("comment_seq"),
        rs.getInt("user_seq"),
        rs.getInt("estimateSeq"),
        rs.getString("commentText"),
        createdAt,
        updatedAt
    );
    return comment;
  }
  
}
