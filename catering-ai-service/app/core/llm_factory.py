"""LLM 工厂占位，后续接入 LangChain ChatModel。"""

from app.core.config import settings


def is_llm_configured() -> bool:
    return bool(settings.llm_api_key)
