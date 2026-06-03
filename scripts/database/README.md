# Database Scripts

该目录用于存放数据库初始化、迁移和维护脚本。

建议文件命名：

```text
001_init_schema.sql
002_add_ai_secretary_tables.sql
003_add_file_storage_tables.sql
```

当前 Java 服务使用 AutoTable，后续如果需要生产环境可控迁移，可以逐步引入 Flyway 或 Liquibase。
