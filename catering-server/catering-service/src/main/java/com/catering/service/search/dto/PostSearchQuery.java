package com.catering.service.search.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostSearchQuery {
    private String keyword;
    private String postType;
    private Long cityId;
    private Long districtId;
    private Integer minSalary;
    private Integer maxSalary;
    private String jobRole;
    private String shopCategory;
    private Boolean canCatering;
    private Boolean canOpenFlame;
    /** DEFAULT | LATEST | EXPIRE_SOON */
    private String sort;
    private int page;
    private int size;
}
