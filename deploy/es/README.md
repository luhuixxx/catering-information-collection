# Elasticsearch 索引初始化

本目录提供「信息帖检索索引」的创建命令与 mapping。

## 创建索引（Windows PowerShell）

```powershell
.\deploy\es\create-post-index.ps1 -EsUrl http://localhost:9200
```

默认：

- Index: `catering_post_v1`
- Alias: `catering_post`

## 说明

- 本 mapping 为 **MVP 骨架**：字段覆盖 PRD 中的主要检索维度（地区、类型、关键字段）。
- 中文分词（如 IK）属于部署能力，未在 mapping 中强依赖；后续若安装 IK 插件，可把 `title/description/address` 的 analyzer 调整为 `ik_max_word / ik_smart`。

