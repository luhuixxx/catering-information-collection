package com.catering.service.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostAuditActionRequest {
    @NotBlank(message = "审核动作不能为空")
    private String action;
    private String reasonCode;
    private String reasonText;
}
