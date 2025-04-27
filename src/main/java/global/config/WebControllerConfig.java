package global.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages= {"domain","web","infra","global"})
public class WebControllerConfig {

}