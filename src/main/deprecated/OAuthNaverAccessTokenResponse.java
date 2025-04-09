package deprecated;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class OAuthNaverAccessTokenResponse {

  private final String access_token;
  private final String refresh_token;
  private final String token_type;
  private final String expires_in;
  private final String error;
  private final String error_description;
  private final String result;
}
