package main;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import dao.UserDao;

public class TestMain {
  UserDao userDao;
  static String clientId = "615090a30bbd7710330ccca09d00a1b4";
  static String baseUrl = "https://dapi.kakao.com/v2/local/search/address";
  static String mainAddress = "금암리 322";
  static int postcode = 0;
  
  public static void main(String[] args) {
    
    
    
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "KakaoAK "+clientId);
    
    UriComponents uriComponents = UriComponentsBuilder
            .fromUriString(baseUrl)
            .queryParam("query", mainAddress)
            .queryParam("analyze_type", "exact")
            .build();
    
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        uriComponents.toUriString(), 
        HttpMethod.GET, 
        entity, 
        Map.class
        );
    if(response.getStatusCode()==HttpStatus.OK) {
      Map<String, Object> responseBody = response.getBody();
      System.out.println(responseBody);
      
      List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");
      System.out.println(documents.toString());
      
      for (Map<String, Object> document : documents) {
        
        Map<String, Object> roadAddress = (Map<String, Object>) document.get("road_address");
        System.out.println(roadAddress.toString());
        
        if (roadAddress != null && roadAddress.containsKey("zone_no")) {
          String zoneNo = (String) roadAddress.get("zone_no");
          System.out.println("zoneNo = "+zoneNo + "비교 postcode = "+postcode);
          if (zoneNo.equals(postcode+"")) {
            System.out.println("일치값 찾음.");
          }
        }
      }
    }

    
  }

}