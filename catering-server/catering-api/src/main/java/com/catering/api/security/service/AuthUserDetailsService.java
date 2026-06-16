package com.catering.api.security.service;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.api.security.model.AuthUserType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthUserDetailsService {

    public AuthUserPrincipal loadUser(Long userId, AuthUserType userType) {
        if (userType == AuthUserType.ADMIN_USER) {
            return AuthUserPrincipal.builder()
                    .userId(userId)
                    .userType(userType)
                    .username("admin-" + userId)
                    .roles(List.of("ROLE_ADMIN"))
                    .build();
        }
        return AuthUserPrincipal.builder()
                .userId(userId)
                .userType(userType)
                .username("user-" + userId)
                .roles(List.of("ROLE_USER"))
                .build();
    }
}

