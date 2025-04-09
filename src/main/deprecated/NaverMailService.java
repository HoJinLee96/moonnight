package service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.util.ExceptionUtil;
import entity.Verification;
import lombok.RequiredArgsConstructor;
import repository.EstimateRepository;
import repository.UserRepository;
import util.PayloadHeaderUtil;


@Service
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
public class NaverMailService {
  
  private final PayloadHeaderUtil payloadHeaderUtil;
  
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
  public Verification sendMail(String reqEmail) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, SQLException, JsonMappingException, JsonProcessingException {
    
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
    mailRequest.put("title", "[ 달밤청소 회원 가입 인증번호 ]");
    mailRequest.put("body", "달밤청소 회원 가입 인증번호" + "\n" + "[" + verificationCode + "]를 입력해주세요");
    mailRequest.put("recipients", recipients);
    mailRequest.put("individual", true);
    mailRequest.put("advertising", false);

    // 헤더세팅
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-ncp-apigw-timestamp", time);
    headers.set("x-ncp-iam-access-key", accessKey);
    headers.set("x-ncp-apigw-signature-v2", oAuthUtil.getNaverSignature("POST", mailEndpoint + sendMailUri, time));
    headers.set("x-ncp-lang", "ko-KR");

    // JSON 본문 설정
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(mailRequest, headers);

    // RestTemplate를 통해 외부 API와 통신
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    
    ResponseEntity<String> response = restTemplate.exchange(mailApiUrl + mailEndpoint + sendMailUri, HttpMethod.POST, entity, String.class);
    if(response.getStatusCode().is2xxSuccessful()){
      Verification verificationDto =
          new Verification(
              reqEmail,
              verificationCode,
              response.getStatusCode().value()+""
              );
      return verificationDto;
    }else {
      throw ExceptionUtil.createIllegalState("네이버 MAIL API 요청 실패 상태 : \s", response.getStatusCode());
    }

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
