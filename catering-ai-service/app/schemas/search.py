from typing import Any

from pydantic import BaseModel, Field


class SearchContext(BaseModel):
    province: str | None = None
    post_types: list[str] = Field(default_factory=list)
    cities: list[str] = Field(default_factory=list)
    job_types: list[str] = Field(default_factory=list)


class SearchParseRequest(BaseModel):
    query: str
    context: SearchContext | None = None


class SearchParseResponse(BaseModel):
    intent: str = "search"
    filters: dict[str, Any] = Field(default_factory=dict)
    confidence: float = 0.0
    reply: str = ""
