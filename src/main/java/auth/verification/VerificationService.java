package auth.verification;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import auth.crypto.JwtTokenProvider;
import auth.redis.TokenStore;
import auth.verification.Verification.VerificationBuilder;
import global.exception.VerificationTimeoutException;
import infra.naver.mail.MailRecipientPayload;
import infra.naver.mail.NaverMailClient;
import infra.naver.mail.NaverMailPayload;
import infra.naver.sms.NaverSmsClient;
import infra.naver.sms.NaverSmsPayload;
import infra.naver.sms.SmsRecipientPayload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
  
  private final VerificationRepository verificationRepository;
  private final NaverMailClient naverMailClient;
  private final NaverSmsClient naverSmsClient;
  private final TokenStore uuidProvider;
  private final JwtTokenProvider jwtTokenProvider;
  private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
  
  @Value("${naver-sms.senderPhone}")
  private String senderPhone;
  
  @Value("${naver-email.senderEmail}")
  private String senderEmail;
  
  @Transactional
  public void sendSmsVerificationCode(String recipientPhone, String requestIp) {
   
    String verificationCode = generateVerificationCode();
    String body = "인증번호 [" + verificationCode + "]를 입력해주세요.";
    
    // 수신자 설정
    SmsRecipientPayload smsRecipientPayload = new SmsRecipientPayload(recipientPhone.replaceAll("[^0-9]", ""),body);
    List<SmsRecipientPayload> messages = List.of(smsRecipientPayload);

    // 요청 데이터 생성
    NaverSmsPayload naverSmsPayload = NaverSmsPayload.builder()
            .type("SMS")
            .contentType("COMM")
            .countryCode("82")
            .from(senderPhone)
            .content("달밤청소 인증번호")
//            .content("[ 달밤청소 가입 인증번호 ]\n[" + verificationCode + "]를 입력해주세요")
            .messages(messages)
            .build();
    
    VerificationBuilder verificationBuilder = Verification.builder()
    .requestIp(requestIp)
    .recipient(recipientPhone)
    .verificationCode(verificationCode);

    try {
      int sendStatus = naverSmsClient.sendVerificationCode(naverSmsPayload);
      verificationBuilder.sendStatus(sendStatus);
      if((sendStatus/100)!=2) {
        logger.info("인증번호 발송 실패: sendStatus: {}, phone: {} , ip: {}", sendStatus, recipientPhone, requestIp);
      }
    } catch (Exception e) {
      verificationBuilder.sendStatus(500);
      e.printStackTrace();
      logger.error("인증번호 발송 실패: phone: {} , ip: {}", recipientPhone, requestIp);
      throw new IllegalStateException("인증번호 발송 실패. 나중에 다시 시도해주세요.");
    }finally {
      verificationRepository.save(verificationBuilder.build());
    }
  }
  
  @Transactional
  public void sendEmailVerificationCode(String recipientEmail, String requestIp) {
    System.out.println("메일 발송 서비스 시작.");
    String verificationCode = generateVerificationCode();
    String body = "인증번호 [" + verificationCode + "]를 입력해주세요.";
    
    // 수신자 설정
    MailRecipientPayload mailRecipientPayload = new MailRecipientPayload(recipientEmail,recipientEmail,"R");
    List<MailRecipientPayload> mails = List.of(mailRecipientPayload);

    // 요청 데이터 생성
    NaverMailPayload naverMailPayload = NaverMailPayload.builder()
        .senderAddress(senderEmail)
        .title("달밤청소 인증번호")
        .body(body)
        .recipients(mails)
        .individual(true)
        .advertising(false)
        .build();
    
//  Map<String, Object> recipient = Map.of(
//  "address", recipientEmail,
//  "name", recipientEmail,
//  "type", "R"
//);
//
//Map<String, Object> mailRequest = Map.of(
//  "senderAddress", senderEmail,
//  "title", "[ 달밤청소 회원 가입 인증번호 ]",
//  "body", "달밤청소 회원 가입 인증번호\n[" + verificationCode + "]를 입력해주세요",
//  "recipients", List.of(recipient),
//  "individual", true,
//  "advertising", false
//);
    
    VerificationBuilder verificationBuilder = Verification.builder()
    .requestIp(requestIp)
    .recipient(recipientEmail)
    .verificationCode(verificationCode);

    try {
      int sendStatus = naverMailClient.sendVerificationCode(naverMailPayload);
      verificationBuilder.sendStatus(sendStatus);
      if((sendStatus/100)!=2) {
        System.out.println("인증번호 발송 실패 (sendStatus/100)!=2: "+(sendStatus/100));
        logger.info("인증번호 발송 실패: sendStatus: {}, phone: {} , ip: {}", sendStatus, recipientEmail, requestIp);
      }
    } catch (Exception e) {
      System.out.println("익셉션 발생.");
      verificationBuilder.sendStatus(500);
      e.printStackTrace();
      logger.error("인증번호 발송 실패: phone: {} , ip: {}", recipientEmail, requestIp);
      throw new IllegalStateException("인증번호 발송 실패. 나중에 다시 시도해주세요.");
    }finally {
      System.out.println("발송된 인증 DB verification 저장");
      verificationRepository.save(verificationBuilder.build());
    }
  }
  
  @Transactional
  public String compareSmsForJwt(String phone, String reqCode, String requestIp) throws TimeoutException {
    Verification verification = compareCode(phone, reqCode, requestIp);
    return jwtTokenProvider.createVerifyPhoneToken(verification.getVerificationSeq(), List.of("RULES_GUEST"),Map.of("phone",phone));
  }
  
  @Transactional
  public String compareSms(String phone, String reqCode, String requestIp) throws TimeoutException {
    compareCode(phone, reqCode, requestIp);
    return uuidProvider.createVerificationPhoneToken(phone);
  }
  
  @Transactional
  public String compareEmail(String email, String reqCode, String requestIp) throws TimeoutException {
    compareCode(email, reqCode, requestIp);
    return uuidProvider.createVerificationEmailToken(email);
  }
  
  //  (to 기준, 10분 이내 요청한, 제일 최근에 요청한, 인증 여부)
  //  인증 요청이후 5분이내에 성공했지만, 인증 성공 uuid의 유효시간이 5분이기에 최대 10분으로 조회
  public boolean validateVerify(String to) {
    return verificationRepository.findRecentVerificationWithin10Min(to)
        .map(verification -> Boolean.TRUE.equals(verification.getVerify()))
        .orElseThrow(()->new BadCredentialsException("인증 되지 않았습니다."));
  }
  
  private Verification compareCode(String to, String reqCode, String requestIp) throws TimeoutException {
    // ======= 수신자에 일치하는 DB 찾기 =======
    Verification verification = verificationRepository.findTopByRecipientOrderByCreatedAtDesc(to)
        .orElseThrow(() -> {
          logger.info("인증 비교 실패 : 존재하지 않는 인증을 인증 요청함. to: {}, reqCode: {}, requestIp: {}",to, reqCode, requestIp);
          return new NoSuchElementException("존재하지 않는 인증을 인증 요청함");
          });
    
    // ======= 해당 데이터가 3분 이내인지 여부 확인 =======
    boolean withinTimeResult = verificationRepository.isWithinVerificationTime(verification.getVerificationSeq()) == 1L;
    System.out.println("인증시퀀스키값: "+ verification.getVerificationSeq()+", 3분이내 시퀀스인지 조회 결과: "+withinTimeResult);
    if (!withinTimeResult) {
      verification.setVerify(false);
      verificationRepository.flush();
      throw new VerificationTimeoutException("인증 시간이 초과 되었습니다.");
    }
    
    // ======= 인증번호 일치 결과 =======
    if(!verification.getVerificationCode().equals(reqCode)) {
      logger.info("인증 번호 불일치 : to: {}, reqCode: {}, requestIp: {}",to, reqCode, requestIp);
      throw new IllegalArgumentException("인증 비교 실패 : 인증 번호 불일치");
    }
    
    verificationRepository.markAsVerified(verification.getVerificationSeq());
    verificationRepository.flush();
    
    return verification;
  }
  
  private String generateVerificationCode() {
    return String.format("%06d", new Random().nextInt(1000000)); // 6자리 인증번호 생성
  }
    
