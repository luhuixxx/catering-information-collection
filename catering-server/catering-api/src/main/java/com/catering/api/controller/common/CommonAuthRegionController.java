package com.catering.api.controller.common;

import com.catering.common.result.Result;
import com.catering.service.auth.AppAuthService;
import com.catering.service.auth.dto.AppLoginResponse;
import com.catering.service.auth.dto.SmsLoginRequest;
import com.catering.service.auth.dto.SmsSendRequest;
import com.catering.service.region.SysRegionService;
import com.catering.service.region.dto.RegionNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Common - Auth & Region")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonAuthRegionController {

    private final AppAuthService appAuthService;
    private final SysRegionService sysRegionService;

    @Operation(summary = "发送短信验证码（Mock）")
    @PostMapping("/auth/sms/send")
    public Result<Map<String, Object>> sendSms(@Valid @RequestBody SmsSendRequest request) {
        return Result.ok(appAuthService.sendSmsCode(request));
    }

    @Operation(summary = "手机号验证码登录")
    @PostMapping("/auth/sms/login")
    public Result<AppLoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return Result.ok(appAuthService.loginBySms(request));
    }

    @Operation(summary = "地区树（仅启用）")
    @GetMapping("/regions/tree")
    public Result<List<RegionNodeVO>> regionTree() {
        return Result.ok(sysRegionService.listEnabledTree());
    }

    @Operation(summary = "下级地区列表（仅启用）")
    @GetMapping("/regions/children")
    public Result<List<RegionNodeVO>> regionChildren(@RequestParam Long parentId) {
        return Result.ok(sysRegionService.listChildren(parentId, true));
    }
}
