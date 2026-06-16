package com.catering.service.post.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingPostVO {
    private Long id;
    private String postNo;
    private String postType;
    private String title;
    private Long cityId;
    private Long districtId;
    private Long publisherUserId;
    private LocalDateTime createdAt;
}
