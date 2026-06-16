package com.catering.api.controller.app;

import com.catering.common.result.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户端基础连通性接口。
 */
@RestController
@RequestMapping("/api/app")
public class AppApiPlaceholderController {

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping(Authentication authentication) {
        return Result.ok(Map.of(
                "scope", "app",
                "authenticated", authentication != null
        ));
    }
}
