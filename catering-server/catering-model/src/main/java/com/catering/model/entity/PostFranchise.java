package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_franchise")
public class PostFranchise {

    @TableId("post_id")
    private Long postId;

    @TableField("brand_name")
    private String brandName;

    @TableField("category")
    private String category;

    @TableField("investment_desc")
    private String investmentDesc;

    @TableField("franchise_desc")
    private String franchiseDesc;
}

