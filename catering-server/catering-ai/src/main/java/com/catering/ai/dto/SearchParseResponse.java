package com.catering.ai.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SearchParseResponse {

    private String intent;
    private Map<String, Object> filters;
    private Double confidence;
    private String reply;
}
