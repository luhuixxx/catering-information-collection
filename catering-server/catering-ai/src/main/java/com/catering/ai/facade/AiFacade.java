package com.catering.ai.facade;

import com.catering.ai.dto.SearchParseRequest;
import com.catering.ai.dto.SearchParseResponse;

public interface AiFacade {

    SearchParseResponse parseSearchQuery(SearchParseRequest request);

    boolean isAvailable();
}
