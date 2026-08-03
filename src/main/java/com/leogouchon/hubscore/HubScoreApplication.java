package com.leogouchon.hubscore;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(scanBasePackages = "com.leogouchon.hubscore")
@EnableScheduling
public class HubScoreApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(HubScoreApplication.class, args);
    }

    @Value("${hubscore.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
