package global.validator;

import global.validator.annotaion.ValidText;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TextValidator implements ConstraintValidator<ValidText, String> {

  private static final String TEXT_REGEX = "^(?!\\s*$).{1,250}$\n";

  @Override
  public void initialize(ValidText constraintAnnotation) {
    // 초기화 로직 필요하면 여기에 (지금은 없음)
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return false;
    System.out.println("입력값: "+value);
    System.out.println("글 검증 결과: "+value.matches(TEXT_REGEX));
    return value.matches(TEXT_REGEX);
  }
  
}