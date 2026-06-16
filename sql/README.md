# 餐饮信息采集平台 — 数据库初始化

占位脚本，业务表结构待后续迁移实现。

## 文件说明

| 文件 | 说明 |
|------|------|
| `schema.sql` | 建库与表结构（待补充） |
| `seed.sql` | 初始数据（地区、管理员等，待补充） |

## 使用

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p catering < sql/seed.sql
```
