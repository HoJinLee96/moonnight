package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import intercepter.LoginAuthInterceptor;
import intercepter.MasterAuthInterceptor;
import intercepter.OAuthRefreshInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="intercepter")
public class MvcConfig implements WebMvcConfigurer {

	@Autowired
	private LoginAuthInterceptor loginAuthInterceptor;
	@Autowired
	private OAuthRefreshInterceptor oAuthRefreshInterceptor;
	@Autowired
	private MasterAuthInterceptor masterAuthInterceptor;

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
	  registry.addInterceptor(oAuthRefreshInterceptor);
	  registry.addInterceptor(loginAuthInterceptor).addPathPatterns("/login","/my/**","/verifyUser");
	  registry.addInterceptor(masterAuthInterceptor).addPathPatterns("/master/**").excludePathPatterns("/master/login");
	}
    

}
