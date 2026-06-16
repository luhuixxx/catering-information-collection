package com.catering.service.upload.impl;

import com.catering.service.upload.UploadService;
import com.catering.service.upload.config.MinioProperties;
import com.catering.service.upload.dto.UploadTokenResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioUploadServiceImpl implements UploadService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public UploadTokenResponse createUploadToken(String fileName, String contentType) {
        if (!minioProperties.isEnabled()) {
            throw new IllegalStateException("MinIO upload is disabled");
        }
        String ext = extractExt(fileName);
        String objectKey = "post/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try {
            PostPolicy policy = new PostPolicy(minioProperties.getBucket(),
                    ZonedDateTime.now().plusSeconds(minioProperties.getExpireSeconds()));
            policy.addEqualsCondition("key", objectKey);
            if (contentType != null && !contentType.isBlank()) {
                policy.addStartsWithCondition("Content-Type", contentType);
            }
            Map<String, String> formData = minioClient.getPresignedPostFormData(policy);
            String uploadUrl = minioProperties.getEndpoint() + "/" + minioProperties.getBucket();
            String objectUrl = minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectKey;
            return UploadTokenResponse.builder()
                    .provider("minio")
                    .uploadUrl(uploadUrl)
                    .uploadMethod("POST")
                    .formData(formData)
                    .objectKey(objectKey)
                    .objectUrl(objectUrl)
                    .expireAt(Instant.now().plusSeconds(minioProperties.getExpireSeconds()).toEpochMilli())
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create MinIO upload token", ex);
        }
    }

    private String extractExt(String fileName) {
        if (fileName == null) {
            return ".jpg";
        }
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

