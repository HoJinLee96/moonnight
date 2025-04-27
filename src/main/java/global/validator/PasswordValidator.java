package global.validator;

import global.validator.annotaion.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String>{
  
  private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{8,60}$";
  
  @Override
  public void initialize(ValidPassword constraintAnnotation) {
    // 초기화 로직 필요하면 여기에 (지금은 없음)
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return false;
    System.out.println("입력값: "+value);
    System.out.println("비밀번호 검증 결과: "+value.matches(PASSWORD_REGEX));
    return value.matches(PASSWORD_REGEX);
  }
}