//  @Transactional
//  public VerificationDto registerVerificationDto(VerificationDto verificationDto, String requestIp) throws SQLException {
//    verificationDto.setRequestIp(requestIp);
//    int result = verificationDao.registerVerificationDto(verificationDto);
//    return verificationDao.getDtoBySequence(result);
//  }
  
//  public Verification sendVerificationEmail(String email, String requestIp) throws Exception {
//    String verificationCode = generateVerificationCode();
//    int statusCode = naverMailClient.sendVerificationCode(email, verificationCode);
//
//    Verification verificationDto =
//        new Verification(
//            requestIp,
//            email,
//            verificationCode,
//            statusCode+""
//            );
//
//    int verSeq = verificationDao.registerVerificationDto(verificationDto);
//    return verificationDao.getDtoBySequence(verSeq);
//  }
//
//  public Verification sendVerificationSms(String phone, String requestIp) throws Exception {
//    String verificationCode = generateVerificationCode();
//    int statusCode = naverSmsClient.sendVerificationCode(phone, verificationCode);
//    Verification verificationDto =
//        new Verification(
//            requestIp,
//            phone,
//            verificationCode,
//            statusCode+""
//            );
//    
//    int verSeq = verificationDao.registerVerificationDto(verificationDto);
//    return verificationDao.getDtoBySequence(verSeq);
//  }
//  
//  @Transactional
//  public Verification compareCode(String to, String reqCode) throws TimeoutException {
//    Verification verificationDto = verificationDao.getVerificationDtoByTo(to)
//        .orElseThrow(() -> new IllegalArgumentException("인증 비교 실패 : 등록된 인증 정보가 없습니다."));
//    
//    boolean withinTimeResult = verificationDao.isWithinVerificationTime(verificationDto.getVerificationSeq());
//    if (!withinTimeResult) {
//      verificationDao.updateVerify(verificationDto.getVerificationSeq(),false);
//      throw new TimeoutException("인증 비교 실패 : 인증 시간이 초과 되었습니다.");
//    } 
//
//    String verificationCode = verificationDto.getVerificationCode();
//    if(!verificationCode.equals(reqCode)) throw new BadCredentialsException("인증 비교 실패 : 인증 번호가 일치하지 않습니다.");
//    
//    verificationDao.updateVerify(verificationDto.getVerificationSeq(),true);
//    
//    return verificationDao.getDtoBySequence(verificationDto.getVerificationSeq());
//  }
//  
//  public void validateVerify(int verSeq) {
//    Verification verificationDto = verificationDao.getDtoBySequence(verSeq);
//    if(!verificationDto.isVerify()) {
//      throw new BadCredentialsException("인증 확인 실패 : 인증이 완료되지 않은 요청입니다.");
//    }
//  }
//  

  
}
