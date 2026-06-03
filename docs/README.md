# Docs

`docs` 是项目级文档目录，用于沉淀产品、架构、接口、部署、集成方案等文档。

## 目录结构

```text
docs/
  architecture/       系统架构、服务拆分、技术选型
  api/                API 协议、接口说明、回调协议
  deployment/         部署、环境变量、Docker、Nacos、数据库等
  integration/        第三方系统集成、MCP、采集器、MQ 协同
  product/            产品需求、业务流程、用户故事
```

## 文档维护约定

- 重要方案先写到 `docs/architecture`
- 接口协议写到 `docs/api`
- 第三方系统、采集器、MCP 相关写到 `docs/integration`
- 部署说明写到 `docs/deployment`
- 产品需求和业务流程写到 `docs/product`

## 当前推荐阅读顺序

1. `architecture/system-overview.md`
2. `integration/collector-design.md`
3. `api/service-collaboration.md`
4. `deployment/local-development.md`
