from fastapi import APIRouter

from app.schemas.task import PythonTaskRequest, PythonTaskResponse

router = APIRouter(prefix="/tasks", tags=["tasks"])


@router.post("/submit", response_model=PythonTaskResponse)
async def submit_task(request: PythonTaskRequest) -> PythonTaskResponse:
    return PythonTaskResponse(
        accepted=True,
        task_type=request.task_type,
        trace_id=request.trace_id,
        mode="http",
        message="任务已接收，后续可通过 MQ 或回调和 Java 服务协同处理。",
    )
