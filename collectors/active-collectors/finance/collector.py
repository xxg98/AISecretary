from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class CollectRequest:
    action: str
    params: dict[str, Any]
    trace_id: str | None = None


@dataclass(frozen=True)
class CollectResult:
    success: bool
    collector: str
    source: str
    trace_id: str | None
    data: dict[str, Any] | list[dict[str, Any]] | None = None
    error: str | None = None


class FinanceCollector:
    collector_name = "finance-demo-collector"
    source = "demo-finance-system"

    def collect(self, request: CollectRequest) -> CollectResult:
        handlers = {
            "get_receivables": self.get_receivables,
            "get_payables": self.get_payables,
            "get_cash_flow": self.get_cash_flow,
        }
        handler = handlers.get(request.action)
        if handler is None:
            return CollectResult(
                success=False,
                collector=self.collector_name,
                source=self.source,
                trace_id=request.trace_id,
                error=f"Unsupported finance action: {request.action}",
            )
        return CollectResult(
            success=True,
            collector=self.collector_name,
            source=self.source,
            trace_id=request.trace_id,
            data=handler(request.params),
        )

    def get_receivables(self, params: dict[str, Any]) -> dict[str, Any]:
        return {
            "period": params.get("period", "current_month"),
            "total_amount": 0,
            "currency": "CNY",
            "items": [],
        }

    def get_payables(self, params: dict[str, Any]) -> dict[str, Any]:
        return {
            "period": params.get("period", "current_month"),
            "total_amount": 0,
            "currency": "CNY",
            "items": [],
        }

    def get_cash_flow(self, params: dict[str, Any]) -> dict[str, Any]:
        return {
            "period": params.get("period", "current_month"),
            "cash_in": 0,
            "cash_out": 0,
            "net_cash_flow": 0,
            "currency": "CNY",
        }
