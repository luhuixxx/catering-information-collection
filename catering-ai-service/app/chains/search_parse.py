"""LangChain 智能搜索解析链占位。"""

from app.schemas.search import SearchParseRequest, SearchParseResponse


def parse_search_query(request: SearchParseRequest) -> SearchParseResponse:
    # 初始化阶段：未配置 LLM 时返回 fallback，便于联调 Spring catering-ai 模块
    return SearchParseResponse(
        intent="fallback",
        filters={},
        confidence=0.0,
        reply=f"已收到查询：{request.query}（LangChain 链待实现）",
    )
