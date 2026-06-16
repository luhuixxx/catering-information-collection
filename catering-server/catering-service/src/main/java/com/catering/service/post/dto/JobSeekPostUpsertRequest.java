package com.catering.service.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class JobSeekPostUpsertRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotNull(message = "城市不能为空")
    private Long cityId;
    @NotNull(message = "区县不能为空")
    private Long districtId;
    private String address;
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    private String contactWechat;
    private String description;
    private List<String> images;
    private Integer expireDays;

    @NotBlank(message = "期望岗位不能为空")
    private String desiredRoles;
    private String desiredCities;
    private String desiredDistricts;
    private Integer workYears;
    private String cuisines;
    private Integer salaryMin;
    private Integer salaryMax;
    private String gender;
    private Integer age;
    private String intro;
}
