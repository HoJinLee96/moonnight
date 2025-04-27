package global.validator;

import global.validator.annotaion.ValidId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdValidator implements ConstraintValidator<ValidId, String> {

  private static final String ID_REGEX = "^\\{1,10}$";

  @Override
  public void initialize(ValidId constraintAnnotation) {
    // 초기화 로직 필요하면 여기에 (지금은 없음)
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return false;
    System.out.println("입력값: "+value);
    System.out.println("DB ID 형식 검증 결과: "+value.matches(ID_REGEX));
    return value.matches(ID_REGEX);
  }
  
}