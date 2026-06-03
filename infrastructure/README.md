# Infrastructure

`infrastructure` 用于存放项目本地开发、测试和部署相关的基础设施配置。

## 目录结构

```text
infrastructure/
  docker-compose/     本地开发环境编排
  mysql/              MySQL 初始化脚本和配置
  redis/              Redis 配置
  rabbitmq/           RabbitMQ 配置
  minio/              MinIO 文件存储配置
  nacos/              Nacos 配置中心/注册中心配置
  nginx/              网关、反向代理配置
```

## 当前定位

该目录不直接承载业务代码，只保存运行环境相关内容，例如：

- 本地依赖服务编排
- 中间件初始化脚本
- 部署配置模板
- 网关代理配置
- 配置中心模板

## 使用建议

本地开发优先使用：

```bash
./scripts/dev/start-infra.sh
```

停止环境：

```bash
./scripts/dev/stop-infra.sh
```
