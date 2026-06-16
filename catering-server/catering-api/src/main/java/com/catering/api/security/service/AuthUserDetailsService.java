package com.catering.api.security.service;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.api.security.model.AuthUserType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class AuthUserDetailsService {

    public AuthUserPrincipal loadUser(Long userId, AuthUserType userType, List<String> roles) {
        List<String> resolvedRoles = CollectionUtils.isEmpty(roles) ? defaultRoles(userType) : roles;
        String usernamePrefix = userType == AuthUserType.ADMIN_USER ? "admin-" : "user-";
        return AuthUserPrincipal.builder()
                .userId(userId)
                .userType(userType)
                .username(usernamePrefix + userId)
                .roles(resolvedRoles)
                .build();
    }

    private List<String> defaultRoles(AuthUserType userType) {
        if (userType == AuthUserType.ADMIN_USER) {
            return List.of("ROLE_OPERATOR");
        }
        return List.of("ROLE_USER");
    }
}
