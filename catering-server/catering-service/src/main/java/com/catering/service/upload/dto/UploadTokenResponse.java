package com.catering.service.upload.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UploadTokenResponse {
    private String provider;
    private String uploadUrl;
    private String uploadMethod;
    private Map<String, String> formData;
    private String objectKey;
    private String objectUrl;
    private long expireAt;
}

