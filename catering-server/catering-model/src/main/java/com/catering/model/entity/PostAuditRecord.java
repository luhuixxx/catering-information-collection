package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_audit_record")
public class PostAuditRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("action")
    private String action;

    @TableField("reason_code")
    private String reasonCode;

    @TableField("reason_text")
    private String reasonText;

    @TableField("operator_admin_id")
    private Long operatorAdminId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

