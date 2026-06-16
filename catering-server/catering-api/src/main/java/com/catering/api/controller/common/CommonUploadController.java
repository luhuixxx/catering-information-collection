package com.catering.api.controller.common;

import com.catering.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Common - Upload")
@RestController
@RequestMapping("/api/common")
public class CommonUploadController {

    @Operation(summary = "获取上传凭证（MVP 占位）")
    @GetMapping("/upload-token")
    public Result<Map<String, Object>> uploadToken() {
        return Result.ok(Map.of(
                "provider", "mock",
                "uploadUrl", "https://mock-upload.local/object",
                "expireAt", Instant.now().plusSeconds(900).toEpochMilli(),
                "note", "阶段2使用占位凭证，阶段3接入 MinIO STS"
        ));
    }
}
