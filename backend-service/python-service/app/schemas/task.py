from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field


class TaskType(StrEnum):
    SPEECH_TO_TEXT = "speech_to_text"
    TEXT_TO_SPEECH = "text_to_speech"
    DOCUMENT_PARSE = "document_parse"
    IMAGE_OCR = "image_ocr"
    PYTHON_TOOL = "python_tool"


class PythonTaskRequest(BaseModel):
    task_type: TaskType
    payload: dict[str, Any] = Field(default_factory=dict)
    callback_url: str | None = None
    trace_id: str | None = None


class PythonTaskResponse(BaseModel):
    accepted: bool
    task_type: TaskType
    trace_id: str | None = None
    mode: str
    message: str
