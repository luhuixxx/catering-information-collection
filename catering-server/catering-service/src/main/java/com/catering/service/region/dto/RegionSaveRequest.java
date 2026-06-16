package com.catering.service.region.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegionSaveRequest {

    @NotNull(message = "父级地区不能为空")
    private Long parentId;

    @NotNull(message = "层级不能为空")
    @Min(1)
    @Max(3)
    private Integer level;

    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private Integer sortNo;

    private Integer enabled;
}
