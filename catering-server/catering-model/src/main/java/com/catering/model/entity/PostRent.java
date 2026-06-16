package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.catering.model.enums.PublisherIdentity;
import lombok.Data;

@Data
@TableName("post_rent")
public class PostRent {

    @TableId("post_id")
    private Long postId;

    @TableField("area_sqm")
    private Integer areaSqm;

    @TableField("rent_monthly")
    private Integer rentMonthly;

    @TableField("rent_negotiable")
    private Integer rentNegotiable;

    @TableField("entry_fee")
    private Integer entryFee;

    @TableField("entry_fee_negotiable")
    private Integer entryFeeNegotiable;

    @TableField("can_catering")
    private Integer canCatering;

    @TableField("can_open_flame")
    private Integer canOpenFlame;

    @TableField("floor_desc")
    private String floorDesc;

    @TableField("publisher_identity")
    private PublisherIdentity publisherIdentity;
}

