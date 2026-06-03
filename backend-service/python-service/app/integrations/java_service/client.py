import httpx

from app.core.config import Settings


class JavaServiceClient:
    def __init__(self, settings: Settings) -> None:
        self._base_url = str(settings.java_service_base_url).rstrip("/")
        self._timeout = settings.java_service_timeout_seconds

    async def notify_task_result(self, path: str, payload: dict) -> dict:
        async with httpx.AsyncClient(base_url=self._base_url, timeout=self._timeout) as client:
            response = await client.post(path, json=payload)
            response.raise_for_status()
            return response.json()
