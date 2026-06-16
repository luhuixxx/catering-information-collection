package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_transfer")
public class PostTransfer {

    @TableId("post_id")
    private Long postId;

    @TableField("shop_category")
    private String shopCategory;

    @TableField("area_sqm")
    private Integer areaSqm;

    @TableField("rent_monthly")
    private Integer rentMonthly;

    @TableField("rent_negotiable")
    private Integer rentNegotiable;

    @TableField("transfer_fee")
    private Integer transferFee;

    @TableField("fee_negotiable")
    private Integer feeNegotiable;

    @TableField("include_equipment")
    private Integer includeEquipment;

    @TableField("operating")
    private Integer operating;

    @TableField("revenue_desc")
    private String revenueDesc;

    @TableField("reason")
    private String reason;
}

