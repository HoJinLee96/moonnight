package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import dao.AddressDao;
import dto.AddressDto;
import dto.UserDto;
import exception.NotFoundException;

@Service
@PropertySource("classpath:application.properties")
public class AddressService {
  
  @Value("${kakao-addressSearch.baseUrl}")
  private String baseUrl;
  @Value("${kakao.clientId}")
  private String clientId;
  
  private AddressDao addressDao;

  @Autowired
  public AddressService(AddressDao addressDao) {
    this.addressDao = addressDao;
  }
  
  @PostConstruct
  private void init() {
      System.out.println("kakao-login.baseUrl : " + baseUrl);
      System.out.println("kakao.clientId : " + clientId);
  }
  
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
           System.out.println(zoneNo +" "+ postcode);
           if (zoneNo.equals(postcode)) {
             System.out.println("주소 검증 정상");
             return true;
           }
         }
       }
     }
       System.out.println("주소 검증 비정상");
       return false;
     
  }
 
 
  
  @Transactional
  public void registerAddress(AddressDto addressDto) throws SQLException {
    int result = addressDao.registerAddress(addressDto);
    if(result==0)
      throw new SQLException();
  }
  
  @Transactional
  public void updateAddress(AddressDto addressDto) throws SQLException,NotFoundException{
    int result = addressDao.updateAddress(addressDto);
    if(result==0) throw new NotFoundException();
  }
  
  @Transactional
  public void deleteAddress(int addressSeq) throws SQLException{
    addressDao.deleteAddress(addressSeq);
  }
  
  public AddressDto getAddressDtoByAddressSeq(int addressSeq) throws SQLException, NotFoundException {
    return addressDao.getAddressDtoByAddressSeq(addressSeq).orElseThrow(()->new NotFoundException());
  }
  
  public List<AddressDto> getListByUserSeq(int userSeq) throws SQLException, NotFoundException {
    List<AddressDto> list = addressDao.getAddressListByUserSeq(userSeq);
    if (list.isEmpty()) {
        throw new NotFoundException("저장된 주소가 없습니다.");
    }
    return list;
  }
  
  public List<AddressDto> getSortedListByUserSeq(UserDto userDto) throws SQLException, NotFoundException {
    List<AddressDto> list = addressDao.getAddressListByUserSeq(userDto.getUserSeq());
    if (list.isEmpty()) {
        throw new NotFoundException("저장된 주소가 없습니다.");
    }
    List<AddressDto> copyList = new ArrayList<>(list);
    for (AddressDto l : copyList) {
        if (userDto.getAddressSeq() == l.getAddressSeq()) {
            list.remove(l); // 원본 리스트에서 수정
            list.addFirst(l);
        }
    }
    return list;
  }

}
