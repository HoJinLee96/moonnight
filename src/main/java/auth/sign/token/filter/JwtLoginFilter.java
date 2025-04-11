package auth.sign.token.filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import auth.sign.token.CustomUserDetails;
import domain.user.User.UserProvider;
import global.exception.IllegalJwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtLoginFilter extends AbstractJwtFilter<CustomUserDetails> {

  @Override
  protected CustomUserDetails buildUserDetails(Map<String, Object> claims) {
    // 복호화 때문에 claims.getSbuject()가 아님.
    Object subjectRaw = claims.get("subject");
    if (subjectRaw == null) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - subject");
    }
    int userId = Integer.parseInt(subjectRaw.toString());
    if(userId==0) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - subject");
    }
    
    Object rolesObj = claims.get("roles");
    if (!(rolesObj instanceof List)) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - roles");
    }
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) rolesObj;
    List<GrantedAuthority> authorities =
        roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    String email = (String) claims.get("email");
    if (email == null || email.isEmpty()) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - email");
    }
    String providerStr = (String) claims.get("provider");
    if (providerStr == null || providerStr.isEmpty()) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - providerStr");
    }
    UserProvider userProvider = UserProvider.valueOf(providerStr); // 문자열을 Provider Enum으로 변환

    return new CustomUserDetails(userId, userProvider, email, authorities);
  }
}
