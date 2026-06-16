package com.catering.api.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "catering.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String issuer = "catering-api";
        private String secret = "replace-with-strong-secret-key";
        private long accessTokenExpireSeconds = 86400;
    }
}

