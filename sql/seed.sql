-- seed data placeholder
USE catering;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 地区：浙江省（最小可用数据）
-- 说明：后续由管理端继续维护市/区县；这里仅提供省根节点，避免“写死在代码里”
-- =========================
INSERT INTO sys_region (id, parent_id, level, code, name, sort_no, enabled, deleted)
VALUES (330000, 0, 1, '330000', '浙江省', 0, 1, 0)
ON DUPLICATE KEY UPDATE
  parent_id=VALUES(parent_id),
  level=VALUES(level),
  code=VALUES(code),
  name=VALUES(name),
  sort_no=VALUES(sort_no),
  enabled=VALUES(enabled),
  deleted=VALUES(deleted);

-- =========================
-- 管理端角色（最小 RBAC）
-- =========================
INSERT INTO sys_admin_role (id, role_key, role_name, deleted)
VALUES
  (9001, 'ROLE_ADMIN', '管理员', 0),
  (9002, 'ROLE_AUDITOR', '审核员', 0),
  (9003, 'ROLE_OPERATOR', '运营', 0)
ON DUPLICATE KEY UPDATE
  role_name=VALUES(role_name),
  deleted=VALUES(deleted);

-- =========================
-- 系统配置（MVP 默认值）
-- =========================
INSERT INTO sys_config (id, config_key, config_value, remark, deleted)
VALUES
  (10001, 'post.expire.default_days', '15', '默认有效期（天）', 0),
  (10002, 'post.expire.options', '7,15,30', '发布可选有效期（天）', 0),
  (10003, 'post.renew.days', '15', '一键续期延长天数', 0),
  (10004, 'post.renew.max_count', '1', '每帖最大续期次数', 0),
  (10005, 'post.publish.daily_limit', '5', '单用户每日发布上限', 0)
ON DUPLICATE KEY UPDATE
  config_value=VALUES(config_value),
  remark=VALUES(remark),
  deleted=VALUES(deleted);

SET FOREIGN_KEY_CHECKS = 1;
