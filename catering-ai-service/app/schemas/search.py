from pydantic import BaseModel, ConfigDict, Field


class SearchContext(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    province: str | None = None
    post_types: list[str] = Field(default_factory=list, alias="postTypes")
    cities: list[str] = Field(default_factory=list)
    districts: list[str] = Field(default_factory=list)
    job_types: list[str] = Field(default_factory=list, alias="jobTypes")
    shop_categories: list[str] = Field(default_factory=list, alias="shopCategories")


class SearchMessage(BaseModel):
    role: str
    content: str


class SearchParseRequest(BaseModel):
    query: str
    context: SearchContext | None = None
    messages: list[SearchMessage] = Field(default_factory=list)


class PostSearchFilter(BaseModel):
    post_type: str | None = None
    city_name: str | None = None
    district_name: str | None = None
    city_id: int | None = None
    district_id: int | None = None
    keyword: str | None = None
    min_salary: int | None = None
    max_salary: int | None = None
    job_role: str | None = None
    shop_category: str | None = None
    can_catering: bool | None = None
    can_open_flame: bool | None = None
    sort: str | None = None


class SearchParseResponse(BaseModel):
    intent: str = "search"
    filters: PostSearchFilter = Field(default_factory=PostSearchFilter)
    confidence: float = 0.0
    reply: str = ""
