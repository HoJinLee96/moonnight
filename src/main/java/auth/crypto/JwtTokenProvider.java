package auth.crypto;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import global.exception.ExpiredException;
import global.exception.IllegalJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
@PropertySource("classpath:application.properties")
public class JwtTokenProvider {
  
  private final AESProvider aesUtil;
  private final Key loginHmacShaKey;
  private final Key verifyPhoneHmacShaKey;
  
  public JwtTokenProvider(
      @Autowired AESProvider aesUtil,
      @Value("${jwt.login.secretKey}") String loginSecretKey,
      @Value("${jwt.verify.phone.secretKey}") String verifyPhoneSecretKey
  ) {
      this.aesUtil = aesUtil;
      this.loginHmacShaKey = Keys.hmacShaKeyFor(loginSecretKey.getBytes(StandardCharsets.UTF_8));
      this.verifyPhoneHmacShaKey = Keys.hmacShaKeyFor(verifyPhoneSecretKey.getBytes(StandardCharsets.UTF_8));
  }
  
  private final long expiration14Days = 1000 * 60 * 60 * 24 * 14; // 14
  private final long expiration1Hour = 1000 * 60 * 60; // 1시간
  private final long expiration10Minute = 1000 * 60 * 10; // 10분
  
  public Map<String,String> createLoginToken(int userId, List<String> roles, Map<String, Object> claims) {
    String accessToken = createAccessToken(userId, roles, claims);
    String refreshToken = createRefreshToken(userId);
    return Map.of("accessToken",accessToken,"refreshToken",refreshToken);
  }

  public String createAccessToken(int userId, List<String> roles, Map<String, Object> claims) {
    JwtBuilder builder = Jwts.builder()
        .setSubject(aesUtil.encrypt(userId + ""))
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration1Hour))
        .signWith(loginHmacShaKey, SignatureAlgorithm.HS256);
    
    if (claims != null) {
      for (Map.Entry<String, Object> entry : claims.entrySet()) {
          Object value = entry.getValue();
          if (value instanceof String strVal) {
              builder.claim(entry.getKey(), aesUtil.encrypt(strVal));
          } else {
              builder.claim(entry.getKey(), value); // 예외 방지
          }
      }
    }
    builder.claim("roles",roles);
  
    return builder.compact();
  }
  
  public String createRefreshToken(int userId) {
    return Jwts.builder()
        .setSubject(aesUtil.encrypt(userId + ""))
        .setExpiration(new Date(System.currentTimeMillis() + expiration14Days))
        .signWith(loginHmacShaKey, SignatureAlgorithm.HS256)
        .compact();
  }
  
  public String createVerifyPhoneToken(int verificationId, List<String> roles, Map<String, Object> claims) {

    JwtBuilder builder = Jwts.builder()
        .setSubject(aesUtil.encrypt(verificationId+"")) 
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration10Minute))
        .signWith(verifyPhoneHmacShaKey, SignatureAlgorithm.HS256);
    
    if (claims != null) {
      for (Map.Entry<String, Object> entry : claims.entrySet()) {
          Object value = entry.getValue();
          if (value instanceof String strVal) {
              builder.claim(entry.getKey(), aesUtil.encrypt(strVal));
          } else {
              builder.claim(entry.getKey(), value); // 예외 방지
          }
      }
    }
    
    builder.claim("roles",roles);
    
    return builder.compact();
  }
  
  public Map<String, Object> validateLoginToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(loginHmacShaKey)
          .build()
          .parseClaimsJws(token)
          .getBody();

      return getDecryptedClaims(claims);
    } catch (ExpiredJwtException e) {
      throw new ExpiredException("유효기간 만료.");
    } catch (JwtException | IllegalArgumentException e) {
      throw e;
    }
  }
  
  public String validateRefreshToken(String token) {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(loginHmacShaKey)
          .build()
          .parseClaimsJws(token)
          .getBody();

      String encryptedUserId = claims.getSubject(); 
      return aesUtil.decrypt(encryptedUserId);
  }
  
  public Map<String,Object> validateVerifyPhoneToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(verifyPhoneHmacShaKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
      
      return getDecryptedClaims(claims);
    } catch (ExpiredJwtException e) {
      throw new ExpiredException("TOKEN_EXPIRED");
    } catch (JwtException | IllegalArgumentException e) {
      throw new IllegalJwtException("TOKEN_INVALID");
    }
  }
  
  // 복호화
  private Map<String,Object> getDecryptedClaims(Claims claims) {
    Map<String, Object> result = new HashMap<>();
    result.put("subject",aesUtil.decrypt(claims.getSubject()));
    claims.forEach((k, v) -> {
        if ("roles".equals(k)) {
          result.put(k, v);
        }else {
        result.put(k, aesUtil.decrypt((String)v));
        }
    });
    return result;
  }
  
  public long getLoginJwtRemainingTime(String token) {
    try {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(loginHmacShaKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis(); // milliseconds
    } catch (JwtException e) {
      throw new IllegalJwtException("TOKEN_INVALID"); 
    }
  }
  
}
//  public String createVerifyPhoneToken(String phone) {
//    return Jwts.builder()
//        .setSubject(phone) 
//        .setIssuedAt(new Date())
//        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
//        .signWith(SignatureAlgorithm.HS256, loginSecretKey)
//        .compact();
//  }
//  
//  public Claims validateVerifyPhoneToken(String token) {
//    try {
//      return Jwts.parser()
//          .setSigningKey(verifyPhoneSecretKey)
//          .parseClaimsJws(token)
//          .getBody();
//    } catch (JwtException | IllegalArgumentException e) {
//      return null;
//    }
//  }
//  
//  public String createVerifyEmailToken(String email) {
//    return Jwts.builder()
//        .setSubject(email) 
//        .setIssuedAt(new Date())
//        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
//        .signWith(SignatureAlgorithm.HS256, verifyEmailSecretKey)
//        .compact();
//  }
//  
//  public Claims validateVerifyEmailToken(String token) {
//    try {
//      return Jwts.parser()
//          .setSigningKey(verifyEmailSecretKey)
//          .parseClaimsJws(token)
//          .getBody();
//    } catch (JwtException | IllegalArgumentException e) {
//      return null;
//    }
//  }

//  public String createLoginToken(Authentication authentication) {
//    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//    return Jwts.builder()
//        .setSubject(userDetails.getEmail()) // 사용자 식별자 (email)
//        .claim("name", userDetails.getName()) // 추가 정보 (이름)
//        .claim("provider", userDetails.getUserProvider().name()) // 사용자 제공자 정보
//        .claim("status", userDetails.getUserStatus().name()) // 사용자 상태 정보
//        .claim("roles", userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList())) // 권한 목록
//        .setIssuedAt(new Date()) // 발급 시간
//        .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간
//        .signWith(SignatureAlgorithm.HS256, secretKey) // 서명
//        .compact();
//  }