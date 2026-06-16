package com.catering.service.governance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostReportVO {
    private String id;
    private String postId;
    private String postNo;
    private String postTitle;
    private String postStatus;
    private String publisherUserId;
    private String reporterUserId;
    private String reporterPhone;
    private String reason;
    private String description;
    private String evidenceImage;
    private String status;
    private String handledAction;
    private String handledNote;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
