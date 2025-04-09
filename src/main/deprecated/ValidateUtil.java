package deprecated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Deprecated
// 해당 ValidateUtil에 작성되어 있는 메서드들은 주로 Contorller가 클라이언트로 부터 받은 값들의 데이터 형식 검사
// 이외 실제로 존재하는 데이터인지 또는 권한이 적합한지 확인하는 3차 검증은 서비스 계층에서 따로 작성하여 실행
public class ValidateUtil {

  private final static String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
  private final static String passwordRegex = "/^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$/";  //최소 8자 이상이며, 영어, 숫자, 특수기호를 포함해야 합니다.
  private final static String phoneRegex = "^0\\d{2,3}-\\d{3,4}-\\d{4}$"; // 0으로 시작하며 3~4자리-3~4자리-4자리
  private final static String verificationCodeRegex = "^\\d{6}$"; // 숫자 6자리
  private final static String birthRegex = "^\\d{4}\\d{2}\\d{2}$"; // yyyy-MM-dd 형식


  public static boolean validateEmail(String email) {
    validateNull(email);
    if (Pattern.matches(emailRegex, email)) {
      return true;
    }
    throw new IllegalArgumentException("데이터 형식 부적합 [이메일] email : " + email);
  }
  
  public static boolean validatePassword(String password) {
    validateNull(password);
    if (Pattern.matches(passwordRegex, password)) {
      return true;
    }
    throw new IllegalArgumentException("데이터 형식 부적합 [비밀번호] password : " + password);
  }

  public static boolean validatePhone(String phone) {
    validateNull(phone);
    if (Pattern.matches(phoneRegex, phone)) {
      return true;
    }
    throw new IllegalArgumentException("데이터 형식 부적합 [휴대폰] phone : " + phone);
  }

  public static boolean validateEmailOrPhone(String to) {
    validateNull(to);
    boolean isEmail = Pattern.matches(emailRegex, to);
    boolean isPhone = Pattern.matches(phoneRegex, to);

    if (isEmail || isPhone) {
      return true;
    }
    throw new IllegalArgumentException("데이터 형식 부적합 [수신자] to : " + to);
  }
  
  public static boolean validateVerificationCodeRegex(String verificationCode) {
    validateNull(verificationCode);
    if (Pattern.matches(verificationCodeRegex, verificationCode)) {
      return true;
    }
    throw new IllegalArgumentException("데이터 형식 부적합 [인증코드] verificationCode : " + verificationCode);
  }

  private boolean validateBirth(String birth) {
    validateNull(birth);
    if(!Pattern.matches(birthRegex, birth)) {
      throw new IllegalArgumentException("데이터 형식 부적합 [생년월일] birth : " + birth);
    }
      // 문자열을 LocalDate로 변환
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
      LocalDate birthDate = LocalDate.parse(birth, formatter);
      
      // 허용 범위 설정: 1900-01-01부터 현재 날짜까지
      LocalDate earliestDate = LocalDate.of(1900, 1, 1);
      LocalDate currentDate = LocalDate.now();
      
      if (birthDate.isBefore(earliestDate) || birthDate.isAfter(currentDate)) {
        throw new IllegalArgumentException("데이터 부적합 [생년월일] birth : " + birth);
      }
    return true;
  }
  
  public static boolean validateNull(String data) {
    if (data == null || data.trim().isEmpty()) {
      throw new IllegalArgumentException("데이터 형식 부적합 Data : NULL");
    }
    return true;
  }
  
}
