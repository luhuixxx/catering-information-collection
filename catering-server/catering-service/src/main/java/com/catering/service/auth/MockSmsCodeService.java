package com.catering.service.auth;

import com.catering.service.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MockSmsCodeService {

    private final AuthProperties authProperties;
    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();

    public void sendCode(String phone) {
        String code = authProperties.getSmsMockCode();
        codes.put(phone, new CodeEntry(code, Instant.now().getEpochSecond() + authProperties.getSmsExpireSeconds()));
    }

    public boolean verify(String phone, String code) {
        // Dev convenience: allow fixed mock code without a prior "send"
        if (code != null && code.equals(authProperties.getSmsMockCode())) {
            codes.remove(phone);
            return true;
        }
        CodeEntry entry = codes.get(phone);
        if (entry == null) {
            return false;
        }
        if (Instant.now().getEpochSecond() > entry.expireAt()) {
            codes.remove(phone);
            return false;
        }
        boolean ok = entry.code().equals(code);
        if (ok) {
            codes.remove(phone);
        }
        return ok;
    }

    private record CodeEntry(String code, long expireAt) {
    }
}
