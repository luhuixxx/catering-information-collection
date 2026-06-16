package com.catering.service.upload;

import com.catering.service.upload.dto.UploadTokenResponse;

public interface UploadService {
    UploadTokenResponse createUploadToken(String fileName, String contentType);
}

