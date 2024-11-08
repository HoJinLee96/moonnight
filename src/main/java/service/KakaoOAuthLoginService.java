package service;

import java.io.IOException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import dtoKakaoLogin.KakaoUserInfoResponseDto;
import dtoNaverLogin.OAuthToken;
import exception.NotFoundException;

@Service
@PropertySource("classpath:application.properties")
public class KakaoOAuthLoginService {
  @Value("${kakao-login.baseUrl}")
  private String baseUrl;
  @Value("${kakao.clientId}")
  private String clientId;
  @Value("${kakao-login.redirectUrl}")
  private String redirectUrl;
  @Value("${kakao-login.clientSecret}")
  private String clientSecret;
  @Value("${kakao-login.redirectUri}")
  private String redirectUri;
  
  @PostConstruct
  private void init() {
      System.out.println("kakao-login.baseUrl : " + baseUrl);
      System.out.println("kakao.clientId : " + clientId);
      System.out.println("kakao-login.redirectUrl : " + redirectUrl);
      System.out.println("kakao-login.clientSecret : " + clientSecret);
      System.out.println("kakao-login.redirectUri : " + redirectUri);
  }
  
// 인가 코드 받기
  public String getKakaoLoginUrl(String path) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
System.out.println("KakaoOAuthLoginService.getKakaoAuthorizeUrl()");
    UriComponents uriComponents = UriComponentsBuilder
            .fromUriString(baseUrl + "/" + path)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", URLEncoder.encode(redirectUrl, "UTF-8"))
            .queryParam("response_type", "code")
            .build();

    return uriComponents.toString();
}

// 접근 토큰 받기
public String getKakaoToken(String path, String code) throws NotFoundException {
  HttpHeaders headers = new HttpHeaders();
  headers.add("Content-Type", "application/json;charset=utf-8");
  
  UriComponents uriComponents = UriComponentsBuilder
          .fromUriString(baseUrl + "/" + path)
          .queryParam("grant_type", "authorization_code")
          .queryParam("client_id", clientId)
          .queryParam("redirect_uri",redirectUri )
          .queryParam("code", code)
          .build();

      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
          uriComponents.toUriString(), 
          HttpMethod.GET, 
          entity, 
          String.class
          );
      
      int responseCode = response.getStatusCodeValue();
      String responseBody = response.getBody();
      
      System.out.println("responseCode = " + responseCode);
      if(responseCode==200) {
        return responseBody;
      }else {
        throw new NotFoundException("카카오톡 서버에서 받아오지 못함.");
      }
}

// 회원 정보 받기
public KakaoUserInfoResponseDto getKakaoUserByToken(OAuthToken token) throws IOException, NotFoundException {
  System.out.println("KakaoOAuthLoginService.getKakaoUserByToken() 실행");
  System.out.println("token.getAccess_token() = " + token.getAccess_token());
  
  HttpHeaders headers = new HttpHeaders();
  headers.add("Authorization", "Bearer "+token.getAccess_token());
  headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
  UriComponents uriComponents = UriComponentsBuilder
          .fromUriString("https://kapi.kakao.com/v2/user/me")
          .build();

      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
          uriComponents.toUriString(), 
          HttpMethod.GET, 
          entity, 
          KakaoUserInfoResponseDto.class
          );
      
      int responseCode = response.getStatusCodeValue();
      KakaoUserInfoResponseDto kakaoUserInfoResponseDto = response.getBody();
      System.out.println("kakaoUserInfoResponseDto = " + kakaoUserInfoResponseDto.toString());
      System.out.println("responseCode = " + responseCode);
      if(responseCode==200) {
        return kakaoUserInfoResponseDto;
      }else {
        throw new NotFoundException("카카오톡 서버에서 받아오지 못함.");
      }
      
}

// 토큰 갱신
public String updateTokenUrl(String path,String grant_type,OAuthToken oAuthToken) throws IOException {
  System.out.println("KakaoOAuthLoginService.updateTokenUrl() 실행");
  HttpHeaders headers = new HttpHeaders();
  headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
  
  UriComponentsBuilder uriComponentsBuilder =UriComponentsBuilder
          .fromUriString(baseUrl + "/" + path)
          .queryParam("grant_type", grant_type)
          .queryParam("client_id", clientId);
  
  if(grant_type.equals("refresh_token")) {
    uriComponentsBuilder.queryParam("refresh_token", oAuthToken.getRefresh_token());
  }
//  else if(grant_type.equals("logout")){
//    uriComponentsBuilder.queryParam("access_token", URLEncoder.encode(oAuthToken.getAccess_token(),"UTF-8"));
//    uriComponentsBuilder.queryParam("service_provider", "KAKAO");
//  }
  else {
    throw new IOException("grant_type 잘못 입력");
  }
  
  
  UriComponents uriComponents = uriComponentsBuilder.build();
  RestTemplate restTemplate = new RestTemplate();
  HttpEntity<String> entity = new HttpEntity<>(headers);

  System.out.println("uriComponents.toUriString() = "+uriComponents.toUriString());
  ResponseEntity<String> response = restTemplate.exchange(
      uriComponents.toUriString(), 
      HttpMethod.POST, 
      entity, 
      String.class
      );
  System.out.println("response.getBody() = " + response.getBody());
  
  return response.getBody();

}

// path : 로그아웃(logout), 회원 탈퇴(unlink), 토큰 정보 조회(access_token_info), 배송지 조회(shipping_address) 등
public String executeKakaoUser(String path, OAuthToken oAuthToken) throws IOException {
  System.out.println("KakaoOAuthLoginService.deleteTokenUrl() 실행");
  
  HttpHeaders headers = new HttpHeaders();
  headers.add("Authorization", "Bearer "+oAuthToken.getAccess_token());
  UriComponents uriComponents = 
      UriComponentsBuilder
      .fromUriString("https://kapi.kakao.com/v1/user/"+path).build();

  RestTemplate restTemplate = new RestTemplate();
  HttpEntity<String> entity = new HttpEntity<>(headers);

  ResponseEntity<String> response = restTemplate.exchange(
      uriComponents.toUriString(), 
      HttpMethod.GET, 
      entity, 
      String.class
      );
  

  System.out.println("response.getBody() = " + response.getBody());
  
  return response.getBody();

}


  
}
