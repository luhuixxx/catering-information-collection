package com.catering.ai.dto;

import lombok.Data;

@Data
public class SearchParseResponse {

    private String intent;
    private PostSearchFilter filters;
    private Double confidence;
    private String reply;
}
