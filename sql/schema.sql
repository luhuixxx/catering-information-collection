-- catering-information-collection schema
-- Version: 0.1.0 (scaffold for MVP)
-- MySQL 8.x required

CREATE DATABASE IF NOT EXISTS catering
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE catering;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 0. 基础：地区（管理端动态维护）
-- =========================
DROP TABLE IF EXISTS sys_region;
CREATE TABLE sys_region (
  id            BIGINT       NOT NULL COMMENT '主键',
  parent_id     BIGINT       NULL COMMENT '父级ID，省=0',
  level         TINYINT      NOT NULL COMMENT '层级：1省 2市 3区县',
  code          VARCHAR(32)  NULL COMMENT '行政区划代码（可选）',
  name          VARCHAR(64)  NOT NULL COMMENT '名称',
  sort_no       INT          NOT NULL DEFAULT 0 COMMENT '排序',
  enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_region_parent (parent_id, sort_no),
  KEY idx_region_level_enabled (level, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区树（省/市/区县）';

-- =========================
-- 1. 用户（微信/手机号）
-- =========================
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
  id              BIGINT       NOT NULL COMMENT '主键',
  wx_openid       VARCHAR(64)  NULL COMMENT '微信openid（H5/小程序按实际接入）',
  nickname        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
  avatar_url      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '头像',
  phone           VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '手机号（绑定后写入）',
  phone_bound     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已绑定手机号',
  banned_until    DATETIME(3)  NULL COMMENT '封禁到期时间，NULL=未封禁',
  ban_reason      VARCHAR(256) NOT NULL DEFAULT '' COMMENT '封禁原因',
  violation_count INT          NOT NULL DEFAULT 0 COMMENT '违规次数（运营参考）',
  last_login_at   DATETIME(3)  NULL,
  created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_openid (wx_openid),
  KEY idx_user_phone (phone),
  KEY idx_user_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =========================
-- 2. 管理端账号（RBAC 先做最小表）
-- =========================
DROP TABLE IF EXISTS sys_admin_user;
CREATE TABLE sys_admin_user (
  id           BIGINT       NOT NULL COMMENT '主键',
  username     VARCHAR(64)  NOT NULL COMMENT '账号',
  password_hash VARCHAR(128) NOT NULL COMMENT 'BCrypt hash',
  display_name VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '显示名',
  enabled      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
  last_login_at DATETIME(3) NULL,
  created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理端账号';

DROP TABLE IF EXISTS sys_admin_role;
CREATE TABLE sys_admin_role (
  id         BIGINT      NOT NULL,
  role_key   VARCHAR(64) NOT NULL COMMENT 'ROLE_ADMIN/ROLE_AUDITOR/ROLE_OPERATOR',
  role_name  VARCHAR(64) NOT NULL COMMENT '角色名称',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理端角色';

DROP TABLE IF EXISTS sys_admin_user_role;
CREATE TABLE sys_admin_user_role (
  id           BIGINT      NOT NULL,
  admin_user_id BIGINT     NOT NULL,
  role_id      BIGINT      NOT NULL,
  created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_user_role (admin_user_id, role_id),
  KEY idx_aur_role (role_id),
  CONSTRAINT fk_aur_user FOREIGN KEY (admin_user_id) REFERENCES sys_admin_user(id),
  CONSTRAINT fk_aur_role FOREIGN KEY (role_id) REFERENCES sys_admin_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理端用户-角色';

-- =========================
-- 3. 信息帖（通用字段 + 各类型扩展表）
-- =========================
DROP TABLE IF EXISTS post;
CREATE TABLE post (
  id              BIGINT       NOT NULL COMMENT '主键',
  post_no         VARCHAR(32)  NOT NULL COMMENT '信息编号（展示/客服用）',
  post_type       VARCHAR(32)  NOT NULL COMMENT 'RECRUIT/TRANSFER/RENT/JOB_SEEK/FRANCHISE',
  status          VARCHAR(32)  NOT NULL COMMENT 'DRAFT/PENDING/APPROVED/REJECTED/EXPIRED/OFFLINE/DELETED',

  title           VARCHAR(128) NOT NULL COMMENT '标题（5-30字，扩展到128）',
  province_id     BIGINT       NOT NULL COMMENT '省（浙江省）',
  city_id         BIGINT       NOT NULL COMMENT '城市',
  district_id     BIGINT       NOT NULL COMMENT '区县',
  address         VARCHAR(256) NOT NULL DEFAULT '' COMMENT '详细地址（可空）',

  contact_name    VARCHAR(64)  NOT NULL COMMENT '联系人',
  contact_phone   VARCHAR(32)  NOT NULL COMMENT '联系电话（存明文，展示受限）',
  contact_wechat  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '微信号（可选）',

  description     VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '补充说明（0-500字，存为1024）',
  cover_image     VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '封面图（首图）',
  images_count    INT           NOT NULL DEFAULT 0 COMMENT '图片数量（冗余）',

  expire_at       DATETIME(3)   NOT NULL COMMENT '到期时间',
  renew_count     INT           NOT NULL DEFAULT 0 COMMENT '续期次数（每帖最多1）',
  renewed_at      DATETIME(3)   NULL COMMENT '续期时间',

  is_top          TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶',
  top_until       DATETIME(3)   NULL COMMENT '置顶结束时间（运营设置）',

  publisher_user_id BIGINT      NOT NULL COMMENT '发布者用户ID',

  verified_tag    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否标记已核实（运营）',

  created_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_post_no (post_no),
  KEY idx_post_type_status_city (post_type, status, city_id),
  KEY idx_post_city_district (city_id, district_id),
  KEY idx_post_expire (status, expire_at),
  KEY idx_post_publisher (publisher_user_id, created_at),
  KEY idx_post_top (is_top, top_until),
  CONSTRAINT fk_post_province FOREIGN KEY (province_id) REFERENCES sys_region(id),
  CONSTRAINT fk_post_city FOREIGN KEY (city_id) REFERENCES sys_region(id),
  CONSTRAINT fk_post_district FOREIGN KEY (district_id) REFERENCES sys_region(id),
  CONSTRAINT fk_post_user FOREIGN KEY (publisher_user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息帖（通用字段）';

DROP TABLE IF EXISTS post_image;
CREATE TABLE post_image (
  id         BIGINT       NOT NULL,
  post_id    BIGINT       NOT NULL,
  url        VARCHAR(512) NOT NULL,
  sort_no    INT          NOT NULL DEFAULT 0,
  created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_post_image_post (post_id, sort_no),
  CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子图片';

-- 3.1 招聘 RECRUIT
DROP TABLE IF EXISTS post_recruit;
CREATE TABLE post_recruit (
  post_id           BIGINT      NOT NULL,
  job_role          VARCHAR(32) NOT NULL COMMENT '岗位枚举',
  job_role_other    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '岗位=其他时',
  shop_category     VARCHAR(32) NOT NULL DEFAULT '' COMMENT '门店类型/品类',
  salary_type       VARCHAR(16) NOT NULL COMMENT 'MONTHLY/NEGOTIABLE',
  salary_min        INT         NULL,
  salary_max        INT         NULL,
  provide_board     TINYINT     NOT NULL DEFAULT 0 COMMENT '包吃住',
  headcount         INT         NOT NULL DEFAULT 1 COMMENT '招聘人数',
  exp_requirement   VARCHAR(32) NOT NULL DEFAULT '' COMMENT '年限要求',
  age_requirement   VARCHAR(32) NOT NULL DEFAULT '' COMMENT '年龄要求',
  cuisines          VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜系（逗号分隔占位）',
  work_time_desc    VARCHAR(128) NOT NULL DEFAULT '' COMMENT '工作时间描述',
  PRIMARY KEY (post_id),
  CONSTRAINT fk_recruit_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘扩展';

-- 3.2 转让 TRANSFER
DROP TABLE IF EXISTS post_transfer;
CREATE TABLE post_transfer (
  post_id          BIGINT      NOT NULL,
  shop_category    VARCHAR(32) NOT NULL COMMENT '经营类型',
  area_sqm         INT         NOT NULL COMMENT '面积㎡',
  rent_monthly     INT         NULL COMMENT '月租（数值；面议则为空）',
  rent_negotiable  TINYINT     NOT NULL DEFAULT 0,
  transfer_fee     INT         NULL COMMENT '转让费（数值；面议则为空）',
  fee_negotiable   TINYINT     NOT NULL DEFAULT 0,
  include_equipment TINYINT    NOT NULL COMMENT '是否带设备：1整体 0空转',
  operating        TINYINT     NOT NULL COMMENT '是否营业中：1营业 0停业',
  revenue_desc     VARCHAR(256) NOT NULL DEFAULT '' COMMENT '流水/盈利描述',
  reason           VARCHAR(256) NOT NULL DEFAULT '' COMMENT '转让原因',
  PRIMARY KEY (post_id),
  CONSTRAINT fk_transfer_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转让扩展';

-- 3.3 出租 RENT
DROP TABLE IF EXISTS post_rent;
CREATE TABLE post_rent (
  post_id         BIGINT      NOT NULL,
  area_sqm        INT         NOT NULL,
  rent_monthly    INT         NULL,
  rent_negotiable TINYINT     NOT NULL DEFAULT 0,
  entry_fee       INT         NULL COMMENT '入场费/转让费（可为空）',
  entry_fee_negotiable TINYINT NOT NULL DEFAULT 0,
  can_catering    TINYINT     NOT NULL COMMENT '是否可餐饮',
  can_open_flame  TINYINT     NOT NULL COMMENT '是否可明火',
  floor_desc      VARCHAR(32) NOT NULL DEFAULT '' COMMENT '楼层描述',
  publisher_identity VARCHAR(16) NOT NULL COMMENT 'OWNER/SUBLEASE',
  PRIMARY KEY (post_id),
  CONSTRAINT fk_rent_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出租扩展';

-- 3.4 求职 JOB_SEEK
DROP TABLE IF EXISTS post_job_seek;
CREATE TABLE post_job_seek (
  post_id         BIGINT       NOT NULL,
  desired_roles   VARCHAR(128) NOT NULL COMMENT '期望岗位（逗号分隔占位）',
  desired_cities  VARCHAR(128) NOT NULL COMMENT '期望城市（regionId逗号分隔占位）',
  desired_districts VARCHAR(256) NOT NULL DEFAULT '' COMMENT '期望区县（可空）',
  work_years      INT          NULL COMMENT '工作年限',
  cuisines        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '擅长菜系',
  salary_min      INT          NULL,
  salary_max      INT          NULL,
  gender          VARCHAR(8)   NOT NULL DEFAULT '' COMMENT 'M/F/UNKNOWN',
  age             INT          NULL,
  intro           VARCHAR(512) NOT NULL DEFAULT '' COMMENT '个人简介',
  PRIMARY KEY (post_id),
  CONSTRAINT fk_job_seek_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求职扩展';

-- 3.5 招商加盟 FRANCHISE
DROP TABLE IF EXISTS post_franchise;
CREATE TABLE post_franchise (
  post_id        BIGINT       NOT NULL,
  brand_name     VARCHAR(64)  NOT NULL COMMENT '品牌/项目名称',
  category       VARCHAR(32)  NOT NULL COMMENT '品类',
  investment_desc VARCHAR(64) NOT NULL DEFAULT '' COMMENT '投资金额描述',
  franchise_desc VARCHAR(1024) NOT NULL COMMENT '加盟说明',
  PRIMARY KEY (post_id),
  CONSTRAINT fk_franchise_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招商加盟扩展';

-- =========================
-- 4. 审核与治理
-- =========================
DROP TABLE IF EXISTS post_audit_record;
CREATE TABLE post_audit_record (
  id            BIGINT       NOT NULL,
  post_id       BIGINT       NOT NULL,
  action        VARCHAR(16)  NOT NULL COMMENT 'APPROVE/REJECT/OFFLINE',
  reason_code   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '原因模板key',
  reason_text   VARCHAR(256) NOT NULL DEFAULT '' COMMENT '补充说明',
  operator_admin_id BIGINT   NOT NULL COMMENT '操作人',
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_audit_post (post_id, created_at),
  KEY idx_audit_operator (operator_admin_id, created_at),
  CONSTRAINT fk_audit_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_audit_admin FOREIGN KEY (operator_admin_id) REFERENCES sys_admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录（不可篡改）';

DROP TABLE IF EXISTS post_report;
CREATE TABLE post_report (
  id            BIGINT       NOT NULL,
  post_id       BIGINT       NOT NULL,
  reporter_user_id BIGINT    NOT NULL,
  reason        VARCHAR(32)  NOT NULL COMMENT 'FAKE/SCAM/SPAM/ABUSE/OTHER',
  description   VARCHAR(256) NOT NULL DEFAULT '' COMMENT '补充说明',
  evidence_image VARCHAR(512) NOT NULL DEFAULT '' COMMENT '截图（单张占位）',
  status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE',
  handled_by_admin_id BIGINT NULL,
  handled_action VARCHAR(16) NOT NULL DEFAULT '' COMMENT 'IGNORE/OFFLINE/BAN',
  handled_note  VARCHAR(256) NOT NULL DEFAULT '' COMMENT '处理备注',
  handled_at    DATETIME(3)  NULL,
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_unique (post_id, reporter_user_id),
  KEY idx_report_status_created (status, created_at),
  CONSTRAINT fk_report_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_report_user FOREIGN KEY (reporter_user_id) REFERENCES app_user(id),
  CONSTRAINT fk_report_admin FOREIGN KEY (handled_by_admin_id) REFERENCES sys_admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报';

-- =========================
-- 5. 收藏
-- =========================
DROP TABLE IF EXISTS post_favorite;
CREATE TABLE post_favorite (
  id          BIGINT      NOT NULL,
  user_id     BIGINT      NOT NULL,
  post_id     BIGINT      NOT NULL,
  created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_fav_user_post (user_id, post_id),
  KEY idx_fav_post (post_id, created_at),
  CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_fav_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏';

-- =========================
-- 6. 系统配置/敏感词（先做最小）
-- =========================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
  id          BIGINT       NOT NULL,
  config_key  VARCHAR(64)  NOT NULL,
  config_value VARCHAR(256) NOT NULL,
  remark      VARCHAR(256) NOT NULL DEFAULT '',
  created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

DROP TABLE IF EXISTS sys_sensitive_word;
CREATE TABLE sys_sensitive_word (
  id         BIGINT       NOT NULL,
  word       VARCHAR(64)  NOT NULL,
  enabled    TINYINT      NOT NULL DEFAULT 1,
  created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sensitive_word (word),
  KEY idx_sensitive_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库';

SET FOREIGN_KEY_CHECKS = 1;
