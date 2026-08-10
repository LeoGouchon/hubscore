package com.leogouchon.hubscore;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.leogouchon.hubscore.common.config.CorsProperties;

@SpringBootApplication(scanBasePackages = "com.leogouchon.hubscore")
@EnableScheduling
public class HubScoreApplication implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public HubScoreApplication(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(HubScoreApplication.class, args);
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
