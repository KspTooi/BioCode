---
name: codegraph
description: 代码知识图谱索引工具，基于 SQLite AST 解析提供亚毫秒级符号查询、调用链追踪、影响分析。当需要"理解代码/查调用链/找符号/重构影响分析/代码导航/查看架构"时优先使用本工具，替代手动 grep/Read 组合。
---

# Codegraph 代码知识图谱

## 工具选型

| 意图 | 工具 | 说明 |
| --- | --- | --- |
| 理解某个功能/模块/架构 | `codegraph_explore` | **首选**，自然语言或符号名查询，一次返回相关源码，通常无需再查 |
| 查某个符号的文件位置 | `codegraph_search` | 只返回位置信息，不含源码 |
| 获取单个符号完整源码 | `codegraph_node` | 重载名称时一次性返回所有匹配，用 `includeCode: true` |
| 谁调用了某符号 | `codegraph_callers` | 调用者列表 |
| 某符号调用了谁 | `codegraph_callees` | 被调用者列表 |
| 重构前分析影响范围 | `codegraph_impact` | 修改某符号会波及哪些地方 |
| 查看项目文件结构 | `codegraph_files` | 比 Glob 更快，含语言和符号数 |
| 索引健康检查 | `codegraph_status` | 调试用 |

---

## 核心原则

### 1. 首选 explore，不是 grep

大多数问题一个 `codegraph_explore` 就够了——它会返回相关符号的完整源码并按文件分组，同时展示调用路径（含动态分发：回调、React 重渲染、JSX children 等），这些是 grep 无法追踪的。

```
// ✅ 正确：直接用 explore
codegraph_explore({ query: "UserLogin login 验证码校验" })

// ❌ 错误：先 grep 再 Read 兜一圈
grep "UserLogin" → Read UserLogin.vue → 发现引用了 service → grep service 方法...
```

### 2. 信任 codegraph 结果，不要用 grep 二次验证

codegraph 的结果来自完整 AST 解析，比 grep 更准确且更全面。二次验证浪费上下文。

### 3. explore 返回的源码不用再 Read

`codegraph_explore` 返回的源码是 Read-equivalent 的，不要再打开已展示的文件重复阅读。

### 4. 编辑后注意过期提示

如果响应开头有 "⚠️ Some files referenced below were edited since the last index sync…"，说明文件待重新索引，需要用 Read 直接读取那些文件。未出现在提示中的文件仍然可信。

---

## 典型使用链

### 理解代码（最常见）

一个 `codegraph_explore` 解决：

```json
{ "query": "BasicPat 修改密码 校验流程" }
```

### 追踪调用路径

`codegraph_explore` 传入相关符号名即可追踪从 X 到 Y 的完整路径：

```json
{ "query": "AuthRouteRegister AdminMain router" }
```

### 重构准备

```
codegraph_search → codegraph_callers → codegraph_impact
```

先找到符号位置，再查调用者，最后评估影响范围。

### 过载名称定位

当同名方法存在于多个类时，用 `codegraph_node` 一次性获取所有定义的完整源码：

```json
{ "symbol": "save", "includeCode": true }
```

用 `file`/`line` 参数精确到特定定义。

---

## 陷阱

- **不要 explore 之后再 Read 同一文件** — explore 返回的源码已经足够，重复阅读浪费 token
- **不要 grep 之后再用 codegraph** — 应该反过来，先 codegraph
- **不要对简单符号查 node** — 只有一个匹配时，explore 已经包含了完整源码
- **不要在 explore 之后逐文件 Read** — explore 已经按文件分好组了
- **explore 用自然语言或符号名，不是文件路径** — 查文件布局用 `codegraph_files`

---

## 索引状态

当前项目已完整索引，无需初始化。索引滞后文件写入约 1 秒，日常使用无需关心。
