package com.catering.service.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FranchisePostUpsertRequest {
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

    @NotBlank(message = "品牌/项目名称不能为空")
    private String brandName;
    @NotBlank(message = "品类不能为空")
    private String category;
    private String investmentDesc;
    @NotBlank(message = "加盟说明不能为空")
    private String franchiseDesc;
}
