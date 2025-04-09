package domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import domain.user.User.UserProvider;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
  Optional<User> findByUserProviderAndEmail(UserProvider userProvider, String email);
  Optional<User> findByUserProviderAndPhone(UserProvider userProvider, String phone);
  Optional<User> findByUserProviderAndEmailAndPhone(UserProvider userProvider, String email, String phone);
  boolean existsByUserProviderAndEmail(UserProvider userProvider, String email);
  boolean existsByUserProviderAndPhone(UserProvider userProvider, String phone);

//  @Modifying(clearAutomatically = true)
//  @Query("UPDATE User u SET u.status = 'STAY' WHERE u.userProvider = :userProvider AND u.email = :email")
//  int updateStatusSetStayByEmailAndUserProvider(@Param("userProvider") UserProvider userProvider, @Param("email") String email);

}
