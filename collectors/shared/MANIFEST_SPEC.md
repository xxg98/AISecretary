# Collector Manifest 规范

每个采集器建议提供一个 `collector.yaml` 或 `collector.json`，用于描述采集器能力，方便秘书系统或 Agent 动态发现。

## 字段说明

```yaml
name: finance-collector
version: 0.1.0
domain: finance
type: active | mcp | hybrid
description: 财务系统采集器
capabilities:
  - name: get_receivables
    description: 查询应收账款
    input_schema: {}
    output_schema: {}
auth:
  type: api_key | oauth2 | basic | custom
runtime:
  language: python | node | java | shell
  entry: run.py
communication:
  http:
    enabled: true
    base_path: /collectors/finance
  mq:
    enabled: false
    request_topic: collector.tasks.finance
    result_topic: collector.results
  mcp:
    enabled: false
```

## 命名建议

```text
{domain}-{system}-{purpose}-collector
```

示例：

```text
finance-yonyou-u8-collector
finance-kingdee-k3-collector
crm-salesforce-collector
oa-feishu-collector
```
