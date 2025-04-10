package infra.kakao;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@PropertySource("classpath:application.properties")
public class DaumMapClient {
  
  @Value("${kakao-addressSearch.baseUrl}")
  private String baseUrl;
  @Value("${kakao.clientId}")
  private String clientId;
  
  public boolean validateAddress(String postcode, String mainAddress) {
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
      List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");
 
      for (Map<String, Object> document : documents) {
        Map<String, Object> roadAddress = (Map<String, Object>) document.get("road_address");
        
        if (roadAddress != null && roadAddress.containsKey("zone_no")) {
          String zoneNo = (String) roadAddress.get("zone_no");
          if (zoneNo.equals(postcode)) {
            System.out.println("주소 검증 정상.");
            return true;
          }
        }
      }
      // 반복문을 다 돌았지만 일치하는 주소가 없는 경우
      return false;
    }else {
      throw new IllegalStateException("주소 검증 요청 실패 : 다음 서버에서 응답을 받을 수 없습니다.");
    }
 }
  
}
