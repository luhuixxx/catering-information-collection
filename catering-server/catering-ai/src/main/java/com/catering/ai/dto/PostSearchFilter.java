package com.catering.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class PostSearchFilter {

    @JsonAlias({"type", "post_type"})
    private String postType;

    @JsonAlias({"city", "city_name"})
    private String cityName;

    @JsonAlias({"district", "district_name"})
    private String districtName;

    @JsonAlias("city_id")
    private Long cityId;

    @JsonAlias("district_id")
    private Long districtId;

    private String keyword;

    @JsonAlias({"salary_min", "min_salary"})
    private Integer minSalary;

    @JsonAlias({"salary_max", "max_salary"})
    private Integer maxSalary;

    @JsonAlias({"job_type", "job_role"})
    private String jobRole;

    @JsonAlias({"store_type", "shop_category", "category"})
    private String shopCategory;

    @JsonAlias({"can_catering"})
    private Boolean canCatering;

    @JsonAlias({"can_open_flame"})
    private Boolean canOpenFlame;

    private String sort;
}
