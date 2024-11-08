package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
import dtoNaverLogin.OAuthToken;
import exception.NotFoundException;

@Service
@PropertySource("classpath:application.properties")
public class NaverOAuthLoginService {

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
  
  public String getNaverAuthorizeUrl(String path) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {

      UriComponents uriComponents = UriComponentsBuilder
              .fromUriString(baseUrl + "/" + path)
              .queryParam("response_type", "code")
              .queryParam("client_id", clientId)
              .queryParam("redirect_uri", URLEncoder.encode(redirectUrl, "UTF-8"))
              .build();

      return uriComponents.toString();
  }
  
  public String getTokenUrl(String path,String code) throws IOException, NotFoundException {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json;charset=utf-8");
    
    UriComponents uriComponents =UriComponentsBuilder
        .fromUriString(baseUrl + "/" + path)
        .queryParam("grant_type", "authorization_code")
        .queryParam("client_id", clientId)
        .queryParam("client_secret", clientSecret)
        .queryParam("code", code)
//        .queryPa  ram("state", new URL().)
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
          throw new NotFoundException("네이버 서버에서 받아오지 못함.");
        }
   
}
  public String updateTokenUrl(String path,String grant_type,OAuthToken oAuthToken) throws IOException {

        UriComponentsBuilder uriComponentsBuilder =UriComponentsBuilder
                .fromUriString(baseUrl + "/" + path)
                .queryParam("grant_type", grant_type)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret);
        
        if(grant_type.equals("refresh_token")) {
          uriComponentsBuilder.queryParam("refresh_token", oAuthToken.getRefresh_token());
        }else if(grant_type.equals("delete")){
          uriComponentsBuilder.queryParam("access_token", URLEncoder.encode(oAuthToken.getAccess_token(),"UTF-8"));
        }else {
          throw new IOException("grant_type 잘못 입력");
        }
        
        UriComponents uriComponents = uriComponentsBuilder.build();
        
        URL url = new URL(uriComponents.toString());
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
    
        int responseCode = con.getResponseCode();
        BufferedReader br;
    
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
    
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
    
        br.close();
        return response.toString();
   
}
  
//회원 정보 받기
  public String getNaverUserByToken(OAuthToken token) throws IOException {

        String accessToken = token.getAccess_token();
        String tokenType = token.getToken_type();

        URL url = new URL("https://openapi.naver.com/v1/nid/me");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", tokenType + " " + accessToken);

        int responseCode = con.getResponseCode();
        BufferedReader br;

        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }

        br.close();
        return response.toString();
}



}
