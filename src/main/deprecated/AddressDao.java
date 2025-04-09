package deprecated;

//@Repository
public class AddressDao 
//extends BaseDao<AddressRequestDto>
{
  
//  public AddressDao(JdbcTemplate jdbcTemplate) {
//    super(jdbcTemplate);
//  }
//  
//  public int registerAddressDto(AddressRequestDto addressDto) {
//    String sql = "INSERT INTO `address` (user_seq, name, postcode, main_address, detail_address,is_primary) VALUES (?, ?, ?, ?, ?, ?)";
//    return register(sql, addressDto.getUserSeq(), addressDto.getName(), addressDto.getPostcode(), addressDto.getMainAddress(), addressDto.getDetailAddress(),addressDto.isPrimary());
//  }
//
//  public AddressRequestDto getAddressDtoByAddressSeq(int addressSeq) {
//    return getDtoBySequence(addressSeq);
//  }
//  
//  public List<AddressRequestDto> getAddressListByUserSeq(int userSeq) {
//    String sql = "SELECT * FROM `address` WHERE `user_seq` = ? ORDER BY `is_primary` DESC, COALESCE(`updated_at`, `created_at`) DESC";
//    List<AddressRequestDto> list = jdbcTemplate.query(sql, new AddressDtoRowMapper(), userSeq);
//    if(list.size()<1) {
//      throw new NoSuchElementException("조회 불가 : 일치한 행 없음.(userSeq : " + userSeq + ")");
//    }
//    return list;
//  }
//  
//  public void updateAddress(AddressRequestDto addressDto) {
//    String sql = "UPDATE `address` SET `name` = ?, `postcode` = ?, `main_address` = ?, `detail_address` = ?, `updated_at` = NOW() WHERE `address_seq` = ?";
//    update(sql, 
//        addressDto.getName(),
//        addressDto.getPostcode(), 
//        addressDto.getMainAddress(), 
//        addressDto.getDetailAddress(), 
//        addressDto.getAddressSeq()
//    );
//  }
//  
//  public void updatePrimaryAddress(int userSeq, int addressSeq) {
//    String sql =
//        "UPDATE `address` "
//        + "SET `is_primary` = "
//          + "CASE "
//            + "WHEN `address_seq` = ? THEN TRUE "
//            + "ELSE FALSE "
//          + "END, "
//          + "`updated_at` = NOW() "
//        + "WHERE `user_seq` = ?";
//    int affectedRows = jdbcTemplate.update(sql, addressSeq, userSeq);
//    if (affectedRows < 2) throw new NoSuchElementException("수정 불가 : 일치한 행 없음. (userSeq : " + userSeq + ", addressSeq : " + addressSeq +")");
//  }
//
//  public void deleteAddressDto(int addressSeq) {
//    String sql = "DELETE FROM `address` WHERE `address_seq` = ?";
//    update(sql, addressSeq);
//  }
//
//
//  class AddressDtoRowMapper implements RowMapper<AddressRequestDto> {
//      @Override
//      public AddressRequestDto mapRow(ResultSet rs, int rowNum) throws SQLException {
//        Timestamp updatedTs = rs.getTimestamp("updated_at");
//        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
//        
//        return new AddressRequestDto(
//            rs.getInt("address_seq"),
//            rs.getInt("user_seq"),
//            rs.getString("name"),
//            rs.getString("postcode"),
//            rs.getString("main_address"),
//            rs.getString("detail_address"),
//            rs.getBoolean("is_primary"),
//            rs.getTimestamp("created_at").toLocalDateTime(),
//            updatedAt
//        );
//    }
//}
//
//  @Override
//  protected RowMapper<AddressRequestDto> getRowMapper() {
//    return new AddressDtoRowMapper();
//  }
//
//  @Override
//  protected String getTableName() {
//    return "address";
//  }
//
//  @Override
//  protected String getPrimaryKeyColumn() {
//    return "address_seq";
//  }


//  public int registerAddress(AddressDto addressDto) throws SQLException {
//    String sql = "INSERT INTO `address` (user_seq, name, postcode, main_address, detail_address) VALUES (?, ?, ?, ?, ?)";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);){
//      pst.setInt(1, addressDto.getUserSeq());
//      pst.setString(2, addressDto.getName());
//      pst.setString(3, addressDto.getPostcode());
//      pst.setString(4, addressDto.getMainAddress());
//      pst.setString(5, addressDto.getDetailAddress());
//      pst.executeUpdate();
//      try(ResultSet rs = pst.getGeneratedKeys();){
//        if (rs.next()) {
//          return rs.getInt(1);
//        }
//      }
//    }
//    return 0;
//  }
//  
//  public int updateAddress(AddressDto addressDto) throws SQLException {
//    String sql = "UPDATE `address` SET `name` = ?, `postcode` = ?, `main_address` = ?,  `detail_address` = ?, `updated_at` = ? WHERE `address_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);
//        ) {
//      pst.setString(1, addressDto.getName());
//      pst.setString(2, addressDto.getPostcode());
//      pst.setString(3, addressDto.getMainAddress());
//      pst.setString(4, addressDto.getDetailAddress());
//      pst.setInt(5, addressDto.getAddressSeq());
//      return pst.executeUpdate();
//    }
//  }
//  
//  public int deleteAddress(int addressSeq) throws SQLException {
//    String sql = "DELETE FROM `address` WHERE `address_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);
//        ) {
//      pst.setInt(1, addressSeq);
//      return pst.executeUpdate();
//    }
//  }
//  
//  public Optional<AddressDto> getAddressDtoByAddressSeq(int addressSeq) throws SQLException {
//    String sql = "SELECT * FROM `address` WHERE `address_seq` = ?";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//        pst.setInt(1, addressSeq);
//        try (ResultSet rs = pst.executeQuery()) {
//          if (rs.next()) {
//            AddressDto addressDto = mapAddressDto(rs);
//            return Optional.of(addressDto);
//          }
//        }
//    }
//    return Optional.empty();
//  }
//
//  public List<AddressDto> getAddressListByUserSeq(int userSeq) throws SQLException {
//    List<AddressDto> list = new ArrayList<>();
//    String sql = "SELECT * FROM `address` WHERE `user_seq`= ? ORDER BY `updated_at` DESC";
//    try (Connection con = dataSource.getConnection();
//        PreparedStatement pst = con.prepareStatement(sql);) {
//      pst.setInt(1, userSeq);
//      try (ResultSet rs = pst.executeQuery()) {
//        while (rs.next()) {
//          list.add(mapAddressDto(rs));
//        }
//      }
//    }
//    return list;
//  }
//
//  private AddressDto mapAddressDto(ResultSet rs) throws SQLException {
//    Timestamp createdTs = rs.getTimestamp("created_at");
//    Timestamp updatedTs = rs.getTimestamp("updated_at");
//    LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
//    LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;
//    AddressDto addressDto = 
//        new AddressDto(
//        rs.getInt("address_seq"),
//        rs.getInt("user_seq"),
//        rs.getString("name"),
//        rs.getString("postcode"),
//        rs.getString("main_address"),
//        rs.getString("detail_address"),
//        createdAt,
//        updatedAt
//    );
//    return addressDto;
//  }

}
