package deprecated;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public abstract class BaseDao<T> {

  protected final JdbcTemplate jdbcTemplate;

  protected BaseDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  protected abstract RowMapper<T> getRowMapper();

  protected abstract String getTableName();

  protected abstract String getPrimaryKeyColumn();

  protected int register(String sql, Object... params) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int affectedRows = jdbcTemplate.update(connection -> {
      PreparedStatement pst = connection.prepareStatement(sql, new String[] {getPrimaryKeyColumn()});
      for (int i = 0; i < params.length; i++) {
        pst.setObject(i + 1, params[i]);
      }
      return pst;
    }, keyHolder);
    if (affectedRows == 0) {
      throw new IllegalStateException("등록 불가: 등록된 행 없음.");
    }
    return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
  }
  
  // 일치하는 데이터가 없으면 익셉션 발생
  public T getDtoBySequence(int seq) {
    String sql = "SELECT * FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
    return jdbcTemplate.queryForObject(sql, getRowMapper(), seq);
  }
  
  public List<T> getAllDto() {
    String sql = "SELECT * FROM " + getTableName() + " ORDER BY `created_at` DESC";
    return jdbcTemplate.query(sql, getRowMapper());
  }
  
  protected void update(String updateSql, Object... params) {
    if (jdbcTemplate.update(updateSql, params) == 0) {
      throw new NoSuchElementException("수정 불가 : 변경된 행 없음.");
    }
  }
}
