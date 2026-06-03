from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class MqMessage:
    routing_key: str
    payload: dict[str, Any]
    trace_id: str | None = None
