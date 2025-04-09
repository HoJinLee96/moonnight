package service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.management.openmbean.InvalidKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ExceptionUtil;
import deprecated.OAuthUtil;
import entity.Verification;
import infra.client.payload.NaverSmsPayload;
import infra.client.payload.SmsRecipientPayload;

@PropertySource("classpath:application.properties")
@Service
public class NaverSmsService {
	
  private final ApiUtil oAuthUtil;
	
	@Value("${naver-api.accessKey}")
	private String accessKey;

	@Value("${naver-sms.serviceId}")
	private String serviceId;

	@Value("${naver-sms.senderPhone}")
	private String phone;
	
	@Autowired
	public NaverSmsService(ApiUtil oAuthUtil) {
	    this.oAuthUtil = oAuthUtil;
	}
	
	@PostConstruct
    private void init() {
        System.out.println("serviceId: " + serviceId);
        System.out.println("phone: " + phone);
    }
	
	// url
	private final String smsApiUrl = "https://sens.apigw.ntruss.com";
	private final String smsEndpoint = "/sms/v2/services/";
	private final String sendSmsUri = "/messages";


	public Verification sendSms(String reqPhone) throws JsonProcessingException, RestClientException,
			URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, java.security.InvalidKeyException, SQLException {
	    
	    System.out.println("인증번호 발생 시도 : "+reqPhone);
	    
	    //인증번호 생성
	    String verificationCode = createVerificationCode();
	    System.out.println("인증번호 발생 : "+ verificationCode);
	    
		// 현재시간
		String time = Long.toString(System.currentTimeMillis());
		System.out.println("현재시간 발생 : "+ time);
		
		// 수신사 생성
		SmsRecipientPayload messageDto = new SmsRecipientPayload(reqPhone.replaceAll("[^0-9]", ""));
		List<SmsRecipientPayload> messages = new ArrayList<>();
		messages.add(messageDto);
		
		// 헤더세팅
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-ncp-apigw-timestamp", time);
		headers.set("x-ncp-iam-access-key", accessKey);
		headers.set("x-ncp-apigw-signature-v2", oAuthUtil.getNaverSignature("POST", smsEndpoint + this.serviceId + sendSmsUri, time)); // signature 서명
		headers.add("Content-Type", "application/json; charset=UTF-8");

		// api 요청 양식에 맞춰 세팅
		NaverSmsPayload smsRequestDto = NaverSmsPayload.builder().type("SMS").contentType("COMM").countryCode("82").from(phone)
				.content("[ 달밤청소 가입 인증번호 ]"+ "\n" +  "[" + verificationCode + "]를 입력해주세요").messages(messages).build();

		// request를 json형태로 body로 변환
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
//		String body = objectMapper.writeValueAsString(smsRequestDto);
		
		// entity
		HttpEntity<NaverSmsPayload> entity = new HttpEntity<>(smsRequestDto, headers);

		// restTemplate를 통해 외부 api와 통신
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		ResponseEntity<String> response = restTemplate.postForEntity(new URI(smsApiUrl +smsEndpoint+ this.serviceId + sendSmsUri), entity,String.class);
		
	    if(response.getStatusCode().is2xxSuccessful()){
	        Verification verificationDto =
	            new Verification(
	                reqPhone,
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
	
    public void sendEstimateSeq(String phoneNumber, int estimateSeq)
        throws RestClientException, URISyntaxException, JsonProcessingException,
        java.security.InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {

      // 현재시간
      String time = Long.toString(System.currentTimeMillis());
      System.out.println("현재시간 발생 : " + time);

      // 수신사 생성
      phoneNumber = phoneNumber.replaceAll("-", "");
      System.out.println(phoneNumber);
      SmsRecipientPayload messageDto = new SmsRecipientPayload(phoneNumber);
      List<SmsRecipientPayload> messages = new ArrayList<>();
      messages.add(messageDto);

      // 헤더세팅
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("x-ncp-apigw-timestamp", time);
      headers.set("x-ncp-iam-access-key", accessKey);
      headers.set("x-ncp-apigw-signature-v2", oAuthUtil.getNaverSignature("POST",
          smsEndpoint + this.serviceId + sendSmsUri, time));
      headers.add("Content-Type", "application/json; charset=UTF-8");

      // api 요청 양식에 맞춰 세팅
      NaverSmsPayload request = NaverSmsPayload.builder().type("SMS").contentType("COMM")
          .countryCode("82").from(phone)
          .content("[견적 번호 : " + estimateSeq + " ]\n" + "달밤청소 문의 주셔서 감사합니다." + "\n빠른 시일 내에 연락 드리겠습니다")
          .messages(messages).build();

      // request를 json형태로 body로 변환
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
      String body = objectMapper.writeValueAsString(request);

      // body와 header을 합친다
      HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

      // restTemplate를 통해 외부 api와 통신
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.getMessageConverters().add(0,new StringHttpMessageConverter(StandardCharsets.UTF_8));
      restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

      restTemplate.postForEntity(new URI(smsApiUrl + smsEndpoint + this.serviceId + sendSmsUri),
          httpBody, String.class);

    }
	
}