package com.catering.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.AppUserMapper;
import com.catering.model.entity.AppUser;
import com.catering.service.auth.dto.AppLoginResponse;
import com.catering.service.auth.dto.SmsLoginRequest;
import com.catering.service.auth.dto.SmsSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppAuthService {

    private final AppUserMapper appUserMapper;
    private final MockSmsCodeService mockSmsCodeService;
    private final TokenIssuer tokenIssuer;

    public Map<String, Object> sendSmsCode(SmsSendRequest request) {
        mockSmsCodeService.sendCode(request.getPhone());
        Map<String, Object> result = new HashMap<>();
        result.put("phone", request.getPhone());
        result.put("message", "验证码已发送（开发环境可使用固定验证码）");
        return result;
    }

    @Transactional
    public AppLoginResponse loginBySms(SmsLoginRequest request) {
        if (!mockSmsCodeService.verify(request.getPhone(), request.getCode())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getPhone, request.getPhone())
                .last("LIMIT 1"));
        if (user == null) {
            user = new AppUser();
            user.setPhone(request.getPhone());
            user.setPhoneBound(1);
            user.setNickname(maskPhone(request.getPhone()));
            user.setAvatarUrl("");
            user.setWxOpenid(null);
            user.setBanReason("");
            user.setViolationCount(0);
            appUserMapper.insert(user);
        } else if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403, "账号已被封禁");
        }

        user.setPhoneBound(1);
        user.setLastLoginAt(LocalDateTime.now());
        appUserMapper.updateById(user);

        return AppLoginResponse.builder()
                .token(tokenIssuer.issueAppToken(user.getId()))
                .expiresIn(tokenIssuer.tokenExpireSeconds())
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "用户";
        }
        return "用户" + phone.substring(phone.length() - 4);
    }
}
