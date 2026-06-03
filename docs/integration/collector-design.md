# 采集器设计

采集器用于把第三方业务系统的数据安全、可控、结构化地接入 AI 秘书。

## 典型场景

用户对秘书说：

> 帮我查一下本月财务应收账款，并总结风险客户。

秘书系统执行链路：

```text
理解用户意图
  ↓
识别需要财务数据
  ↓
选择 finance collector
  ↓
调用财务软件采集器
  ↓
返回结构化应收账款数据
  ↓
AI 分析并生成回复/待办/报表
```

## 采集器分类

### MCP 采集器

以 MCP Server 方式暴露工具，适合 Agent 自动调用。

示例工具：

```text
get_finance_receivables
get_finance_payables
get_cash_flow
```

### 主动式采集器

通过定时任务、Webhook、MQ 消费等方式主动采集数据。

适合：

- 每日同步财务数据
- 定时采集销售数据
- 接收 OA 审批推送
- 采集邮件附件

## 数据返回协议

统一返回：

```json
{
  "success": true,
  "collector": "finance-demo-collector",
  "source": "demo-finance-system",
  "trace_id": "trace-001",
  "data": {},
  "error": null
}
```

## 安全原则

- 采集器只拿完成任务所需的最小权限
- 所有访问凭证放环境变量或密钥系统
- 财务、人事、客户数据必须经过权限校验
- 采集结果进入主系统前应做脱敏、审计、追踪
