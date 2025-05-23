package com.leogouchon.squashapp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(scanBasePackages = "com.leogouchon.squashapp")
public class SquashappApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(SquashappApplication.class, args);
    }

    @PostConstruct
    public void checkProfile() {
        System.out.println("🧭 Profil actif : " + activeProfile);
    }

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if ("prod".equalsIgnoreCase(activeProfile)) {
            registry.addMapping("/api/**")
                    .allowedOrigins(
                            "https://squash.leogouchon.com",
                            "https://squash.leogouchon.com/",
                            "https://www.squash.leogouchon.com",
                            "https://www.squash.leogouchon.com/"

                    )
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        } else {
            registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}
