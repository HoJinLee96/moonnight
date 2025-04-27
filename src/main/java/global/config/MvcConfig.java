package global.config;

import java.util.List;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import global.interceptor.ClientIpInterceptor;
import global.validator.ClientSpecificArgumentResolver;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="global")
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

  private final ClientIpInterceptor clientIpInterceptor;
  private final ClientSpecificArgumentResolver clientSpecificArgumentResolver;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(clientSpecificArgumentResolver);
    }
//	
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
//
//        registry.addResourceHandler("/v3/api-docs/**")
//                .addResourceLocations("classpath:/META-INF/resources/");
//    }
	
}
