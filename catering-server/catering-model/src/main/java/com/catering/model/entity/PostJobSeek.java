package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_job_seek")
public class PostJobSeek {

    @TableId("post_id")
    private Long postId;

    @TableField("desired_roles")
    private String desiredRoles;

    @TableField("desired_cities")
    private String desiredCities;

    @TableField("desired_districts")
    private String desiredDistricts;

    @TableField("work_years")
    private Integer workYears;

    @TableField("cuisines")
    private String cuisines;

    @TableField("salary_min")
    private Integer salaryMin;

    @TableField("salary_max")
    private Integer salaryMax;

    @TableField("gender")
    private String gender;

    @TableField("age")
    private Integer age;

    @TableField("intro")
    private String intro;
}

