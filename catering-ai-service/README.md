# catering-ai-service

Python AI 推理服务（FastAPI + LangChain），供 Spring Boot `catering-ai` 模块 HTTP 调用。

## 本地运行

```bash
cd catering-ai-service
python -m venv .venv
.venv\Scripts\activate
pip install -e .
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

健康检查：`GET http://localhost:8000/health`
