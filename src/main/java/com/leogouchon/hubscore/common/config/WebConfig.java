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
                .excludePathPatterns("/api/v1/authenticate/login")
                .excludePathPatterns("/api/v1/authenticate/signup")
                .excludePathPatterns("/api/v1/authenticate/refresh-token")
                .excludePathPatterns("/api/v1/ping");
    }
}
