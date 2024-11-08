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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.MessageDto;
import dto.SmsRequestDto;
import dto.VerifyResponseDto;

@PropertySource("classpath:application.properties")
@Service
public class NaverSmsService {
	
	private final NaverCreateSignature naverCreateSignature;
	
	@Value("${naver-api.accessKey}")
	private String accessKey;

	@Value("${naver-sms.serviceId}")
	private String serviceId;

	@Value("${naver-sms.senderPhone}")
	private String phone;
	
	
	@Autowired
	public NaverSmsService(NaverCreateSignature naverCreateSignature) {
		this.naverCreateSignature = naverCreateSignature;
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


	public VerifyResponseDto sendSms(String phoneNumber) throws JsonProcessingException, RestClientException,
			URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, java.security.InvalidKeyException, SQLException {
	    
	    System.out.println("인증번호 발생 시도 : "+phoneNumber);
	    
	    //인증번호 생성
	    String verificationCode = createVerificationCode();
	    System.out.println("인증번호 발생 : "+ verificationCode);
	    
		// 현재시간
		String time = Long.toString(System.currentTimeMillis());
		System.out.println("현재시간 발생 : "+ time);
		
		// 수신사 생성
		MessageDto messageDto = new MessageDto(phoneNumber);
		List<MessageDto> messages = new ArrayList<>();
		messages.add(messageDto);
		
		// 헤더세팅
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-ncp-apigw-timestamp", time);
		headers.set("x-ncp-iam-access-key", accessKey);
		headers.set("x-ncp-apigw-signature-v2", naverCreateSignature.getSignature("POST", smsEndpoint + this.serviceId + sendSmsUri, time)); // signature 서명
		headers.add("Content-Type", "application/json; charset=UTF-8");

		// api 요청 양식에 맞춰 세팅
		SmsRequestDto request = SmsRequestDto.builder().type("SMS").contentType("COMM").countryCode("82").from(phone)
				.content("[ goodsone1 인증번호 ]"+ "\n" +  "[" + verificationCode + "]를 입력해주세요").messages(messages).build();

		// request를 json형태로 body로 변환
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		String body = objectMapper.writeValueAsString(request);
		
		// body와 header을 합친다
		HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

		// restTemplate를 통해 외부 api와 통신
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters()
	    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		VerifyResponseDto verifyResponseDto = restTemplate.postForObject(new URI(smsApiUrl +smsEndpoint+ this.serviceId + sendSmsUri), httpBody,VerifyResponseDto.class);
		VerifyResponseDto newverifyResponseDto =
		    new VerifyResponseDto.Builder()
		    .verificationCode(verificationCode)
		    .to(phoneNumber)
		    .statusCode(verifyResponseDto.getStatusCode())
		    .requestTime(verifyResponseDto.getRequestTime())
		    .build();
		
		return newverifyResponseDto;
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