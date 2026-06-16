package com.catering.service.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RecruitPostUpsertRequest {

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

    @NotBlank(message = "岗位不能为空")
    private String jobRole;
    private String jobRoleOther;
    private String shopCategory;
    @NotBlank(message = "薪资类型不能为空")
    private String salaryType;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer provideBoard;
    private Integer headcount;
    private String expRequirement;
    private String ageRequirement;
    private String cuisines;
    private String workTimeDesc;
}
