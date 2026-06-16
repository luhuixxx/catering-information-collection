package com.catering.ai.config;

import com.catering.ai.client.AiServiceClient;
import com.catering.ai.facade.AiFacade;
import com.catering.ai.facade.AiFacadeImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebClient aiWebClient(AiProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(properties.getReadTimeout());
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("X-Internal-Api-Key", properties.getApiKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AiServiceClient aiServiceClient(WebClient aiWebClient, AiProperties properties) {
        return new AiServiceClient(aiWebClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AiFacade aiFacade(AiServiceClient aiServiceClient, AiProperties properties) {
        return new AiFacadeImpl(aiServiceClient, properties);
    }
}
