# KaiLei AI Secretary Python Service

Python 服务用于补充 Java 服务在以下场景中的能力：

- 语音识别、语音合成
- 文档解析、OCR、复杂文件处理
- Python 生态 AI 工具链集成
- 长耗时任务、异步任务消费
- 后续与 Java 服务通过 HTTP 或 MQ 协同

## 技术栈

- Python 3.12
- FastAPI
- Uvicorn
- Pydantic Settings
- HTTPX
- RabbitMQ/Redis 预留

## 目录结构

```text
app/
  api/v1/                 HTTP API
  core/                   配置、日志等基础设施
  integrations/           外部系统集成，例如 Java 服务回调
  mq/                     MQ 抽象层，后续可接 RabbitMQ/Redis Stream
  schemas/                Pydantic 请求响应模型
  services/               业务服务
  tasks/                  异步任务处理
run.py                    本地启动入口
requirements.txt          Python 依赖
Dockerfile                容器构建文件
docker-compose.yml        本地容器启动示例
```

## 本地启动

```bash
cd backend-service/python-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
python run.py
```

访问：

```text
http://localhost:8090/docs
http://localhost:8090/api/v1/health
```

## Java 与 Python 协同方式

### 方式一：HTTP 微服务调用

Java 服务直接调用 Python：

```text
POST http://localhost:8090/api/v1/tasks/submit
```

示例请求：

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

适合：

- 短任务
- Java 需要同步获得接收结果
- 调试简单

### 方式二：MQ 异步协同

推荐后续用于：

- 语音识别
- 文档解析
- OCR
- 大模型长任务
- 文件批处理

建议队列：

```text
python.tasks       Java -> Python
java.callbacks     Python -> Java
```

消息格式建议：

```json
{
  "trace_id": "trace-001",
  "task_type": "speech_to_text",
  "payload": {
    "file_url": "http://localhost:8083/files/audio.wav"
  },
  "callback_url": "http://java-service/api/v1/python/callback"
}
```

## 环境变量

参考 `.env.example`。

核心变量：

```text
PYTHON_PORT=8090
JAVA_SERVICE_BASE_URL=http://localhost:8083
MQ_ENABLED=false
MQ_BROKER=rabbitmq
MQ_URL=amqp://guest:guest@localhost:5672/
```

## 当前已实现接口

```text
GET  /api/v1/health
POST /api/v1/tasks/submit
```

当前 MQ 层是抽象和日志实现，后续确认使用 RabbitMQ、Redis Stream 或 Kafka 后，可以在 `app/mq/` 下补对应 Broker 实现。
