package com.catering.service.search;

import com.catering.service.search.dto.AiSearchRequest;
import com.catering.service.search.dto.AiSearchResponse;

public interface SearchService {

    AiSearchResponse aiSearch(AiSearchRequest request);
}
