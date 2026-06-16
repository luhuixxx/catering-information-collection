package com.catering.service.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostReportRequest {
    @NotBlank(message = "请选择举报原因")
    private String reason;

    @Size(max = 256, message = "补充说明最多 256 个字")
    private String description;

    @Size(max = 512, message = "截图地址过长")
    private String evidenceImage;
}
