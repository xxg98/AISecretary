# MySQL Init Scripts

该目录用于存放 MySQL 初始化 SQL 脚本。

Docker Compose 首次初始化 MySQL 数据卷时，会自动执行该目录下的 `.sql` 文件。

建议命名：

```text
001_init_schema.sql
002_init_data.sql
```

注意：不要在这里提交包含真实敏感数据的 SQL。
