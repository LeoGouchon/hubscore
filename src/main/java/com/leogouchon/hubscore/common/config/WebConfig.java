package com.leogouchon.hubscore.common.config;

import com.leogouchon.hubscore.common.security.AuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    public WebConfig(AuthorizationInterceptor authorizationInterceptor) {
        this.authorizationInterceptor = authorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/authenticate/login")
                .excludePathPatterns("/api/authenticate/signup")
                .excludePathPatterns("/api/authenticate/refresh-token")
                .excludePathPatterns("/api/ping");
    }
}
