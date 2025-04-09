package global.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import global.interceptor.ClientIpInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="global.interceptor")
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

  private final ClientIpInterceptor clientIpInterceptor;

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	  registry.addInterceptor(clientIpInterceptor)
      .addPathPatterns("/**"); 
	}
	
}
