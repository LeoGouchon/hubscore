package com.leogouchon.hubscore;

import com.leogouchon.hubscore.common.config.CorsProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.leogouchon.hubscore.common.config.CorsProperties;

@SpringBootApplication(scanBasePackages = "com.leogouchon.hubscore")
@EnableConfigurationProperties(CorsProperties.class)
@EnableScheduling
public class hubscoreApplication implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public hubscoreApplication(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(hubscoreApplication.class, args);
    }

    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
