package com.catering.service.post.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyPostVO {
    private String id;
    private String postNo;
    private String postType;
    private String status;
    private String title;
    private String coverImage;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}
