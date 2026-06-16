"""Search parsing chain with DashScope-first parsing and local fallback."""

import json
import re

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_openai import ChatOpenAI

from app.core.llm_factory import is_llm_configured, llm_api_key, llm_base_url, llm_model
from app.schemas.search import PostSearchFilter, SearchParseRequest, SearchParseResponse


POST_TYPE_KEYWORDS = {
    "RECRUIT": ("招聘", "招人", "找人", "服务员", "传菜员", "收银员", "店长", "大厨", "厨师", "洗碗"),
    "TRANSFER": ("转让", "转店", "接手", "生意转"),
    "RENT": ("出租", "铺面", "门面", "商铺", "明火", "可餐饮"),
    "JOB_SEEK": ("求职", "找工作", "应聘"),
    "FRANCHISE": ("加盟", "招商", "品牌"),
}


def parse_search_query(request: SearchParseRequest) -> SearchParseResponse:
    if is_llm_configured():
        try:
            return parse_with_llm(request)
        except Exception:
            return parse_with_rules(request)
    return parse_with_rules(request)


def parse_with_llm(request: SearchParseRequest) -> SearchParseResponse:
    query = (request.query or "").strip()
    if not query:
        return empty_query_response()

    chat = ChatOpenAI(
        api_key=llm_api_key(),
        base_url=llm_base_url(),
        model=llm_model(),
        temperature=0,
        timeout=8,
    ).bind_tools([search_posts_tool_schema()], tool_choice="auto")
    response = chat.invoke(
        [
            SystemMessage(content=system_prompt()),
            *history_messages(request),
            HumanMessage(content=json.dumps(llm_payload(request), ensure_ascii=False)),
        ]
    )
    parsed = parse_tool_response(response)
    return normalize_llm_response(parsed, query)


def parse_with_rules(request: SearchParseRequest) -> SearchParseResponse:
    query = (request.query or "").strip()
    if not query:
        return empty_query_response()

    context = request.context
    filters = PostSearchFilter(sort="DEFAULT")
    filters.post_type = infer_post_type(query)
    filters.city_name = first_contained(query, context.cities if context else [])
    filters.district_name = first_contained(query, context.districts if context else [])
    filters.job_role = first_contained(query, context.job_types if context else [])
    filters.shop_category = first_contained(query, context.shop_categories if context else [])
    filters.min_salary, filters.max_salary = infer_amount_range(query)
    filters.can_catering = True if "可餐饮" in query or "餐饮" in query else None
    filters.can_open_flame = True if "明火" in query else None

    matched = sum(
        value is not None
        for value in (
            filters.post_type,
            filters.city_name,
            filters.district_name,
            filters.job_role,
            filters.shop_category,
            filters.min_salary,
            filters.max_salary,
            filters.can_catering,
            filters.can_open_flame,
        )
    )
    if matched == 0:
        filters.keyword = query
    confidence = min(0.9, 0.35 + matched * 0.08)
    return SearchParseResponse(
        intent="search",
        filters=filters,
        confidence=confidence,
        reply=build_reply(filters),
    )


def empty_query_response() -> SearchParseResponse:
    return SearchParseResponse(
        intent="fallback",
        confidence=0.0,
        reply="请输入想找的信息，例如“杭州大厨 8000 以上”。",
    )


def system_prompt() -> str:
    return (
        "你是餐饮信息平台的对话式找信息助手。你的任务不是闲聊，而是把用户需求转成搜索工具参数。"
        "优先调用 search_posts 工具；如果确实不是搜索需求，再直接返回 fallback/chitchat JSON。"
        "工具只生成搜索条件，不会由你直接查询数据库。Java 服务会拿工具参数查询 ES 并返回信息卡片。"
        "intent 只能是 search、fallback 或 chitchat。非搜索意图用 fallback。"
        "post_type 只能是 RECRUIT、TRANSFER、RENT、JOB_SEEK、FRANCHISE 或 null。"
        "只使用用户原话或最近对话明确给出的约束，不要从候选列表里臆测条件。"
        "如果已解析出城市、岗位、薪资、类型等结构化条件，不要把整句原文放入 keyword；"
        "只有无法结构化理解时才把原文作为 keyword。"
        "sort 默认 DEFAULT。confidence 取 0 到 1。reply 用简短中文说明已理解的条件。"
    )


def llm_payload(request: SearchParseRequest) -> dict:
    context = request.context
    return {
        "query": request.query,
        "conversation": conversation_summary(request),
        "context": context.model_dump(by_alias=True) if context else {},
        "tool_name": "search_posts",
        "output_example": {
            "intent": "search",
            "filters": {
                "post_type": "RECRUIT",
                "city_name": "杭州市",
                "district_name": None,
                "city_id": None,
                "district_id": None,
                "keyword": None,
                "min_salary": 8000,
                "max_salary": None,
                "job_role": "大厨",
                "shop_category": None,
                "can_catering": None,
                "can_open_flame": None,
                "sort": "DEFAULT",
            },
            "confidence": 0.86,
            "reply": "正在为你筛选：杭州市、大厨、8000以上",
        },
    }


