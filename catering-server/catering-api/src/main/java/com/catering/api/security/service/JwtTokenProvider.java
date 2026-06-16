package com.catering.api.security.service;

import com.catering.api.security.config.SecurityProperties;
import com.catering.api.security.model.AuthUserType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String HMAC_ALG = "HmacSHA256";

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public String createToken(Long userId, AuthUserType userType, List<String> roles) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + securityProperties.getJwt().getAccessTokenExpireSeconds();

            String headerJson = objectMapper.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "iss", securityProperties.getJwt().getIssuer(),
                    "sub", String.valueOf(userId),
                    "uid", userId,
                    "typ", userType.name(),
                    "roles", roles,
                    "iat", now,
                    "exp", exp
            ));

            String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("failed to create token", ex);
        }
    }

    public TokenClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String signedData = parts[0] + "." + parts[1];
            String expectedSign = sign(signedData);
            if (!expectedSign.equals(parts[2])) {
                return null;
            }

            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
            long exp = Long.parseLong(String.valueOf(claims.get("exp")));
            if (Instant.now().getEpochSecond() >= exp) {
                return null;
            }

            Long userId = Long.parseLong(String.valueOf(claims.get("uid")));
            AuthUserType userType = AuthUserType.valueOf(String.valueOf(claims.get("typ")));
            List<String> roles = objectMapper.convertValue(claims.get("roles"), new TypeReference<>() {
            });

            return new TokenClaims(userId, userType, roles);
        } catch (Exception ex) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALG);
        SecretKeySpec keySpec = new SecretKeySpec(
                securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8),
                HMAC_ALG
        );
        mac.init(keySpec);
        byte[] sign = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(sign);
    }

    private String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private byte[] base64UrlDecode(String input) {
        return Base64.getUrlDecoder().decode(input);
    }

    public record TokenClaims(Long userId, AuthUserType userType, List<String> roles) {
    }
}

