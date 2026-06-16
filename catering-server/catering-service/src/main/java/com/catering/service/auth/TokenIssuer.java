package com.catering.service.auth;

import java.util.List;

public interface TokenIssuer {

    String issueAppToken(Long userId);

    String issueAdminToken(Long userId, List<String> roles);

    long tokenExpireSeconds();
}
