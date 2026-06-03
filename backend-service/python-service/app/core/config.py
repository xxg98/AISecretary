from functools import lru_cache
from typing import Literal

from pydantic import AnyHttpUrl, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    service_name: str = Field(default="kai-lei-python-service", alias="PYTHON_SERVICE_NAME")
    env: Literal["dev", "test", "prod"] = Field(default="dev", alias="PYTHON_ENV")
    debug: bool = Field(default=False, alias="PYTHON_DEBUG")
    host: str = Field(default="0.0.0.0", alias="PYTHON_HOST")
    port: int = Field(default=8090, alias="PYTHON_PORT")

    java_service_base_url: AnyHttpUrl = Field(default="http://localhost:8083", alias="JAVA_SERVICE_BASE_URL")
    java_service_timeout_seconds: float = Field(default=10.0, alias="JAVA_SERVICE_TIMEOUT_SECONDS")

    mq_enabled: bool = Field(default=False, alias="MQ_ENABLED")
    mq_broker: Literal["rabbitmq", "redis"] = Field(default="rabbitmq", alias="MQ_BROKER")
    mq_url: str = Field(default="amqp://guest:guest@localhost:5672/", alias="MQ_URL")
    mq_exchange: str = Field(default="kai.lei.ai.secretary", alias="MQ_EXCHANGE")
    mq_request_queue: str = Field(default="python.tasks", alias="MQ_REQUEST_QUEUE")
    mq_result_queue: str = Field(default="java.callbacks", alias="MQ_RESULT_QUEUE")

    redis_url: str = Field(default="redis://localhost:6379/1", alias="REDIS_URL")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        populate_by_name=True,
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
