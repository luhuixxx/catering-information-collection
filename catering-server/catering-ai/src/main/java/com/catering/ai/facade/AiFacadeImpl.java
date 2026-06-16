package com.catering.ai.facade;

import com.catering.ai.client.AiServiceClient;
import com.catering.ai.config.AiProperties;
import com.catering.ai.dto.SearchParseRequest;
import com.catering.ai.dto.SearchParseResponse;
import com.catering.ai.exception.AiDegradedException;
import com.catering.ai.exception.AiServiceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AiFacadeImpl implements AiFacade {

    private final AiServiceClient aiServiceClient;
    private final AiProperties properties;

    @Override
    public SearchParseResponse parseSearchQuery(SearchParseRequest request) {
        if (!properties.isEnabled()) {
            throw new AiDegradedException("AI module disabled", null);
        }
        try {
            return aiServiceClient.parseSearch(request);
        } catch (AiServiceException ex) {
            if (properties.isDegradeOnFailure()) {
                throw new AiDegradedException(ex.getMessage(), ex);
            }
            throw ex;
        }
    }

    @Override
    public boolean isAvailable() {
        return properties.isEnabled();
    }
}
