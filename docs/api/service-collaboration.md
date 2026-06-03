# 服务协同协议

本文档描述 Java 服务、Python 服务、采集器之间的协同方式。

## HTTP 协同

适合短任务。

```text
Java -> Python
Java -> Collector
Python -> Java Callback
Collector -> Java Callback
```

任务提交示例：

```json
{
  "task_type": "document_parse",
  "payload": {
    "file_url": "http://localhost:8083/files/demo.pdf"
  },
  "callback_url": "http://localhost:8083/api/v1/python/callback",
  "trace_id": "trace-001"
}
```

## MQ 协同

适合长任务、失败重试、削峰。

建议 Topic/Queue：

```text
python.tasks
java.callbacks
collector.tasks.finance
collector.results
```

## MCP 协同

适合 Agent 自动选择工具。

工具命名建议：

```text
{domain}_{action}
```

示例：

```text
finance_get_receivables
finance_get_cash_flow
crm_get_customer_profile
```

## Trace ID

所有跨服务请求必须携带 `trace_id`，用于：

- 日志追踪
- 异步任务回调
- 用户操作审计
- 问题排查
