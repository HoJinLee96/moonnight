package global.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

//@Documented // 자바독(javadoc) 문서에 이 애노테이션도 표시됨.
//@Constraint(validatedBy = {}) // Validator 생략 가능하면 비워둬도 됨 
//@Target({ ElementType.FIELD, ElementType.PARAMETER }) // 어디에 붙일 수 있는지 지정 (예: 필드, 파라미터 등).
//@Retention(RetentionPolicy.RUNTIME) // 언제까지 유지할지 설정 (런타임까지 유지해야 검증됨).
//@NotBlank(message = "{validation.user.email.required}")
//@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "{validation.user.email.invalid}")
//@Size(min = 5, max = 50, message = "{validation.user.email.length}")
//public @interface ValidEmail {
//  String message() default "Invalid email";
//  Class<?>[] groups() default {};
//  Class<? extends Payload>[] payload() default {};
//}

@Documented
@Constraint(validatedBy = EmailValidator.class) 
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

  String message() default "Invalid email";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}