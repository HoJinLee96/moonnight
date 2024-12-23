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
import dto.EstimateSearchRequest;
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
  
  public Optional<EstimateDto> getEstimateByEstimateSeq(int estimateSeq) throws SQLException{
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
  
  
  public int getCountByStatus(String status) throws SQLException {
    String sql;
    if ("ALL".equals(status)) {
      sql = "SELECT COUNT(*) FROM estimate WHERE status IN ('RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'DELETE')";
    } else {
      sql = "SELECT COUNT(*) FROM estimate WHERE status = ?";
    }
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
       if (!"ALL".equals(status)) {
           pst.setString(1, status);
       }

       try (ResultSet rs = pst.executeQuery()) {
           if (rs.next()) {
               return rs.getInt(1);
           } else {
               return 0;
           }
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

  public List<EstimateDto> getEstimateSearch(EstimateSearchRequest reqEstimateDto) throws SQLException {

    String sql = buildEstimateSearchQuery(reqEstimateDto);
    System.out.println(sql);
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)){
      try(ResultSet rs = pst.executeQuery()){ 
        List<EstimateDto> list = new ArrayList<>();
        list = createResultSetToEstimates(rs);
        return list;
      }
    }
  }
  
  public int getCountEstimateSearch(EstimateSearchRequest reqEstimateDto) throws SQLException {

    String sql = buildEstimateSearchQuery(reqEstimateDto);
    sql=sql.replaceFirst("SELECT \\*", "SELECT COUNT(*)");
    if(sql.contains("LIMIT")) {
      sql = sql.substring(0, sql.indexOf("LIMIT")).trim() + ";";
    }
    System.out.println(sql);
    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()){
      if(rs.next()) {
        return rs.getInt(1);
      }
        return 0;
    }
  }

  public List<EstimateDto> getEstimateByStatus(int chepter, Status status) throws SQLException {

    String sql = "SELECT * FROM estimate WHERE status = ? ORDER BY created_at DESC LIMIT 50 OFFSET "+ chepter * 50;

    try (Connection con = dataSource.getConnection();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, status.name());
      try (ResultSet rs = pst.executeQuery()) {
        List<EstimateDto> list = new ArrayList<>();
        list = createResultSetToEstimates(rs);
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
  

  public String buildEstimateSearchQuery(EstimateSearchRequest request) {
    // 기본 SQL 시작 부분
    StringBuilder sql = new StringBuilder("SELECT * FROM estimate WHERE 1=1");

    // 상태 조건 추가
    if (request.getStatus() != null && request.getStatus() != EstimateSearchRequest.Status.ALL) {
      sql.append(" AND status = '").append(request.getStatus().name()).append("'");
    }

    // 기간 조건 추가
    if (request.getPeriodType() != null) {
        switch (request.getPeriodType()) {
            case TODAY:
                sql.append(" AND DATE(created_at) = CURDATE()");
                break;
            case DAYS7:
                sql.append(" AND created_at >= NOW() - INTERVAL 7 DAY");
                break;
            case DAYS30:
                sql.append(" AND created_at >= NOW() - INTERVAL 30 DAY");
                break;
            case MONTHLY:
                if (request.getYear() != null && request.getMonth() != null) {
                  String formattedMonth = String.format("%02d", Integer.parseInt(request.getMonth())); // 앞에 0을 붙여 두 자리로 변환
                  sql.append(" AND DATE_FORMAT(created_at, '%Y-%m') = '")
                     .append(request.getYear()).append("-").append(formattedMonth).append("'");
                }
                break;
            case RANGE:
                if (request.getStartDate() != null && request.getEndDate() != null) {
                    sql.append(" AND created_at BETWEEN '")
                       .append(request.getStartDate()).append("' AND '").append(request.getEndDate()).append("'");
                }
                break;
        }
    }

    // 검색 조건 추가
    if (request.getSearchType() != null && request.getSearchWords() != null && !request.getSearchWords().trim().isEmpty()) {
        switch (request.getSearchType()) {
            case ADDRESS:
                sql.append(" AND address LIKE '%").append(request.getSearchWords()).append("%'");
                break;
            case EMAIL:
                sql.append(" AND email = '").append(request.getSearchWords()).append("'");
                break;
            case ESTIMATE_SEQ:
                sql.append(" AND estimate_seq = ").append(request.getSearchWords());
                break;
            case NAME:
                sql.append(" AND name LIKE '%").append(request.getSearchWords()).append("%'");
                break;
            case PHONE:
                sql.append(" AND phone = '").append(request.getSearchWords()).append("'");
                break;
        }
    }

    // 정렬 조건 추가
    if (request.getSortType() != null) {
        sql.append(" ORDER BY created_at ").append(request.getSortType().name());
    } else {
        // 기본 정렬: 최신순
        sql.append(" ORDER BY created_at DESC");
    }

    // 페이징 조건 추가
    if (request.getPage() > 0 && request.getSize() > 0) {
        int offset = (request.getPage() - 1) * request.getSize();
        sql.append(" LIMIT ").append(offset).append(", ").append(request.getSize());
    }

    // 최종 SQL 반환
    return sql.toString();
}

}
