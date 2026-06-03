# MySQL

该目录用于存放 MySQL 初始化脚本。

## 使用方式

将初始化 SQL 放到：

```text
infrastructure/mysql/init/
```

本地 Docker Compose 启动 MySQL 时会自动挂载到：

```text
/docker-entrypoint-initdb.d
```

注意：MySQL 官方镜像只会在首次初始化数据目录时执行这些 SQL。
