package domain.estimate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Integer>{
  List<Estimate> findByUser_UserSeq(int userSeq);
  List<Estimate> findByPhone(String phone);

}
