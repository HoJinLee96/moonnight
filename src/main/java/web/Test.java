package web;

import java.util.Set;
import auth.sign.SigninRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class Test {
  public static void main(String[] args) {
    SigninRequestDto dto = new SigninRequestDto("st2035@naver.com", "Leeought21!@@@!!");

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    Set<ConstraintViolation<SigninRequestDto>> violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      for (ConstraintViolation<SigninRequestDto> v : violations) {
        System.out.println("에러 메시지: " + v.getMessage());
      }
    } else {
      System.out.println(dto.toString());
    }
  }
}