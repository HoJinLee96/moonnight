package global.validator.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있도록 설정
@Retention(RetentionPolicy.RUNTIME) // 런타임에도 어노테이션 정보를 유지하도록 설정
public @interface ClientSpecific {
    String value(); // 어떤 값을 가져올지 지정하기 위한 속성 (예: @ClientSpecific("여기에_넣은_값"))
}