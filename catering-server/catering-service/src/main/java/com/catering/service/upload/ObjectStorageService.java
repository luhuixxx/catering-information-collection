package com.catering.service.upload;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    String uploadPublic(MultipartFile file);
}

