package com.catering.api.security.service;

import com.catering.api.security.config.SecurityProperties;
import com.catering.api.security.model.AuthUserType;
import com.catering.service.auth.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    @Override
    public String issueAppToken(Long userId) {
        return jwtTokenProvider.createToken(userId, AuthUserType.APP_USER, List.of("ROLE_USER"));
    }

    @Override
    public String issueAdminToken(Long userId, List<String> roles) {
        return jwtTokenProvider.createToken(userId, AuthUserType.ADMIN_USER, roles);
    }

    @Override
    public long tokenExpireSeconds() {
        return securityProperties.getJwt().getAccessTokenExpireSeconds();
    }
}
