"""智能搜索解析链（无 LLM 时提供规则解析降级）。"""

from app.schemas.search import SearchParseRequest, SearchParseResponse


def parse_search_query(request: SearchParseRequest) -> SearchParseResponse:
    query = request.query or ""
    normalized = query.lower()
    filters = {}
    intent = "search" if query.strip() else "browse"
    if "招聘" in query or "招人" in query:
        filters["postType"] = "RECRUIT"
    elif "转让" in query:
        filters["postType"] = "TRANSFER"
    elif "出租" in query:
        filters["postType"] = "RENT"
    elif "求职" in query:
        filters["postType"] = "JOB_SEEK"
    elif "加盟" in query:
        filters["postType"] = "FRANCHISE"

    if "杭州" in query:
        filters["city"] = "杭州市"
    elif "宁波" in query:
        filters["city"] = "宁波市"
    elif "温州" in query:
        filters["city"] = "温州市"

    confidence = 0.62 if filters else 0.35
    reply = f"已解析查询：{query}" if query else "未输入关键词，返回默认浏览条件"
    return SearchParseResponse(
        intent=intent,
        filters=filters,
        confidence=confidence,
        reply=reply,
    )
