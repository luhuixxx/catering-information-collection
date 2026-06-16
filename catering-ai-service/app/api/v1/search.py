from fastapi import APIRouter, Header, HTTPException, status

from app.chains.search_parse import parse_search_query
from app.core.config import settings
from app.schemas.search import SearchParseRequest, SearchParseResponse

router = APIRouter(prefix="/v1/search", tags=["search"])


def verify_internal_key(x_internal_api_key: str | None = Header(default=None)) -> None:
    if x_internal_api_key != settings.internal_api_key:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid api key")


@router.post("/parse", response_model=SearchParseResponse)
def search_parse(
    body: SearchParseRequest,
    x_internal_api_key: str | None = Header(default=None),
) -> SearchParseResponse:
    verify_internal_key(x_internal_api_key)
    return parse_search_query(body)
