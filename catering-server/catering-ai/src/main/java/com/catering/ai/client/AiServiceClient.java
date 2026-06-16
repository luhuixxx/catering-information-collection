package com.catering.ai.client;

import com.catering.ai.config.AiProperties;
import com.catering.ai.dto.SearchParseRequest;
import com.catering.ai.dto.SearchParseResponse;
import com.catering.ai.exception.AiServiceException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

public class AiServiceClient {

    private final WebClient aiWebClient;
    private final AiProperties properties;

    public AiServiceClient(WebClient aiWebClient, AiProperties properties) {
        this.aiWebClient = aiWebClient;
        this.properties = properties;
    }

    public SearchParseResponse parseSearch(SearchParseRequest request) {
        try {
            return aiWebClient.post()
                    .uri("/v1/search/parse")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SearchParseResponse.class)
                    .block(properties.getReadTimeout().plus(Duration.ofSeconds(1)));
        } catch (WebClientResponseException ex) {
            throw new AiServiceException("AI service error: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            throw new AiServiceException("AI service unavailable", ex);
        }
    }
}
