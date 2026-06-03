# 系统总览

凯磊 AI 秘书系统按能力边界拆分为多个部分：

```text
frontend-app / frontend-pc
  ↓
Java 后端服务
  ↓
Python 能力服务
  ↓
Collectors 采集器 / MCP Tools / 第三方系统
```

## Java 服务职责

- 用户、角色、权限
- AI 秘书核心业务
- 待办、消息、文件元数据
- WebSocket 实时消息
- 文件存储管理
- 对外 API 网关式入口

## Python 服务职责

- 语音识别、语音合成
- 文档解析、OCR
- Python AI 工具链
- 长耗时任务处理
- 与 Java 通过 HTTP 或 MQ 协同

## Collectors 职责

- 对接财务软件、ERP、CRM、OA、邮箱、网盘等外部系统
- 封装 MCP Tool 或主动式采集器
- 将外部系统数据结构化返回给秘书系统

## 协同建议

短任务优先 HTTP：

```text
Java -> Python / Collector -> Java
```

长任务优先 MQ：

```text
Java 发布任务 -> Python/Collector 消费 -> 回调 Java 或发送结果消息
```

Agent 工具调用优先 MCP：

```text
AI 秘书 -> MCP Tool -> 第三方系统数据
```
