package com.catering.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "catering.ai")
public class AiProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:8000";
    private String apiKey = "";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(15);
    private boolean degradeOnFailure = true;
}
