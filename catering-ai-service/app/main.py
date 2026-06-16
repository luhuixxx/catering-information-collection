from fastapi import FastAPI

from app.api.v1 import health as health_api
from app.api.v1 import search as search_api
from app.core.config import settings

app = FastAPI(title=settings.app_name)

app.include_router(health_api.router)
app.include_router(search_api.router)
