package com.catering.service.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RentPostUpsertRequest {
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

    @NotNull(message = "面积不能为空")
    private Integer areaSqm;
    private Integer rentMonthly;
    private Integer rentNegotiable;
    private Integer entryFee;
    private Integer entryFeeNegotiable;
    private Integer canCatering;
    private Integer canOpenFlame;
    private String floorDesc;
    private String publisherIdentity;
}
