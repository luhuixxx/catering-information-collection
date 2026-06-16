package com.catering.service.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminLoginResponse {

    private String token;
    private Long expiresIn;
    private Long userId;
    private String username;
    private String displayName;
    private List<String> roles;
}
