---
name: mysql-mcp-operations
description: 通过 MCP 的 mysql_query 工具执行 MySQL 查询与更新，处理 Snowflake 大整数精度丢失、批量更新策略、结果验证。当需要查询/修改数据库、执行 UPDATE/DELETE/SELECT/INSERT、操作 MySQL 时使用本 Skill。
---

## 1. 选型

| 业务场景 | 工具 | 关键约束 |
| --- | --- | --- |
| SELECT / UPDATE / DELETE / INSERT / DDL | `CallMcpTool(server="user-mysql", toolName="mysql_query")` | `arguments.sql` 为必填字符串 |
| 查看表结构元数据 | `FetchMcpResource(server="user-mysql", uri="mysql://tables/{table_name}")` | 仅返回描述，不返回 DDL |
| 浏览可用表列表 | `FetchMcpResource(server="user-mysql", uri="mysql://tables")` | — |

## 2. 快速接入

- 调用 `CallMcpTool`，`server` 固定 `"user-mysql"`，`toolName` 固定 `"mysql_query"`
- `arguments` 中 `sql` 字段放完整 SQL 语句
- 查询前用 `codegraph_explore` 查实体类获取表名与字段语义
- 更新后必须 `SELECT` 验证，不可仅依赖 `Affected rows` 返回值

## 3. 参数契约

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `server` | `"user-mysql"` | 是 | 固定值 |
| `toolName` | `"mysql_query"` | 是 | 固定值 |
| `arguments.sql` | `string` | 是 | 完整 SQL 语句，多语句需分次调用 |

## 4. 模板（Templates）

### 查询（SELECT）

```sql
SELECT id, name, policy_crud_json FROM assembly_poly_model WHERE CONCAT(output_schema_id) = '1382322392264740864' ORDER BY seq
```

### 批量更新同一条件（WHERE name IN）

```sql
UPDATE assembly_poly_model SET policy_crud_json = '["LIST_VIEW","DETAILS"]' WHERE name IN('id','root_id') AND CONCAT(output_schema_id) = '1382322392264740864'
```

### 验证更新结果

```sql
SELECT name, policy_crud_json FROM assembly_poly_model WHERE CONCAT(output_schema_id) = '1382322392264740864' ORDER BY seq
```

### 查找表中所有 distinct 值（绕过精度丢失）

```sql
SELECT DISTINCT CONCAT(output_schema_id) as sid FROM assembly_poly_model ORDER BY sid
```

## 5. 陷阱（Traps）

### ❌ 直接用 Snowflake Long 值做 WHERE 条件

```sql
-- ❌ JavaScript 解析时 Snowflake ID 精度丢失，WHERE 不匹配
SELECT * FROM t WHERE id = 1382322392264740864
```

```sql
-- ✅ 转字符串比较
SELECT * FROM t WHERE CONCAT(id) = '1382322392264740864'
```

### ❌ 每次一行 UPDATE（N 条记录 = N 次调用）

```sql
-- ❌ 逐条更新
UPDATE t SET x='a' WHERE name='id' AND CONCAT(sid)='xx'
UPDATE t SET x='a' WHERE name='name' AND CONCAT(sid)='xx'
```

```sql
-- ✅ 批量合并
UPDATE t SET x='a' WHERE name IN('id','name') AND CONCAT(sid)='xx'
```

### ❌ 更新后不验证

```sql
-- ❌ 仅依赖返回值 Affected rows / Changed rows
UPDATE t SET x='a' WHERE CONCAT(sid)='xx'
```

```sql
-- ✅ 立即 SELECT 验证所有行
SELECT name, x FROM t WHERE CONCAT(sid)='xx' ORDER BY seq
```

### ❌ WHERE 条件有歧义时不加表名限制

```sql
-- ❌ 多表可能都有 name 列，且大雪糕 ID 精度丢失仍匹配到了相近值
UPDATE t SET x='a' WHERE name='id'
```

```sql
-- ✅ WHERE 同时限制 name 和雪花 ID
UPDATE t SET x='a' WHERE name='id' AND CONCAT(sid)='1382322392264740864'
```

## 6. 源码索引

- `mcps/user-mysql/tools/mysql_query.json` — MCP 工具 schema，参数 `sql`
- `mcps/user-mysql/resources/assembly_poly_model.json` — 表元数据资源描述
- `src/main/java/com/ksptool/bio/biz/assembly/model/polymodel/PolyModelPo.java:45` — `policy_crud_json` 字段定义（`Set<String>`，JSON 存储，合法值 `ADD/EDIT/DETAILS/LIST_QUERY/LIST_VIEW`）
