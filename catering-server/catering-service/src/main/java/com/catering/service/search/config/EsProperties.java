package com.catering.service.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "catering.es")
public class EsProperties {
    private String baseUrl = "http://localhost:9200";
    private String indexAlias = "catering_post";
}

