package domain.estimate.simple;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import domain.estimate.Estimate;

@Repository
public interface SimpleEstimateRepository extends JpaRepository<SimpleEstimate, Integer>{
  List<Estimate> findByUser_UserSeq(int userId);
  List<SimpleEstimate> findByPhone(String phone);

}
