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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import dto.CommentDto;

@Repository
public class CommentDao {
  
  DataSource dataSource;
  
  @Autowired
  public CommentDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  @Transactional
  public int registerComment(CommentDto commentDto) throws SQLException {
    String sql = "insert into comment (estimate_seq, user_seq, commentText, created_at, updated_at) values(?,?,?,?,?)";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);){
      pst.setInt(1, commentDto.getEstimateSeq());
      pst.setInt(2, commentDto.getUserSeq());
      pst.setString(3, commentDto.getCommentText());
      pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
      pst.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
      pst.executeUpdate();
      try(ResultSet rs = pst.getGeneratedKeys();){
        if(rs.next()) {
          return rs.getInt(1);
        }else {
          return 0;
        }
      }
    }
  }
  
  public List<CommentDto> getCommentList(int estimateSeq) throws SQLException {
    String sql = "select * from comment where estimate_seq = ? ORDER BY created_at ASC";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setInt(1, estimateSeq);
      try(ResultSet rs = pst.executeQuery();){
        List<CommentDto> list = new ArrayList<>();
        while(rs.next()) {
          CommentDto comment = new CommentDto(
              rs.getInt("comment_seq"),
              rs.getInt("user_seq"),
              estimateSeq,
              rs.getString("commentText"),
              rs.getTimestamp("created_at").toLocalDateTime(),
              rs.getTimestamp("updated_at").toLocalDateTime());
          list.add(comment);
        }
        return list;
      }
    }
  }
  
  public Optional<CommentDto> getComment(int commentSeq) throws SQLException {
    String sql = "select * from comment where comment_seq = ?";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setInt(1, commentSeq);
      try(ResultSet rs = pst.executeQuery();){
        CommentDto commentDto = new CommentDto();
        if(rs.next()) {
          commentDto.setCommentSeq(commentSeq);
          commentDto.setUserSeq(rs.getInt("user_seq"));
          commentDto.setEstimateSeq(rs.getInt("estimate_seq"));
          commentDto.setCommentText(rs.getString("commentText"));
          commentDto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
          commentDto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
          return Optional.of(commentDto);
        }else {
        return Optional.empty();
        }
      }
    }
  }
  
  @Transactional
  public int updateComment(CommentDto commentDto) throws SQLException {
    String sql = "update comment set estimate_seq = ?, user_seq = ?, commentText = ?, updated_at = ? where comment_seq = ?";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      pst.setInt(1, commentDto.getEstimateSeq());
      pst.setInt(2, commentDto.getUserSeq());
      pst.setString(3, commentDto.getCommentText());
      pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(5, commentDto.getCommentSeq());
      return pst.executeUpdate();
      }
    }
  
  @Transactional
  public int deleteComment(int commentSeq) throws SQLException {
    String sql = "delete from comment where comment_seq = ?";
    try(Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);){
      return pst.executeUpdate();
      }
  }
  
}
