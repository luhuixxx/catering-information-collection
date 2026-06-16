package com.catering.service.post.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PostDetailVO {
    private String id;
    private String postNo;
    private String postType;
    private String status;
    private String title;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    private String contactName;
    private String contactPhoneMasked;
    private String contactPhone;
    private String contactWechat;
    private boolean phoneVisible;
    private String phoneNotice;
    private String description;
    private String coverImage;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private Map<String, Object> ext;
}
