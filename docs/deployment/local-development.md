# 本地开发环境

本文档说明如何启动项目本地依赖和服务。

## 1. 启动基础设施

```bash
./scripts/dev/start-infra.sh
```

该脚本会启动：

- MySQL
- Redis
- RabbitMQ
- MinIO
- Nacos

配置文件：

```text
infrastructure/docker-compose/.env
```

如果 `.env` 不存在，脚本会自动从 `.env.example` 复制。

## 2. 启动 Java 服务

```bash
./scripts/dev/start-java.sh
```

Java 服务目录：

```text
backend-service/java-service
```

## 3. 启动 Python 服务

```bash
./scripts/dev/start-python.sh
```

Python 服务目录：

```text
backend-service/python-service
```

脚本会自动创建 `.venv`、安装依赖，并复制 `.env.example`。

## 4. 执行基础校验

```bash
./scripts/dev/check-all.sh
```

校验内容：

- Java 编译
- Java 启动模块打包
- Python 语法检查
- 采集器示例语法检查

## 5. 停止基础设施

```bash
./scripts/dev/stop-infra.sh
```

## 6. 常用地址

```text
Java Service:       http://localhost:8083
Python Service:     http://localhost:8090
Python API Docs:    http://localhost:8090/docs
Nacos:              http://localhost:8848
RabbitMQ Console:   http://localhost:15672
MinIO Console:      http://localhost:9001
MySQL:              localhost:3306
Redis:              localhost:6379
```
