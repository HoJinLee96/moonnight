package auth.login.token.filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import auth.login.token.GuestUserDetails;
import global.exception.IllegalJwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtVerifyPhoneFilter extends AbstractJwtFilter<GuestUserDetails> {
  
  @Override
  protected GuestUserDetails buildUserDetails(Map<String, Object> claims) {
    Object subjectRaw = claims.get("subject");
    if (subjectRaw == null) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - subject");
    }
    int verificationId = Integer.parseInt(subjectRaw.toString());
    if(verificationId==0) {
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
    
    String phone = (String) claims.get("phone");
    if (phone == null || phone.isEmpty()) {
      throw new IllegalJwtException("유효하지 않은 JWT 입니다. - phone");
    }
    
    
    return new GuestUserDetails(verificationId, phone, authorities);
  }

}
