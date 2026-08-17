package com.leogouchon.husbcore.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "husbcore.cors")
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>();
}
