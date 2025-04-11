package global.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import auth.oauth.OAuth2LoginSuccessHandler;
import auth.sign.token.filter.JwtLoginFilter;
import auth.sign.token.filter.JwtVerifyPhoneFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity //spring security 활성화
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages={"auth","client"})
public class SecurityConfig {

    private final JwtLoginFilter jwtLoginFilter;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JwtVerifyPhoneFilter jwtVerifyPhoneFilter;
    
    @Value("${naver-login.clientId}")
    private String naverClientId;
    @Value("${naver-login.clientSecret}")
    private String naverClientSecret;
    @Value("${naver-login.redirectUri}")
    private String naverRedirectUri;
    
    @Value("${kakao.clientId}")
    private String kakaoClientId;
    @Value("${kakao-login.clientSecret}")
    private String kakaoClientSecret;
    @Value("${kakao-login.redirectUri}")
    private String kakaoRedirectUri;
  
    @Bean
    public PasswordEncoder passwordEncoder() {
//      비밀번호를 해시할 때 몇 번 반복해서 계산할지를 정하는 값
      int strength = 12; 
      return new BCryptPasswordEncoder(strength);
    }
    @Bean
    public SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/private/**") // 모든 API 경로에 대해 기본 설정 적용
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/private/spem/auth/**", "/api/private/estimate/auth/**").permitAll() // 별도 체인에서 인증 처리
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain phoneAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/private/spem/auth/**", "/api/private/estimate/auth/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtVerifyPhoneFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
          .securityMatcher("/oauth2/**", "/login/oauth2/**")
          .csrf(AbstractHttpConfigurer::disable)
          .oauth2Login(oauth2 -> oauth2
              .clientRegistrationRepository(clientRegistrationRepository())
              .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService()))
              .successHandler(oauth2LoginSuccessHandler))
          .build();
    }
//    
//    @Bean
//    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()
//            )
//            .csrf(AbstractHttpConfigurer::disable);
//
//        return http.build();
//    }

    
    // 사용자 정보를 커스터마이징 처리
    // OAuth2UserService는 OAuth 로그인 성공 후 AccessToken으로 유저 정보를 불러와 인증 객체를 만드는 커스터마이징 포인트.
    // 위 코드는 네이버처럼 구조가 다른 JSON 응답에도 대응하기 위한 커스텀 로직이 포함된 형태.
    @SuppressWarnings("unchecked")
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
      return userRequest -> {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        System.out.println("▶ OAuth2User attributes = " + oAuth2User.getAttributes());
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        if (Objects.equals("naver",registrationId)){
          Map<String, Object> attributes = oAuth2User.getAttributes();
          Map<String, Object> response = (Map<String, Object>) attributes.get("response");
          return new DefaultOAuth2User(
              Collections.singleton(new SimpleGrantedAuthority("ROLE_OAUTH")),
              response,
              "id"
          );
        } 
        return oAuth2User;
        };
    }
    
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            this.naverClientRegistration(),
            this.kakaoClientRegistration()
        );
    }
    
    private ClientRegistration naverClientRegistration() {
      return ClientRegistration.withRegistrationId("naver")
          .clientId(naverClientId)
          .clientSecret(naverClientSecret)
          .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri(naverRedirectUri)
          .scope("name", "email")
          .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
          .tokenUri("https://nid.naver.com/oauth2.0/token")
          .userInfoUri("https://openapi.naver.com/v1/nid/me")
          .userNameAttributeName("response") // 식별값
          .clientName("Naver")
          .build();
    }
    
    private ClientRegistration kakaoClientRegistration() {
      return ClientRegistration.withRegistrationId("kakao")
          .clientId(kakaoClientId)
          .clientSecret(kakaoClientSecret)
          .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri(kakaoRedirectUri)
          .scope("profile_nickname", "account_email")
          .authorizationUri("https://kauth.kakao.com/oauth/authorize")
          .tokenUri("https://kauth.kakao.com/oauth/token")
          .userInfoUri("https://kapi.kakao.com/v2/user/me")
          .userNameAttributeName("id")
          .clientName("Kakao")
          .build();
  }
  
}
