package global.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import jakarta.servlet.MultipartConfigElement;

@Configuration
public class EtcConfig {
  
  @Bean
  public PasswordEncoder passwordEncoder() {
//    비밀번호를 해시할 때 몇 번 반복해서 계산할지를 정하는 값
    int strength = 12; 
    return new BCryptPasswordEncoder(strength);
  }

  @Bean
  public MultipartResolver multipartResolver() {
      return new StandardServletMultipartResolver();
  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
      MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
              null, // 위치를 지정하지 않으면 기본값 사용
              5242880, // 파일 하나의 최대 크기 (5MB)
              104857600, // 전체 요청의 최대 크기 (20MB)
              0 // 임시 파일 임계값 (바이트 단위, 0으로 설정하면 바로 디스크에 저장)
      );
      return multipartConfigElement;
  }
  
  @Bean
  public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
  }
  
//  @Configuration
//  public class SpringDocConfig {
//      
//      @Bean
//      public OpenAPI openAPI() {
//          return new OpenAPI()
//              .info(new Info()
//                  .title("Your API Title")
//                  .description("API 설명")
//                  .version("v1.0"));
//      }
//  }

}