def search_posts_tool_schema() -> dict:
    return {
        "type": "function",
        "function": {
            "name": "search_posts",
            "description": "把餐饮信息搜索需求解析为结构化筛选条件，由 Java 服务继续查询 ES。",
            "parameters": {
                "type": "object",
                "properties": {
                    "intent": {"type": "string", "enum": ["search", "fallback", "chitchat"]},
                    "filters": {
                        "type": "object",
                        "properties": {
                            "post_type": {"type": "string", "enum": ["RECRUIT", "TRANSFER", "RENT", "JOB_SEEK", "FRANCHISE"]},
                            "city_name": {"type": "string"},
                            "district_name": {"type": "string"},
                            "city_id": {"type": "integer"},
                            "district_id": {"type": "integer"},
                            "keyword": {"type": "string"},
                            "min_salary": {"type": "integer"},
                            "max_salary": {"type": "integer"},
                            "job_role": {"type": "string"},
                            "shop_category": {"type": "string"},
                            "can_catering": {"type": "boolean"},
                            "can_open_flame": {"type": "boolean"},
                            "sort": {"type": "string", "enum": ["DEFAULT", "LATEST", "EXPIRE_SOON"]},
                        },
                    },
                    "confidence": {"type": "number"},
                    "reply": {"type": "string"},
                },
                "required": ["intent", "filters", "confidence", "reply"],
            },
        },
    }


def history_messages(request: SearchParseRequest) -> list:
    messages = []
    for item in (request.messages or [])[-8:]:
        role = (item.role or "").lower()
        content = (item.content or "").strip()
        if not content:
            continue
        if role == "assistant":
            messages.append(AIMessage(content=content))
        else:
            messages.append(HumanMessage(content=content))
    return messages


def conversation_summary(request: SearchParseRequest) -> str:
    if not request.messages:
        return "无历史对话。"
    lines = []
    for item in request.messages[-6:]:
        role = "用户" if (item.role or "").lower() != "assistant" else "助手"
        lines.append(f"{role}：{(item.content or '').strip()}")
    return "\n".join(lines)


def parse_tool_response(response) -> SearchParseResponse:
    tool_calls = getattr(response, "tool_calls", None) or []
    if tool_calls:
        args = tool_calls[0].get("args") or {}
        return SearchParseResponse.model_validate(args)
    content = response.content if isinstance(response.content, str) else json.dumps(response.content, ensure_ascii=False)
    return SearchParseResponse.model_validate_json(extract_json(content))


def extract_json(content: str) -> str:
    text = content.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*", "", text)
        text = re.sub(r"\s*```$", "", text)
    start = text.find("{")
    end = text.rfind("}")
    if start >= 0 and end >= start:
        return text[start : end + 1]
    return text


def normalize_llm_response(response: SearchParseResponse, raw_query: str) -> SearchParseResponse:
    response.intent = response.intent if response.intent in {"search", "fallback", "chitchat"} else "fallback"
    response.confidence = max(0.0, min(response.confidence or 0.0, 1.0))
    response.filters = response.filters or PostSearchFilter()
    remove_unsupported_filters(response.filters, raw_query)
    if not response.filters.sort:
        response.filters.sort = "DEFAULT"
    if has_structured_filters(response.filters):
        response.filters.keyword = None
    elif not response.filters.keyword and response.intent == "search":
        response.filters.keyword = raw_query
    if response.intent == "search":
        response.reply = build_reply(response.filters)
    return response


def remove_unsupported_filters(filters: PostSearchFilter, raw_query: str) -> None:
    if filters.city_name and not contains_candidate(raw_query, filters.city_name):
        filters.city_name = None
    if filters.district_name and not contains_candidate(raw_query, filters.district_name):
        filters.district_name = None
    if filters.job_role and not contains_candidate(raw_query, filters.job_role):
        filters.job_role = None
    if filters.shop_category and not contains_candidate(raw_query, filters.shop_category):
        filters.shop_category = None
    if filters.can_catering is True and not ("可餐饮" in raw_query or "餐饮" in raw_query):
        filters.can_catering = None
    if filters.can_open_flame is True and "明火" not in raw_query:
        filters.can_open_flame = None


def contains_candidate(query: str, candidate: str) -> bool:
    text = (candidate or "").strip()
    return bool(text) and (
        text in query
        or text.removesuffix("市") in query
        or text.removesuffix("区") in query
        or text.removesuffix("县") in query
    )


def infer_post_type(query: str) -> str | None:
    for post_type, keywords in POST_TYPE_KEYWORDS.items():
        if any(keyword in query for keyword in keywords):
            return post_type
    return None


def first_contained(query: str, candidates: list[str]) -> str | None:
    for candidate in candidates:
        text = (candidate or "").strip()
        if text and (text in query or text.removesuffix("市") in query or text.removesuffix("区") in query or text.removesuffix("县") in query):
            return text
    return None


def infer_amount_range(query: str) -> tuple[int | None, int | None]:
    numbers = [int(match) for match in re.findall(r"\d{3,6}", query)]
    if not numbers:
        return None, None
    value = numbers[0]
    if any(word in query for word in ("以上", "起", "不少于", "最低")):
        return value, None
    if any(word in query for word in ("以下", "以内", "最高", "不超过")):
        return None, value
    if len(numbers) >= 2:
        return min(numbers[0], numbers[1]), max(numbers[0], numbers[1])
    return value, None


def build_reply(filters: PostSearchFilter) -> str:
    parts = []
    if filters.city_name:
        parts.append(filters.city_name)
    if filters.district_name:
        parts.append(filters.district_name)
    if filters.job_role:
        parts.append(filters.job_role)
    if filters.shop_category:
        parts.append(filters.shop_category)
    if filters.min_salary:
        parts.append(f"{filters.min_salary}以上")
    if filters.can_open_flame:
        parts.append("可明火")
    if filters.can_catering:
        parts.append("可餐饮")
    if not parts:
        return "已按你的描述进行关键词搜索。"
    return "正在为你筛选：" + "、".join(parts)


def has_structured_filters(filters: PostSearchFilter) -> bool:
    return any(
        value is not None and value != ""
        for value in (
            filters.post_type,
            filters.city_name,
            filters.district_name,
            filters.city_id,
            filters.district_id,
            filters.min_salary,
            filters.max_salary,
            filters.job_role,
            filters.shop_category,
            filters.can_catering,
            filters.can_open_flame,
        )
    )
