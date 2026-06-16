package com.catering.service.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppLoginResponse {

    private String token;
    private Long expiresIn;
    private Long userId;
    private String phone;
    private String nickname;
}
