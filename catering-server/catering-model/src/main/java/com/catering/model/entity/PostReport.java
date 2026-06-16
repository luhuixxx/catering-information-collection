package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_report")
public class PostReport {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("reporter_user_id")
    private Long reporterUserId;

    @TableField("reason")
    private String reason;

    @TableField("description")
    private String description;

    @TableField("evidence_image")
    private String evidenceImage;

    @TableField("status")
    private String status;

    @TableField("handled_by_admin_id")
    private Long handledByAdminId;

    @TableField("handled_action")
    private String handledAction;

    @TableField("handled_note")
    private String handledNote;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

