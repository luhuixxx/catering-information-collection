package com.catering.service.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "catering.auth")
public class AuthProperties {

    private String smsMockCode = "123456";
    private long smsExpireSeconds = 300;
}
