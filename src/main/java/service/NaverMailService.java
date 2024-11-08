package service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import dto.VerifyResponseDto;


@Service
@PropertySource("classpath:application.properties")
public class NaverMailService {
  
  private final NaverCreateSignature naverCreateSignature;
  
  @Autowired
  public NaverMailService(NaverCreateSignature naverCreateSignature) {
    this.naverCreateSignature = naverCreateSignature;
  }

  @Value("${naver-api.accessKey}")
  private String accessKey;
  
  @Value("${naver-mail.senderEmail}")
  private String senderEmail;
  
  @PostConstruct
  private void init() {
    System.out.println("accessKey : " + accessKey);
    System.out.println("senderEmail : " + senderEmail);
  }

  // url
  private final String mailApiUrl ="https://mail.apigw.ntruss.com";
  private final String mailEndpoint = "/api/v1";
  
  private final String sendMailUri = "/mails";


  // 메일 전송
  public VerifyResponseDto sendMail(String reqEmail) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, SQLException {
    
 // 인증번호 생성
    String verificationCode = createVerificationCode();
    System.out.println("인증번호 발생 : "+ verificationCode);

    // 현재시간
    String time = Long.toString(System.currentTimeMillis());

    // 수신자 생성
    List<Map<String, Object>> recipients = new ArrayList<>();
    Map<String, Object> recipient = new HashMap<>();
    recipient.put("address", reqEmail);
    recipient.put("name", reqEmail);
    recipient.put("type", "R");
    recipients.add(recipient);

    // 메일 요청 데이터 생성
    Map<String, Object> mailRequest = new HashMap<>();
    mailRequest.put("senderAddress", senderEmail);
    mailRequest.put("title", "[ chamman 회원 가입 인증번호 ]");
    mailRequest.put("body", "[ 인증번호 ]" + "\n" + "[" + verificationCode + "]를 입력해주세요");
    mailRequest.put("recipients", recipients);
    mailRequest.put("individual", true);
    mailRequest.put("advertising", false);

    // 헤더세팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-ncp-apigw-timestamp", time);
    headers.set("x-ncp-iam-access-key", accessKey);
    headers.set("x-ncp-apigw-signature-v2", naverCreateSignature.getSignature("POST", mailEndpoint + sendMailUri, time));
    headers.set("x-ncp-lang", "ko-KR");

    // JSON 본문 설정
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(mailRequest, headers);

    // RestTemplate를 통해 외부 API와 통신
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    ResponseEntity<String> response = restTemplate.exchange(mailApiUrl + mailEndpoint + sendMailUri, HttpMethod.POST, entity, String.class);
    
    VerifyResponseDto responseDto =
        new VerifyResponseDto.Builder()
        .to(reqEmail)
        .verificationCode(verificationCode)
        .statusCode(response.getStatusCodeValue()+"")
        .requestTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(time)), ZoneId.systemDefault()))
        .build();

    return responseDto;
  }
  
//5자리의 난수를 조합을 통해 인증코드 만들기
  private String createVerificationCode() {
      StringBuffer key = new StringBuffer();
      Random rnd = new Random();

      for (int i = 0; i < 5; i++) { // 인증코드 5자리
          key.append((rnd.nextInt(10)));
      }
      return key.toString();
  }
}
