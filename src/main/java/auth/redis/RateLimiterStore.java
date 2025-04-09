package auth.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import global.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimiterStore {

  private final RedisTemplate<String, String> redisTemplate;
  private static final int MAX_VERIFY_REQUESTS = 5; // 최대 요청 횟수
  private static final int MAX_IP_REQUESTS = 20; // 최대 요청 횟수
  private static final long EXPIRATION_30MINUTE = 30; // 제한 시간 (분)

  
  // 휴대폰 인증 Rate Limit
  public boolean isAllowedByPhone(String phone) {
    return isAllowed("rate_limit:verify:phone:" + phone, EXPIRATION_30MINUTE, MAX_VERIFY_REQUESTS);
  }
  // 이메일 인증 Rate Limit
  public boolean isAllowedByEmail(String email) {
    return isAllowed("rate_limit:verify:email:" + email, EXPIRATION_30MINUTE, MAX_VERIFY_REQUESTS);
  }

  // 모든 요청 IP Rate Limit
  public boolean isAllowedByIp(String ip) {
      return isAllowed("rate_limit:ip:" + ip, EXPIRATION_30MINUTE, MAX_IP_REQUESTS);
  }

  private boolean isAllowed(String key, long timeOut, int maxCount) {
      ValueOperations<String, String> ops = redisTemplate.opsForValue();
      Long reqCount = ops.increment(key, 1); 

      if (reqCount == 1) {
          redisTemplate.expire(key, timeOut, TimeUnit.MINUTES); // 30분 TTL 설정
      }

    if(reqCount > maxCount) // 허용된 최대 요청 횟수 초과 여부 반환
      throw new TooManyRequestsException("요청 횟수를 초과했습니다. 잠시 후 다시 시도해 주세요.");
    
    return true;
  }
  
}