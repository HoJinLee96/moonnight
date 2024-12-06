package service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip) {
        String key = "req_count_" + ip;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Long reqCount = ops.increment(key,1);
        if (reqCount == 1) {
            redisTemplate.expire(key, 30, TimeUnit.MINUTES); // 30분 동안 유효
        }
        return reqCount <= 10; // 30분 동안 최대 10회 요청 허용
    }
}