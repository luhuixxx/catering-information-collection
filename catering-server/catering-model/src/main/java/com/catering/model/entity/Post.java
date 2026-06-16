package com.catering.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.catering.model.enums.PostStatus;
import com.catering.model.enums.PostType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
public class Post extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_no")
    private String postNo;

    @TableField("post_type")
    private PostType postType;

    @TableField("status")
    private PostStatus status;

    @TableField("title")
    private String title;

    @TableField("province_id")
    private Long provinceId;

    @TableField("city_id")
    private Long cityId;

    @TableField("district_id")
    private Long districtId;

    @TableField("address")
    private String address;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_wechat")
    private String contactWechat;

    @TableField("description")
    private String description;

    @TableField("cover_image")
    private String coverImage;

    @TableField("images_count")
    private Integer imagesCount;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("renew_count")
    private Integer renewCount;

    @TableField("renewed_at")
    private LocalDateTime renewedAt;

    @TableField("is_top")
    private Integer isTop;

    @TableField("top_until")
    private LocalDateTime topUntil;

    @TableField("publisher_user_id")
    private Long publisherUserId;

    @TableField("verified_tag")
    private Integer verifiedTag;
}

