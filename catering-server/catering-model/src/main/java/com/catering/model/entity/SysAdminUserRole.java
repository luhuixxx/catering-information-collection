package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin_user_role")
public class SysAdminUserRole {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("admin_user_id")
    private Long adminUserId;

    @TableField("role_id")
    private Long roleId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

