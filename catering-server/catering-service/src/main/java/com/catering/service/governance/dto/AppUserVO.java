package com.catering.service.governance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppUserVO {
    private String id;
    private String phone;
    private String nickname;
    private Integer violationCount;
    private LocalDateTime bannedUntil;
    private String banReason;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Long postCount;
    private Long reportCount;
    private boolean banned;
}
