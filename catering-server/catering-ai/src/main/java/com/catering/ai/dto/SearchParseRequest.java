package com.catering.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SearchParseRequest {

    private String query;
    private SearchContext context;
    private List<Message> messages;

    @Data
    @Builder
    public static class SearchContext {
        private String province;
        private List<String> postTypes;
        private List<String> cities;
        private List<String> districts;
        private List<String> jobTypes;
        private List<String> shopCategories;
    }

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
