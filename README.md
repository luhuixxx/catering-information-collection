# 餐饮信息采集平台

浙江省垂直餐饮信息聚合平台（招聘、转让、出租、求职、招商加盟）。

## 工程结构

```
catering-information-collection/
├── docs/                 # PRD、TECH、设计文档
├── catering-server/      # Spring Boot 多模块后端
├── catering-ai-service/  # Python FastAPI + LangChain AI 服务
├── catering-admin/       # Vue3 + Element Plus 管理端
├── catering-app/         # UniApp H5 用户端
├── deploy/               # 部署配置
└── sql/                  # 数据库脚本
```

详见 [docs/TECH.md](docs/TECH.md)。

## 快速启动（框架验证）

### 后端

```bash
cd catering-server
mvn spring-boot:run -pl catering-api
```

健康检查：`GET http://localhost:8080/api/common/health`

### AI 服务

```bash
cd catering-ai-service
pip install -e .
uvicorn app.main:app --reload --port 8000
```

健康检查：`GET http://localhost:8000/health`

### 管理端

```bash
cd catering-admin
npm install
npm run dev
```

### 用户端 H5

```bash
cd catering-app
npm install
npm run dev:h5
```

## 文档

- [PRD](docs/PRD.md)
- [技术设计](docs/TECH.md)
- [产品设计](docs/DESIGN-catering-info-platform.md)
