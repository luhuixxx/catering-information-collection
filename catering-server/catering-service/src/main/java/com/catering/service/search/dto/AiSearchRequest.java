package com.catering.service.search.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiSearchRequest {

    @NotBlank(message = "搜索内容不能为空")
    private String query;

    private Long cityId;
    private Long districtId;
    private Integer page = 1;
    private Integer size = 20;
    private List<Message> messages = new ArrayList<>();

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
