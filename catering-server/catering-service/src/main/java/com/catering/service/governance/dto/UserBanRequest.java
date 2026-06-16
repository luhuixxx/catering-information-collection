package com.catering.service.governance.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserBanRequest {
    private Integer banDays;

    @Size(max = 256, message = "封禁原因最多 256 个字")
    private String reason;

    private boolean offlinePosts;
}
