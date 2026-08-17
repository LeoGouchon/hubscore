package com.leogouchon.husbcore;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.leogouchon.husbcore.common.config.CorsProperties;

@SpringBootApplication(scanBasePackages = "com.leogouchon.husbcore")
@EnableConfigurationProperties(CorsProperties.class)
@EnableScheduling
public class HusbcoreApplication implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public HusbcoreApplication(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(HusbcoreApplication.class, args);
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
