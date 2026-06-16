package com.catering.service.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostReportHandleRequest {
    @NotBlank(message = "请选择处理方式")
    private String action;

    private Integer banDays;

    @Size(max = 256, message = "处理备注最多 256 个字")
    private String note;
}
