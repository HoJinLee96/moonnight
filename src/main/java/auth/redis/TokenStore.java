package auth.redis;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import auth.crypto.AESProvider;
import global.exception.IllegalJwtException;
import global.exception.IllegalUuidException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenStore {

  private final AESProvider aesProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private final Logger logger = LoggerFactory.getLogger(TokenStore.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  
  public enum TokenType {
    VERIFICATION_EMAIL("verification:email:", Duration.ofMinutes(5)),
    VERIFICATION_PHONE("verification:phone:", Duration.ofMinutes(5)),
    ACCESS_FINDPW("access:findpw:", Duration.ofMinutes(10)),
    ACCESS_PASSWORD("access:password:", Duration.ofMinutes(10)),
    ACCESS_SIGNUP("access:signup:", Duration.ofMinutes(20)),
    JWT_REFRESH("jwt:refresh:",Duration.ofDays(14));

    private final String prefix;
    private final Duration ttl;

    TokenType(String prefix, Duration ttl) {
        this.prefix = prefix;
        this.ttl = ttl;
    }

    public String getPrefix() {
        return prefix;
    }

    public Duration getTtl() {
        return ttl;
    }
  }

  public String createMapToken(TokenType type, Map<String, String> map) {
    String token = UUID.randomUUID().toString();
    try {
      // 각 value에 AES 암호화 적용
      Map<String, String> encryptedMap = map.entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              entry -> aesProvider.encrypt(entry.getValue())
        ));
  
      String json = objectMapper.writeValueAsString(encryptedMap);
      redisTemplate.opsForValue().set(type.getPrefix() + token, json, type.getTtl());
      
    } catch (JsonProcessingException e) {
      logger.error("[Redis 저장 실패] type={}, data={}", type.name(), map, e);
      throw new IllegalUuidException("토큰 생성 중 직렬화 오류");
    }
    
    logger.info("[Redis 토큰 생성 및 저장] type={}, token={}", type.name(), token);
    return token;
  }

  public String createVerificationEmailToken(String email) {
    return createToken(TokenType.VERIFICATION_EMAIL, email);
  }

  public String createVerificationPhoneToken(String phone) {
    return createToken(TokenType.VERIFICATION_PHONE, phone);
  }
  
  public String createAccessFindPwToken(String email) {
  return createToken(TokenType.ACCESS_FINDPW, email);
  }
  
  public String createAccessPaaswordToken(String email) {
    return createToken(TokenType.ACCESS_PASSWORD, email);
  }
  
  public void addRefreshJwt(int userId, String refreshToken) {
    redisTemplate.opsForValue().set(TokenType.JWT_REFRESH.getPrefix() + userId, refreshToken, TokenType.JWT_REFRESH.getTtl());
  }
  public void addJwtBlacklist(String accessToken, long ttl, String result) {
    redisTemplate.opsForValue().set("jwt:blacklist:"+ accessToken, result, Duration.ofMillis(ttl));
  }
  
  private String createToken(TokenType type, String value) {
    String token = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(type.getPrefix() + token, aesProvider.encrypt(value), type.getTtl());
    logger.info("[Redis 토큰 생성 및 저장] type={}, token={}", type.name(), token);
    return token;
  }
  
  public Map<String, String> getMapTokenData(TokenType type, String token) {
    String key = type.getPrefix() + token;
    String json = redisTemplate.opsForValue().get(key);
    
    if (json == null) {
      logger.warn("[Redis 토큰 조회 실패] type={}, token={}", type.name(), token);
      throw new IllegalUuidException("시간이 만료됐거나 잘못된 요청입니다.\n다시 시도해 주세요.");
    }
  
    try {
      Map<String, String> encryptedMap = objectMapper.readValue(json, new TypeReference<>() {});
      
      // value 복호화 적용
      return encryptedMap.entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              entry -> aesProvider.decrypt(entry.getValue())
        ));
      
    } catch (JsonProcessingException e) {
      logger.error("[Redis 토큰 조회 실패] type={}, token={}", type.name(), token, e);
      throw new IllegalUuidException("토큰 역직렬화 오류");
    }
  }
  

  public String getVerificationPhone(String token) {
    return getTokenData(TokenType.VERIFICATION_PHONE, token)
        .orElseThrow(()->{
          logger.info("존재하지 않는 토큰 token: {} ", token);
          throw new IllegalUuidException("시간이 만료됐거나 잘못된 요청입니다.\n다시 시도해 주세요.");
        });
  }

  public String getVerificationEmail(String token) {
    return getTokenData(TokenType.VERIFICATION_EMAIL, token)
        .orElseThrow(()->{
          logger.info("존재하지 않는 토큰 token: {}  ", token);
          throw new IllegalUuidException("시간이 만료됐거나 잘못된 요청입니다.\n다시 시도해 주세요.");
        });
  }

  public String getAccessFindpwToken(String token) {
  return getTokenData(TokenType.ACCESS_FINDPW, token)
      .orElseThrow(()->{
        logger.info("존재하지 않는 토큰 token: {} ", token);
        throw new IllegalUuidException("시간이 만료됐거나 잘못된 요청입니다.\n다시 시도해 주세요.");
      });
  }
  
  public String getAccessPasswordToken(String token) {
  return getTokenData(TokenType.ACCESS_PASSWORD, token)
      .orElseThrow(()->{
        logger.info("존재하지 않는 토큰 token: {} ", token);
        throw new IllegalUuidException("시간이 만료됐거나 잘못된 요청입니다.\n다시 시도해 주세요.");
      });
  }
  
  public String getRefreshJwt(String token) {
    String key = TokenType.JWT_REFRESH + token;
    String value = redisTemplate.opsForValue().get(key);
    if (value != null) {
      redisTemplate.delete(key);
      return aesProvider.decrypt(value);
    }
    logger.info("존재하지 않는 토큰 token: {}", token);
    throw new IllegalJwtException("잘못된 요청입니다.");
  }
  
  public boolean isBlackList(String accessToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + accessToken));
  }
  
  private Optional<String> getTokenData(TokenType type, String token) {
    String key = type.getPrefix() + token;
    String value = redisTemplate.opsForValue().get(key);
    if(value==null) {
      return Optional.empty();
    }
    return Optional.of(aesProvider.decrypt(value));
  }
  
  public boolean removeToken(TokenType type, String key) {
    return redisTemplate.delete(type.getPrefix() + key);
  }

  public boolean isValid(TokenType type, String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(type.getPrefix() + key));
  }

}