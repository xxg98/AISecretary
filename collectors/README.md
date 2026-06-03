# Collectors

`collectors` 用于存放 AI 秘书的外部数据采集器、MCP Server、主动式数据采集工具和第三方系统适配器。

## 设计目标

当秘书需要获取外部业务数据时，不直接在 Java 或 Python 主服务中硬编码第三方系统逻辑，而是通过采集器完成隔离：

```text
AI 秘书
  ↓
任务编排 / 工具选择
  ↓
采集器调度层
  ↓
具体采集器：财务软件、ERP、CRM、OA、邮箱、网盘、数据库等
  ↓
结构化数据返回给秘书
```

例如：

> 用户问：帮我查一下本月应收账款情况。

系统可以选择：

```text
finance collector -> 调用财务软件接口/数据库/RPA -> 返回应收账款结构化数据
```

## 目录结构

```text
collectors/
  mcp-servers/                 MCP Server 实现目录
  active-collectors/           主动式采集器目录
    finance/                   财务采集器示例
  shared/                      采集器通用协议、工具、认证封装
  config/                      采集器配置模板
  examples/                    示例请求、返回数据、调试脚本
```

## 采集器类型

### 1. MCP Server

适合：

- 给大模型暴露工具能力
- 支持工具发现、工具调用、资源读取
- 后续接 Cursor、Claude Desktop、自研 Agent 平台

建议放在：

```text
collectors/mcp-servers/{collector-name}
```

### 2. 主动式采集器

适合：

- 定时拉取数据
- Webhook 接收第三方推送
- 从财务软件/ERP/OA/数据库主动同步数据
- 大批量数据采集、清洗、结构化

建议放在：

```text
collectors/active-collectors/{domain-name}
```

## 和主系统的协同方式

推荐三种方式：

### HTTP

Java/Python 主服务直接调用采集器：

```text
POST /collect
```

适合短任务、实时查询。

### MQ

主服务发任务，采集器异步消费：

```text
collector.tasks.finance
collector.results
```

适合长任务、批量采集、失败重试。

### MCP

采集器以 MCP Tool 的方式暴露：

```text
get_finance_receivables
get_finance_payables
get_cash_flow
```

适合 Agent 自动选择工具。

## 返回数据规范

采集器应统一返回结构化数据：

```json
{
  "success": true,
  "collector": "finance",
  "source": "yonyou-u8",
  "trace_id": "trace-001",
  "data": {},
  "error": null
}
```

## 安全要求

- 第三方系统账号、密钥不得提交到 Git
- 每个采集器应支持独立鉴权配置
- 所有采集任务应带 `trace_id`
- 涉及财务、人事、客户数据时必须做权限校验和审计
- 采集结果进入主系统前需要做脱敏和权限过滤
