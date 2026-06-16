package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_user")
public class AppUser extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("wx_openid")
    private String wxOpenid;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("phone")
    private String phone;

    @TableField("phone_bound")
    private Integer phoneBound;

    @TableField("banned_until")
    private LocalDateTime bannedUntil;

    @TableField("ban_reason")
    private String banReason;

    @TableField("violation_count")
    private Integer violationCount;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}

