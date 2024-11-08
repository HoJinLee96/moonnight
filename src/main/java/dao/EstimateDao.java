package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import dto.EstimateDto;
import dto.EstimateDto.Status;

@Repository
public class EstimateDao {

  DataSource dataSource;

  @Autowired
  public EstimateDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Transactional
  public int registerEstimate(EstimateDto estimateDto) throws SQLException {
    
    String sql =
        "INSERT INTO estimate (user_seq, name, phone, email, emailAgree, smsAgree, callAgree, postcode,mainAddress,detailAddress,content, imagesPath,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
      if(estimateDto.getUserSeq()!=0) pst.setInt(1, estimateDto.getUserSeq());
      else pst.setNull(1, Types.INTEGER); 
      
      if(estimateDto.getName()!=null) pst.setString(2, estimateDto.getName());
      else pst.setNull(2, Types.VARCHAR); 
      
      pst.setString(3, estimateDto.getPhone());
      
      if(estimateDto.getEmail()!=null) pst.setString(4, estimateDto.getEmail());
      else pst.setNull(4, Types.VARCHAR); 
      
      pst.setBoolean(5, estimateDto.isEmailAgree());
      pst.setBoolean(6, estimateDto.isSmsAgree());
      pst.setBoolean(7, estimateDto.isCallAgree());
      pst.setString(8, estimateDto.getPostcode());
      pst.setString(9, estimateDto.getMainAddress());
      
      if(estimateDto.getDetailAddress()!=null) pst.setString(10, estimateDto.getDetailAddress());
      else pst.setNull(10, Types.VARCHAR); 
      
      if(estimateDto.getContent()!=null) pst.setString(11, estimateDto.getContent());
      else pst.setNull(11, Types.VARCHAR); 
      
      if(estimateDto.getImagesPath()!=null) pst.setString(12, estimateDto.getImagesPath());
      else pst.setNull(12, Types.VARCHAR); 
      
      pst.setString(13, estimateDto.getStatus().name());
      pst.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
      pst.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
      pst.executeUpdate();
      try (ResultSet rs = pst.getGeneratedKeys();) {
        if (rs.next()) {
          return rs.getInt(1);
        } else {
          return 0;
        }
      }
    }
  }
  
  public Optional<EstimateDto> getEstimate(int estimateSeq) throws SQLException{
    String sql = "select * from estimate where estimate_seq=?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);) {
      pst.setInt(1, estimateSeq);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          EstimateDto estimate = new EstimateDto(
              estimateSeq,
              rs.getInt("user_seq"),
              rs.getString("name"),
              rs.getString("phone"),
              rs.getString("email"),
              rs.getBoolean("emailAgree"),
              rs.getBoolean("smsAgree"),
              rs.getBoolean("callAgree"),
              rs.getString("postcode"),
              rs.getString("mainAddress"),
              rs.getString("detailAddress"),
              rs.getString("content"),
              rs.getString("imagesPath"),
              Status.valueOf(rs.getString("status")),
              rs.getTimestamp("created_at").toLocalDateTime(),
              rs.getTimestamp("updated_at").toLocalDateTime()
          );
          return Optional.of(estimate);
        }
        return Optional.empty();
      }
    }
  }
  
  @Transactional
  public int updateEstimate(EstimateDto estimateDto) throws SQLException {
    String sql = "update estimate set name=?, phone=?, email=?, emailAgree=?, smsAgree=?, callAgree=?, postcode=?, mainAddress=?, detailAddress=?, content=?, imagesPath=?, status=?, updated_at=? where estimate_seq=?";
    try(
        Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ){
      pst.setString(1, estimateDto.getName());
      pst.setString(2, estimateDto.getPhone());
      pst.setString(3, estimateDto.getEmail());
      pst.setBoolean(4, estimateDto.isEmailAgree());
      pst.setBoolean(5, estimateDto.isSmsAgree());
      pst.setBoolean(6, estimateDto.isSmsAgree());
      pst.setString(7, estimateDto.getPostcode());
      pst.setString(8, estimateDto.getMainAddress());
      pst.setString(9, estimateDto.getDetailAddress());
      pst.setString(10, estimateDto.getContent());
      pst.setString(11, estimateDto.getImagesPath());
      pst.setString(12, estimateDto.getStatus().name());
      pst.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(14, estimateDto.getEstimateSeq());
      return pst.executeUpdate();
    }
  }
  
  @Transactional
  public int deleteEstimate(int estimateSeq) throws SQLException {
    String sql = "update estimate set status=?, updated_at=? where estimate_seq=?";
    try(
        Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ){
      pst.setString(1, EstimateDto.Status.DELETE.name());
      pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
      pst.setInt(3, estimateSeq);
      return pst.executeUpdate();
    }
  }
  
  public List<EstimateDto> getEstimateByUserSeq(int userSeq) throws SQLException {
    String sql = "select * from estimate where user_seq = ? ORDER BY created_at DESC";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);) {
      pst.setInt(1, userSeq);
      try (ResultSet rs = pst.executeQuery();) {
        List<EstimateDto> list = new ArrayList<>();
        list = createResultSetToEstimates(rs);
        return list;
      }
    }
  }
  
  
  public int getCountAll() throws SQLException {
    String sql = "SELECT COUNT(*) FROM estimate";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      if (rs.next()) {
        return rs.getInt(1);
      } else {
        return 0;
      }
    }
  }
  

  public String getImagesPath(int estimateSeq) throws SQLException {

    String sql = "SELECT imagesPath FROM estimate WHERE estimate_seq = ?";
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);) {
      pst.setInt(1, estimateSeq);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return rs.getString("imagesPath");
        }
      }
    }
    return "";
  }

  public List<EstimateDto> getAllEstimate(int page) throws SQLException {

    String sql = "SELECT * FROM estimate ORDER BY created_at DESC LIMIT 50 OFFSET " + (page - 1) * 50;
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();) {
      List<EstimateDto> list = new ArrayList<>();
      if (rs.next()) {
        list = createResultSetToEstimates(rs);
      }
      return list;
    }
  }

  public List<EstimateDto> getEstimateByStatus(int chepter, Status status) throws SQLException {

    String sql = "SELECT * FROM estimate WHERE status = ? ORDER BY created_at DESC LIMIT 50 OFFSET "
        + chepter * 50;

    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, status.name());
      try (ResultSet rs = pst.executeQuery()) {
        List<EstimateDto> list = new ArrayList<>();
        if (rs.next()) {
          list = createResultSetToEstimates(rs);
        }
        return list;
      }
    }
  }

  public List<EstimateDto> createResultSetToEstimates(ResultSet rs) throws SQLException {
    System.out.println("EstimateDao.mapResultSetToEstimates() 실행");
    List<EstimateDto> estimates = new ArrayList<>();
    while (rs.next()) {
      EstimateDto estimate = new EstimateDto();
      estimate.setEstimateSeq(rs.getInt("estimate_seq"));
      estimate.setName(rs.getString("name"));
      estimate.setPhone(rs.getString("phone"));
      estimate.setEmail(rs.getString("email"));
      estimate.setEmailAgree(rs.getBoolean("emailAgree"));
      estimate.setSmsAgree(rs.getBoolean("smsAgree"));
      estimate.setCallAgree(rs.getBoolean("callAgree"));
      estimate.setPostcode(rs.getString("postcode"));
      estimate.setMainAddress(rs.getString("mainAddress"));
      estimate.setDetailAddress(rs.getString("detailAddress"));
      estimate.setContent(rs.getString("content"));
      estimate.setImagesPath(rs.getString("imagesPath"));
      estimate.setStatus(Status.valueOf(rs.getString("status")));
      estimate.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
      estimates.add(estimate);
    }
    return estimates;
  }
  



}
