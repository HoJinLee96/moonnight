package domain.address;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import domain.user.User;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer>{
  List<Address> findByUser(User user);
  
  @Query("SELECT a FROM Address a WHERE a.user.userSeq = :userSeq ORDER BY a.isPrimary DESC, COALESCE(a.updatedAt, a.createdAt) DESC")
  List<Address> findByUserOrderByPrimaryAndDate(@Param("userSeq") int userSeq);
  
  @Modifying
  @Query("UPDATE Address a SET a.isPrimary = " +
         "CASE WHEN a.addressSeq = :addressSeq THEN TRUE ELSE FALSE END, " +
         "a.updatedAt = CURRENT_TIMESTAMP WHERE a.user.userSeq = :userSeq")
  int updatePrimaryAddress(@Param("addressSeq") int addressSeq, @Param("userSeq") int userSeq);
}
