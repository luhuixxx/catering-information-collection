from pydantic import AliasChoices, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "catering-ai-service"
    internal_api_key: str = "dev-internal-key"
    dash_scope_api_key: str = Field(
        default="",
        validation_alias=AliasChoices("DASH_SCOPE_API_KEY", "DASHSCOPE_API_KEY"),
    )
    dash_scope_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    dash_scope_model: str = "qwen-plus"
    llm_api_key: str = ""
    llm_model: str = "gpt-4o-mini"
    llm_base_url: str | None = None


settings = Settings()
