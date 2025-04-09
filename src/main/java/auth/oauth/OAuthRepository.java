package auth.oauth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import auth.oauth.OAuth.OAuthProvider;

@Repository
public interface OAuthRepository extends JpaRepository<OAuth, Integer>{
  Optional<OAuth> findByOauthProviderAndId(OAuthProvider oauthProvider, String id);

}
