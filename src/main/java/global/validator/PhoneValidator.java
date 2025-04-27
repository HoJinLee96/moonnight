package global.validator;

import global.validator.annotaion.ValidPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
  private static final String PHONE_REGEX = "^0\\d{2,3}-\\d{3,4}-\\d{4}$";

  @Override
  public void initialize(ValidPhone constraintAnnotation) {
    // 초기화 로직 필요하면 여기에 (지금은 없음)
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return false;
    System.out.println("입력값: "+value);
    System.out.println("휴대폰 검증 결과: "+value.matches(PHONE_REGEX));
    return value.matches(PHONE_REGEX);
  }
}
