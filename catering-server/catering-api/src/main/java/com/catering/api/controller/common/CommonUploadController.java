package com.catering.api.controller.common;

import com.catering.common.result.Result;
import com.catering.service.upload.UploadService;
import com.catering.service.upload.dto.UploadTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common - Upload")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonUploadController {

    private final UploadService uploadService;

    @Operation(summary = "获取 MinIO 直传凭证")
    @GetMapping("/upload-token")
    public Result<UploadTokenResponse> uploadToken(@RequestParam(defaultValue = "image.jpg") String fileName,
                                                   @RequestParam(defaultValue = "image/jpeg") String contentType) {
        return Result.ok(uploadService.createUploadToken(fileName, contentType));
    }
}
