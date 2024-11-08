package service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class NaverCreateSignature {
  
  @Value("${naver-api.accessKey}")
  private String accessKey;

  @Value("${naver-api.secretKey}")
  private String secretKey;
  
  @PostConstruct
  private void init() {
      System.out.println("naver-api.accessKey : " + accessKey);
      System.out.println("naver-api.secretKey : " + secretKey);
  }
  
  public String getSignature(String method, String url, String time) throws UnsupportedEncodingException, NoSuchAlgorithmException, java.security.InvalidKeyException  {
    
    String message = new StringBuilder().append(method).append(" ").append(url).append("\n")
        .append(time).append("\n").append(accessKey).toString();

    SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(signingKey);

    byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
    String encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);

    System.out.println("시그니처 생성완료");
    return encodeBase64String;
  }
}
