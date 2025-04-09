package global.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Documented // 자바독(javadoc) 문서에 이 애노테이션도 표시됨.
@Constraint(validatedBy = {}) // Validator 생략 가능하면 비워둬도 됨 
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // 어디에 붙일 수 있는지 지정 (예: 필드, 파라미터 등).
@Retention(RetentionPolicy.RUNTIME) // 언제까지 유지할지 설정 (런타임까지 유지해야 검증됨).
@NotBlank(message = "{validation.user.phone.required}")
@Pattern(regexp = "^0\\d{2,3}-\\d{3,4}-\\d{4}$", message = "{validation.user.phone.invalid}")
@Size(max = 20, message = "{validation.user.phone.length}")
public @interface ValidPhone {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}