package com.catering.api.controller.common;

import com.catering.common.result.Result;
import com.catering.service.health.AppHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class HealthController {

    private final AppHealthService appHealthService;

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "service", appHealthService.status()
        ));
    }
}
