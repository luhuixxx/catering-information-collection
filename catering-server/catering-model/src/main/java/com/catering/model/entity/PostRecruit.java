package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.catering.model.enums.SalaryType;
import lombok.Data;

@Data
@TableName("post_recruit")
public class PostRecruit {

    @TableId("post_id")
    private Long postId;

    @TableField("job_role")
    private String jobRole;

    @TableField("job_role_other")
    private String jobRoleOther;

    @TableField("shop_category")
    private String shopCategory;

    @TableField("salary_type")
    private SalaryType salaryType;

    @TableField("salary_min")
    private Integer salaryMin;

    @TableField("salary_max")
    private Integer salaryMax;

    @TableField("provide_board")
    private Integer provideBoard;

    @TableField("headcount")
    private Integer headcount;

    @TableField("exp_requirement")
    private String expRequirement;

    @TableField("age_requirement")
    private String ageRequirement;

    @TableField("cuisines")
    private String cuisines;

    @TableField("work_time_desc")
    private String workTimeDesc;
}

