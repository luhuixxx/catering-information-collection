package com.catering.service.search.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostSearchResult {
    private List<Long> ids;
    private long total;
}
