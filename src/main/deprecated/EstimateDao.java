package deprecated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import dto.request.EstimateSearchRequest;
import entity.EstimateDto;
import entity.EstimateDto.Status;

@Repository
public class EstimateDao extends BaseDao<EstimateDto>{

  @Autowired
  public EstimateDao(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public int registerEstimate(EstimateDto estimateDto)  {
    String sql = "INSERT INTO `estimate` (user_seq, name, phone, email, email_agree, sms_agree, call_agree, postcode, main_address, detail_address, content, images) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    return register(sql, 
        estimateDto.getUserSeq() != null ? estimateDto.getUserSeq() : null, 
        estimateDto.getName() != null ? estimateDto.getName() : null,
        estimateDto.getPhone() != null ? estimateDto.getPhone() : null, 
        estimateDto.getEmail(), 
        estimateDto.isEmailAgree(), 
        estimateDto.isSmsAgree(), 
        estimateDto.isCallAgree(),
        estimateDto.getPostcode() != null ? estimateDto.getPostcode() : null,
        estimateDto.getMainAddress(),
        estimateDto.getDetailAddress() != null ? estimateDto.getDetailAddress() : null,
        estimateDto.getContent() != null ? estimateDto.getContent() : null,
        estimateDto.getImages() != null ? estimateDto.getImages() : null
        );
  }
  
  public EstimateDto getEstimateByEstimateSeq(int estimateSeq) {
    return getDtoBySequence(estimateSeq);
  }
  
  public List<EstimateDto> getEstimateByUserSeq(int userSeq)  {
    String sql = "SELECT * FROM `estimate` WHERE `user_seq` = ? ORDER BY `created_at` DESC";
    return jdbcTemplate.query(sql, new EstimateDtoRowMapper(), userSeq);
  }
  
  public List<EstimateDto> getEstimateSearch(EstimateSearchRequest estimateSearchRequest){
    String sql = buildEstimateSearchQuery(estimateSearchRequest);
    return jdbcTemplate.query(sql, new EstimateDtoRowMapper());
  }
  
//  public List<EstimateDto> getEstimateByStatus(int chepter, Status status)  {
//    String sql = "SELECT * FROM estimate WHERE status = ? ORDER BY created_at DESC LIMIT 50 OFFSET "+ chepter * 50;
//    return jdbcTemplate.query(sql, new EstimateDtoRowMapper(), status.name());
//  }

  public Optional<String> getImages(int estimateSeq)  {
    String sql = "SELECT `images` FROM `estimate` WHERE `estimate_seq` = ?";
    return jdbcTemplate.queryForList(sql, String.class, estimateSeq).stream().findFirst();
  }

  public int getCountEstimateSearch(EstimateSearchRequest reqEstimateDto)  {
    String sql = buildEstimateSearchQuery(reqEstimateDto);
    sql=sql.replaceFirst("SELECT \\*", "SELECT COUNT(*)");
    if(sql.contains("LIMIT")) {
      sql = sql.substring(0, sql.indexOf("LIMIT")).trim() + ";";
    }
    return jdbcTemplate.queryForObject(sql, Integer.class);
  }

  public int getCountByStatus(String status) {
    String sql = "SELECT COUNT(*) FROM `estimate`" +
                 ("ALL".equals(status) ? " WHERE `status` IN ('RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'DELETE')" : " WHERE `status` = ?");
    
    return "ALL".equals(status) ? 
           jdbcTemplate.queryForObject(sql, Integer.class) : 
           jdbcTemplate.queryForObject(sql, Integer.class, status);
  }

  public void updateEstimate(EstimateDto estimateDto)  {
    String sql = "UPDATE `estimate` SET `name` = ?, `email` = ?, `email_agree` = ?, `sms_agree` = ?, `call_agree` = ?, `postcode` = ?, `main_address` = ?, `detail_address` = ?, `content` = ?, `images` = ?, `updated_at` = NOW() WHERE `estimate_seq` = ?";
    update(sql, 
        estimateDto.getName() != null ? estimateDto.getName() : null,
        estimateDto.getEmail() != null ? estimateDto.getEmail() : null,
        estimateDto.isEmailAgree(),
        estimateDto.isSmsAgree(),
        estimateDto.isSmsAgree(),
        estimateDto.getPostcode() != null ? estimateDto.getPostcode() : null,
        estimateDto.getMainAddress(),
        estimateDto.getDetailAddress() != null ? estimateDto.getDetailAddress() : null,
        estimateDto.getContent() != null ? estimateDto.getContent() : null,
        estimateDto.getImages() != null ? estimateDto.getImages() : null,
        estimateDto.getEstimateSeq()
        );
  }

