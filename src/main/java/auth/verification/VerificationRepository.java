package auth.verification;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Integer>{
  
  // 최신 Verification 조회 (to 기준, 제일 최근에 요청한)
  Optional<Verification> findTopByToOrderByCreatedAtDesc(String to);
  
  // 특정 verification_seq가 인증되었는지 확인
  boolean existsByVerificationSeqAndVerifyTrue(int verificationSeq);
  
  //  (to 기준, 10분 이내 요청한, 제일 최근에 요청한)
//  @Query("SELECT v FROM Verification v " +
//      "WHERE v.to = :to " +
//      "AND v.createdAt >= CURRENT_TIMESTAMP - INTERVAL 10 MINUTE" +
//      "ORDER BY v.createdAt DESC " +
//      "LIMIT 1")
//  Optional<Verification> findRecentVerificationWithin10Min(@Param("to") String to);

  // verify 상태 및 verify_at 업데이트
  @Modifying
  @Query(value = "UPDATE verification SET verify = TRUE, verify_at = NOW() WHERE verification_seq = :verSeq", nativeQuery = true)
  void markAsVerified(@Param("verSeq") int verSeq);

  // 인증 요청이 최근 5분 이내인지 확인
//  @Query("SELECT COUNT(v) > 0 FROM Verification v WHERE v.verificationSeq = :verSeq AND v.createdAt >= CURRENT_TIMESTAMP - INTERVAL 5 MINUTE")
//  boolean isWithinVerificationTime(@Param("verSeq") int verSeq);

  //  (to 기준, 10분 이내 요청한, 제일 최근에 요청한)
  @Query(
      value = "SELECT * FROM verification " +
              "WHERE `to` = :to " +
              "AND created_at >= CURRENT_TIMESTAMP - INTERVAL 10 MINUTE " +
              "ORDER BY created_at DESC " +
              "LIMIT 1",
      nativeQuery = true
    )
    Optional<Verification> findRecentVerificationWithin10Min(@Param("to") String to);
  
  // 인증 요청이 최근 5분 이내인지 확인
  @Query(
      value = "SELECT COUNT(*) > 0 FROM verification WHERE verification_seq = :verSeq AND created_at >= CURRENT_TIMESTAMP - INTERVAL 5 MINUTE",
      nativeQuery = true
    )
    boolean isWithinVerificationTime(@Param("verSeq") int verSeq);
  
}
