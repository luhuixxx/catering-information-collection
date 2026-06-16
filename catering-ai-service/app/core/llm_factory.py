"""LLM factory for the Python AI service."""

from app.core.config import settings


def is_llm_configured() -> bool:
    return bool(llm_api_key())


def llm_api_key() -> str:
    return settings.dash_scope_api_key or settings.llm_api_key


def llm_base_url() -> str | None:
    return settings.dash_scope_base_url if settings.dash_scope_api_key else settings.llm_base_url


def llm_model() -> str:
    return settings.dash_scope_model if settings.dash_scope_api_key else settings.llm_model