  public void updateEstimateStatus(int estimateSeq, Status status)  {
    String sql = "UPDATE `estimate` SET `status` = ?, `updated_at` = NOW() WHERE `estimate_seq` = ?";
    update(sql, status.name(), estimateSeq);
  }

  public String buildEstimateSearchQuery(EstimateSearchRequest request) {
    // 기본 SQL 시작 부분
    StringBuilder sql = new StringBuilder("SELECT * FROM `estimate` WHERE 1=1");

    // 상태 조건 추가
    if (request.getStatus() != null && request.getStatus() != EstimateSearchRequest.Status.ALL) {
      sql.append(" AND status = '").append(request.getStatus().name()).append("' ");
    }

    // 기간 조건 추가
    if (request.getPeriodType() != null) {
        switch (request.getPeriodType()) {
            case ALL:
              break;
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
                sql.append(" AND main_address LIKE '%").append(request.getSearchWords()).append("%'")
                .append(" OR detail_address LIKE '%").append(request.getSearchWords()).append("%'");
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

  private static class EstimateDtoRowMapper implements RowMapper<EstimateDto> {
    @Override
    public EstimateDto mapRow(ResultSet rs, int rowNum) throws SQLException   {
      String images = rs.getString("images");
      List<String> imagesList = images != null ? List.of(images) : null;
      
      Timestamp updatedTs = rs.getTimestamp("updated_at");
      LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
      
      return new EstimateDto(
        rs.getInt("estimate_seq"),
        rs.getObject("user_seq", Integer.class),
        rs.getString("name"),
        rs.getString("phone"),
        rs.getString("email"),
        rs.getBoolean("email_agree"),
        rs.getBoolean("sms_agree"),
        rs.getBoolean("call_agree"),
        rs.getString("postcode"),
        rs.getString("main_address"),
        rs.getString("detail_address"),
        rs.getString("content"),
        imagesList,
        Status.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toLocalDateTime(),
        updatedAt
        );
    }
  }
  
  @Override
  protected RowMapper<EstimateDto> getRowMapper() {
    return new EstimateDtoRowMapper();
  }

  @Override
  protected String getTableName() {
    return "estimate";
  }

  @Override
  protected String getPrimaryKeyColumn() {
    return "estimate_seq";
  }
  
//  public int registerEstimate(EstimateDto estimateDto)  {
//    String sql = "INSERT INTO `estimate` (user_seq, name, phone, email, emailAgree, smsAgree, callAgree, postcode,mainAddress,detailAddress,content, imagesPath,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
//      pst.setInt(1, estimateDto.getUserSeq());
//      pst.setString(2, estimateDto.getName());
//      pst.setString(3, estimateDto.getPhone());
//      pst.setString(4, estimateDto.getEmail());
//      pst.setBoolean(5, estimateDto.isEmailAgree());
//      pst.setBoolean(6, estimateDto.isSmsAgree());
//      pst.setBoolean(7, estimateDto.isCallAgree());
//      pst.setString(8, estimateDto.getPostcode());
//      pst.setString(9, estimateDto.getMainAddress());
//      pst.setString(10, estimateDto.getDetailAddress());
//      pst.setString(11, estimateDto.getContent());
//      pst.setString(12, estimateDto.getImagesPath());
//      pst.setString(13, estimateDto.getStatus().name());
//      pst.executeUpdate();
//      try (ResultSet rs = pst.getGeneratedKeys();) {
//        if (rs.next()) {
//          return rs.getInt(1);
//        }
//      }
//    }
//    return 0;
//  }
//  
//  public Optional<EstimateDto> getEstimateByEstimateSeq(int estimateSeq) {
//    String sql = "SELECT * FROM `estimate` WHERE `estimate_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, estimateSeq);
//      try (ResultSet rs = pst.executeQuery()) {
//        if (rs.next()) {
//          EstimateDto estimate = mapEstimateDto(rs);
//          return Optional.of(estimate);
//        }
//      }
//    }
//    return Optional.empty();
//  }
//  
//  public int updateEstimate(EstimateDto estimateDto)  {
//    String sql = "UPDATE `estimate` SET name=?, phone=?, email=?, emailAgree=?, smsAgree=?, callAgree=?, postcode=?, mainAddress=?, detailAddress=?, content=?, imagesPath=?, status=?, updated_at = NOW() WHERE estimate_seq = ?";
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);){
//      pst.setString(1, estimateDto.getName());
//      pst.setString(2, estimateDto.getPhone());
//      pst.setString(3, estimateDto.getEmail());
//      pst.setBoolean(4, estimateDto.isEmailAgree());
//      pst.setBoolean(5, estimateDto.isSmsAgree());
//      pst.setBoolean(6, estimateDto.isSmsAgree());
//      pst.setString(7, estimateDto.getPostcode());
//      pst.setString(8, estimateDto.getMainAddress());
//      pst.setString(9, estimateDto.getDetailAddress());
//      pst.setString(10, estimateDto.getContent());
//      pst.setString(11, estimateDto.getImagesPath());
//      pst.setString(12, estimateDto.getStatus().name());
//      pst.setInt(13, estimateDto.getEstimateSeq());
//      return pst.executeUpdate();
//    }
//  }
//  
//  public int deleteEstimate(int estimateSeq)  {
//    String sql = "UPDATE `estimate` SET `status` = ?, `updated_at` = NOW() WHERE `estimate_seq` = ?";
//    try(
//        Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);
//        ){
//      pst.setString(1, EstimateDto.Status.DELETE.name());
//      pst.setInt(2, estimateSeq);
//      return pst.executeUpdate();
//    }
//  }
//  
//  public List<EstimateDto> getEstimateByUserSeq(int userSeq)  {
//    String sql = "SELECT * FROM `estimate` WHERE `user_seq` = ? ORDER BY `created_at` DESC";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, userSeq);
//      try (ResultSet rs = pst.executeQuery();) {
//        ArrayList<EstimateDto> list = new ArrayList<>();
//        while(rs.next()) {
//          list.add(mapEstimateDto(rs));
//        }
//        return list;
//      }
//    }
//  }
//  
//  
//  public int getCountByStatus(String status)  {
//    String sql;
//    if ("ALL".equals(status)) {
//      sql = "SELECT COUNT(*) FROM `estimate` WHERE `status` IN ('RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'DELETE')";
//    } else {
//      sql = "SELECT COUNT(*) FROM `estimate` WHERE `status` = ?";
//    }
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql)) {
//      if (!"ALL".equals(status)) {
//        pst.setString(1, status);
//      }
//      
//      try (ResultSet rs = pst.executeQuery()) {
//        if (rs.next()) {
//          return rs.getInt(1);
//        }
//      }
//    }
//    return 0;
//  }
//  
//  
//  public Optional<String> getImagesPath(int estimateSeq)  {
//    String sql = "SELECT `imagesPath` FROM `estimate` WHERE `estimate_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, estimateSeq);
//      try (ResultSet rs = pst.executeQuery()) {
//        if (rs.next()) {
//          String imagesPath = rs.getString("imagesPath"); 
//          return Optional.of(imagesPath);
//        }
//      }
//    }
//    return Optional.empty();
//  }
//  
//  public List<EstimateDto> getEstimateSearch(EstimateSearchRequest reqEstimateDto)  {
//    
//    String sql = buildEstimateSearchQuery(reqEstimateDto);
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql)){
//      try(ResultSet rs = pst.executeQuery()){ 
//        ArrayList<EstimateDto> list = new ArrayList<>();
//        while(rs.next()) {
//          list.add(mapEstimateDto(rs));
//        }
//        return list;
//      }
//    }
//  }
//  
//  public int getCountEstimateSearch(EstimateSearchRequest reqEstimateDto)  {
//    
//    String sql = buildEstimateSearchQuery(reqEstimateDto);
//    sql=sql.replaceFirst("SELECT \\*", "SELECT COUNT(*)");
//    if(sql.contains("LIMIT")) {
//      sql = sql.substring(0, sql.indexOf("LIMIT")).trim() + ";";
//    }
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);
//        ResultSet rs = pst.executeQuery()){
//      if(rs.next()) {
//        return rs.getInt(1);
//      }
//      return 0;
//    }
//  }
//  
//  public List<EstimateDto> getEstimateByStatus(int chepter, Status status)  {
//    
//    String sql = "SELECT * FROM estimate WHERE status = ? ORDER BY created_at DESC LIMIT 50 OFFSET "+ chepter * 50;
//    
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql)) {
//      pst.setString(1, status.name());
//      try (ResultSet rs = pst.executeQuery()) {
//        ArrayList<EstimateDto> list = new ArrayList<>();
//        while(rs.next()) {
//          list.add(mapEstimateDto(rs));
//        }
//        return list;
//      }
//    }
//  }
//  
//  public String buildEstimateSearchQuery(EstimateSearchRequest request) {
//    // 기본 SQL 시작 부분
//    StringBuilder sql = new StringBuilder("SELECT * FROM estimate WHERE 1=1");
//    
//    // 상태 조건 추가
//    if (request.getStatus() != null && request.getStatus() != EstimateSearchRequest.Status.ALL) {
//      sql.append(" AND status = '").append(request.getStatus().name()).append("'");
//    }
//    
//    // 기간 조건 추가
//    if (request.getPeriodType() != null) {
//      switch (request.getPeriodType()) {
//        case ALL:
//          break;
//        case TODAY:
//          sql.append(" AND DATE(created_at) = CURDATE()");
//          break;
//        case DAYS7:
//          sql.append(" AND created_at >= NOW() - INTERVAL 7 DAY");
//          break;
//        case DAYS30:
//          sql.append(" AND created_at >= NOW() - INTERVAL 30 DAY");
//          break;
//        case MONTHLY:
//          if (request.getYear() != null && request.getMonth() != null) {
//            String formattedMonth = String.format("%02d", Integer.parseInt(request.getMonth())); // 앞에 0을 붙여 두 자리로 변환
//            sql.append(" AND DATE_FORMAT(created_at, '%Y-%m') = '")
//            .append(request.getYear()).append("-").append(formattedMonth).append("'");
//          }
//          break;
//        case RANGE:
//          if (request.getStartDate() != null && request.getEndDate() != null) {
//            sql.append(" AND created_at BETWEEN '")
//            .append(request.getStartDate()).append("' AND '").append(request.getEndDate()).append("'");
//          }
//          break;
//      }
//    }
//    
//    // 검색 조건 추가
//    if (request.getSearchType() != null && request.getSearchWords() != null && !request.getSearchWords().trim().isEmpty()) {
//      switch (request.getSearchType()) {
//        case ADDRESS:
//          sql.append(" AND mainAddress LIKE '%").append(request.getSearchWords()).append("%'")
//          .append(" OR detailAddress LIKE '%").append(request.getSearchWords()).append("%'");
//          break;
//        case EMAIL:
//          sql.append(" AND email = '").append(request.getSearchWords()).append("'");
//          break;
//        case ESTIMATE_SEQ:
//          sql.append(" AND estimate_seq = ").append(request.getSearchWords());
//          break;
//        case NAME:
//          sql.append(" AND name LIKE '%").append(request.getSearchWords()).append("%'");
//          break;
//        case PHONE:
//          sql.append(" AND phone = '").append(request.getSearchWords()).append("'");
//          break;
//      }
//    }
//    
//    // 정렬 조건 추가
//    if (request.getSortType() != null) {
//      sql.append(" ORDER BY created_at ").append(request.getSortType().name());
//    } else {
//      // 기본 정렬: 최신순
//      sql.append(" ORDER BY created_at DESC");
//    }
//    
//    // 페이징 조건 추가
//    if (request.getPage() > 0 && request.getSize() > 0) {
//      int offset = (request.getPage() - 1) * request.getSize();
//      sql.append(" LIMIT ").append(offset).append(", ").append(request.getSize());
//    }
//    
//    // 최종 SQL 반환
//    return sql.toString();
//  }
//  
//  private EstimateDto mapEstimateDto(ResultSet rs)  {
//    Timestamp createdTs = rs.getTimestamp("created_at");
//    Timestamp updatedTs = rs.getTimestamp("updated_at");
//    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
//    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
//    EstimateDto estimate = new EstimateDto(
//        rs.getInt("estimate_seq"),
//        rs.getInt("user_seq"),
//        rs.getString("name"),
//        rs.getString("phone"),
//        rs.getString("email"),
//        rs.getBoolean("emailAgree"),
//        rs.getBoolean("smsAgree"),
//        rs.getBoolean("callAgree"),
//        rs.getString("postcode"),
//        rs.getString("mainAddress"),
//        rs.getString("detailAddress"),
//        rs.getString("content"),
//        rs.getString("imagesPath"),
//        Status.valueOf(rs.getString("status")),
//        createdAt,
//        updatedAt
//        );
//    
//    return estimate;
//  }


}
