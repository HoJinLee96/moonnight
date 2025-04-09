package deprecated;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@PropertySource("classpath:application.properties")
public class NaverOAuthClient {

  @Value("${naver-login.baseUrl}")
  private String baseUrl;
  @Value("${naver-login.clientId}")
  private String clientId;
  @Value("${naver-login.redirectUrl}")
  private String redirectUrl;
  @Value("${naver-login.clientSecret}")
  private String clientSecret;
  
  @PostConstruct
  private void init() {
    System.out.println("naver-login.baseUrl : " + baseUrl);
    System.out.println("naver-login.clientId : " + clientId);
    System.out.println("naver-login.redirectUrl : " + redirectUrl);
    System.out.println("naver-login.clientSecret : " + clientSecret);
  }
  
  public String getNaverAuthorizeUrl() throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
    UriComponents uriComponents = UriComponentsBuilder
        .fromUriString(baseUrl + "/authorize")
        .queryParam("response_type", "code")
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", URLEncoder.encode(redirectUrl, "UTF-8"))
        .build();
    return uriComponents.toString();
  }
  
  public OAuthTokenDto getNaverAccessToken(String callbackCode) {
    UriComponents uriComponents =UriComponentsBuilder
        .fromUriString(baseUrl + "/token")
        .queryParam("grant_type", "authorization_code")
        .queryParam("client_id", clientId)
        .queryParam("client_secret", clientSecret)
        .queryParam("code", callbackCode)
        .build();
    
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<OAuthTokenDto> response = restTemplate.exchange(
        uriComponents.toUriString(),
        HttpMethod.GET,
        null, // 헤더 없음
        OAuthTokenDto.class
    );

    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
        throw new IllegalStateException("네이버 서버에서 토큰을 받아오지 못함.");
    }
    return response.getBody();
  }
  
  public OAuthTokenDto updateNaverAccessToken(OAuthTokenDto oAuthToken) {
    UriComponentsBuilder uriComponents =UriComponentsBuilder
        .fromUriString(baseUrl + "/token")
        .queryParam("grant_type", "refresh_token")
        .queryParam("client_id", clientId)
        .queryParam("client_secret", clientSecret)
        .queryParam("refresh_token", oAuthToken.getRefresh_token());
    
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<OAuthTokenDto> response = restTemplate.exchange(
        uriComponents.toUriString(), 
        HttpMethod.GET, 
        null, 
        OAuthTokenDto.class
        );
    
    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
      throw new IllegalStateException("네이버 서버에서 토큰을 받아오지 못함.");
    }
    return response.getBody();
  }

  
//회원 정보 받기
  public NaverUserInfoResponseDto getNaverUserByToken(OAuthTokenDto token) {
    String accessToken = token.getAccess_token();
    String tokenType = token.getToken_type();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", tokenType + " " + accessToken);

    HttpEntity<Void> entity = new HttpEntity<>(headers);
    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<NaverUserInfoResponseDto> response = restTemplate.exchange(
        "https://openapi.naver.com/v1/nid/me",
        HttpMethod.GET,
        entity,
        NaverUserInfoResponseDto.class
    );

    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
        throw new IllegalStateException("네이버 사용자 정보를 받아오지 못함.");
    }

    return response.getBody();
  }
  
  
  
  
}
