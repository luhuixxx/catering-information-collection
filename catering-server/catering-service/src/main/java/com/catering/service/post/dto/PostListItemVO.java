package com.catering.service.post.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PostListItemVO {
    private String id;
    private String postNo;
    private String postType;
    private String status;
    private String title;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String summary;
    private String coverImage;
    private Integer isTop;
    private LocalDateTime topUntil;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private Map<String, Object> highlights;
}
