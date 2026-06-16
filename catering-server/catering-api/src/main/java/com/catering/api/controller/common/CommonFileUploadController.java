package com.catering.api.controller.common;

import com.catering.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Common - Upload File")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonFileUploadController {

    private final com.catering.service.upload.ObjectStorageService objectStorageService;

    @Operation(summary = "上传图片到对象存储（MinIO）")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = objectStorageService.uploadPublic(file);
        return Result.ok(Map.of("url", url));
    }
}

