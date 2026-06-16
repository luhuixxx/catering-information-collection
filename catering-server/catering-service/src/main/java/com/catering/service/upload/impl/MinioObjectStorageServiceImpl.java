package com.catering.service.upload.impl;

import com.catering.common.exception.BusinessException;
import com.catering.service.upload.ObjectStorageService;
import com.catering.service.upload.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioObjectStorageServiceImpl implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String uploadPublic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String fileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = extractExt(fileName);
        String objectKey = "post/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(in, file.getSize(), -1)
                    .build());
            return minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectKey;
        } catch (Exception ex) {
            throw new IllegalStateException("MinIO upload failed", ex);
        }
    }

    private String extractExt(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return ".jpg";
        }
        String ext = fileName.substring(idx);
        if (ext.length() > 10) {
            return ".jpg";
        }
        return ext.toLowerCase();
    }
}

