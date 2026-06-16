package com.catering.service.region.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RegionNodeVO {

    private Long id;
    private Long parentId;
    private Integer level;
    private String code;
    private String name;
    private Integer sortNo;
    private Integer enabled;
    private List<RegionNodeVO> children = new ArrayList<>();
}
