from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.v1.router import api_router
from app.core.config import get_settings
from app.core.logging import configure_logging

settings = get_settings()
configure_logging(settings.debug)


@asynccontextmanager
async def lifespan(app: FastAPI):
    yield


app = FastAPI(
    title="KaiLei AI Secretary Python Service",
    version="0.1.0",
    description="用于补充 Java 在语音、文档解析、AI 工具链上的能力的 Python 微服务。",
    lifespan=lifespan,
)
app.include_router(api_router)
