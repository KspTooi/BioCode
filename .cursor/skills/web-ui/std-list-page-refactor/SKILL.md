---
name: std-list-page-refactor
description: 将典型旧写法列表页翻新为标准 Std 列表布局（业务逻辑不变）。当需要翻新列表页、迁移旧分页、去除 template pagination、QfCc 旧写法、统一 StdListAreaTable 内置分页时使用本 Skill。
---

# 列表页翻新指南（std-list-page-refactor）

## 1. 选型

| 判断 | 处理 |
| --- | --- |
| 符合「可翻新」条件（见 §3） | 执行本 Skill 翻新流程 |
| 不符合「可翻新」条件 | 停止翻新，向用户说明原因 |
| 分页为「标准分页」 | 删除 `#pagination`，改用 `StdListAreaTable` 内置分页 |
| 分页含特殊逻辑 | 保留 `#pagination`，仅整理其它区域 |

**翻新目标结构**（与 `TicketPlan.vue`、`ArchInfo.vue`、`DbsecHazard.vue` 一致）：

```
StdListContainer
  ├─ StdListAreaQuery      ← 查询表单（可含 StdQueryCollapse）
  ├─ StdListAreaAction     ← 有按钮才保留
  ├─ StdListAreaTable      ← v-model:list-form + :list-total + :load-list，无 #pagination
  └─ el-dialog / 其它弹窗   ← 与 Container 同级，在 Table 之后
```

**参考对照**：旧写法样本 `QfCc.vue`；新写法样本 `TicketPlan.vue`、`ArchInfo.vue`。

---

## 2. 快速接入

1. 用 §3 清单判断目标 `.vue` 是否可翻新；不可翻新则退出
2. 确认 `script` 中已有 `listForm`、`listData`、`listTotal`、`listLoading`、`loadList`（通常来自 `XxxService.useXxxList()`），**不改 Service**
3. 模板保留 `StdListAreaQuery` / `StdListAreaAction` / `el-table` 列与事件绑定，仅调整包裹与分页
4. 删除 `StdListAreaTable` 或 `StdListLayout` 下的 `<template #pagination>` 及其中标准 `el-pagination`
5. 为 `StdListAreaTable` 增加 `v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList"`
6. 空 `StdListAreaAction` 删除；`StdListLayout` + 标准分页的页改为 `StdListContainer` 手动拼装（§4.2）
7. 自检：`el-table` 仍有 `height="100%"`、`v-loading="listLoading"`，弹窗仍在 `StdListContainer` 内

---

## 3. 参数契约

### 3.1 可翻新（须同时满足）

| 条件 | 说明 |
| --- | --- |
| 典型三区布局 | 存在查询区 +（可选）操作区 + 表格区 |
| 已用 Std 列表组件 | 根为 `StdListContainer`，或 `StdListLayout` 且可改为 Container 拼装 |
| 服务端分页 | `listForm` 含 `pageNum`、`pageSize`，翻页调用 `loadList` |
| 弹窗在列表外 | `el-dialog` 不嵌在 `el-table` 内 |

### 3.2 标准分页（可删除 #pagination）

同时满足即视为标准分页，必须改为内置分页：

| 特征 | 标准值 |
| --- | --- |
| `layout` | `"total, sizes, prev, pager, next, jumper"` |
| `:page-sizes` | `[10, 20, 50, 100]` |
| 绑定 | `v-model:current-page="listForm.pageNum"`、`v-model:page-size="listForm.pageSize"` |
| `total` | `:total="listTotal"` |
| 翻页回调 | `@size-change` / `@current-change` 仅改 `listForm` 并 `loadList()` |

### 3.3 禁止翻新

| 场景 | 原因 |
| --- | --- |
| 无 `StdListContainer` / `StdListLayout` | 非典型列表页 |
| 多 Tab / 多表切换 | 布局非单列表 |
| 树表一体且右侧非标准三区 | 如整页自定义 dashboard |
| 客户端假分页、无 `loadList` | 不适用内置分页 |
| `#pagination` 含非标准 layout、自定义 page-sizes、额外交互文案 | 属特殊分页 |
| 翻页除 `loadList` 外还有其它副作用 | 须保留自定义分页 |
| 嵌入组件内分页（如 `ModalUserSelector`） | 非业务列表页 |

---

## 4. 模板（Templates）

### 4.1 QfCc 旧写法 → 新写法（核心变更）

**翻新前（旧，`QfCc.vue`）**

```vue
<StdListAreaTable>
  <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
    <!-- 列 -->
  </el-table>
  <template #pagination>
    <el-pagination
      v-model:current-page="listForm.pageNum"
      v-model:page-size="listForm.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="listTotal"
      background
      @size-change="(val: number) => { listForm.pageSize = val; loadList(); }"
      @current-change="(val: number) => { listForm.pageNum = val; loadList(); }"
    />
  </template>
</StdListAreaTable>
```

**翻新后（新，`TicketPlan.vue` / `ArchInfo.vue`）**

```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
  <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
    <!-- 列：原样保留 -->
  </el-table>
</StdListAreaTable>
```

`script` 与 `XxxService` **零改动**（`listForm` 已为 reactive 时可写 `v-model:list-form`，与 `:list-form` 等价）。

---

### 4.2 StdListLayout + #pagination → StdListContainer 拼装

**翻新前（旧，`AuditLoginRcd.vue`）**

```vue
<StdListLayout>
  <template #query>...</template>
  <template #actions>...</template>
  <template #table>
    <el-table height="100%" ... />
  </template>
  <template #pagination>
    <el-pagination ... @current-change="..." />
  </template>
</StdListLayout>
<el-dialog>...</el-dialog>
```

**翻新后（新）**

```vue
<StdListContainer>
  <StdListAreaQuery>
    <!-- 原 #query 内容 -->
  </StdListAreaQuery>

  <StdListAreaAction>
    <!-- 原 #actions 内容；无按钮则删除整块 -->
  </StdListAreaAction>

  <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
    <!-- 原 #table 内 el-table，原样保留 -->
  </StdListAreaTable>

  <!-- 原 StdListLayout 外的 el-dialog 移入 Container -->
  <el-dialog>...</el-dialog>
</StdListContainer>
```

```ts
// import 替换
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
// 删除 StdListLayout import
```

`show-persist-tip` / `has-tutorial`：若原 `StdListLayout` 有，改写到 `<StdListAreaQuery show-persist-tip has-tutorial>`，`#tutorial` 内容放入 `StdListAreaQuery` 的 `#tutorial` 插槽。

---

### 4.3 Splitpanes 内列表（`ArchInfo.vue` / `DbsecHazard.vue`）

仅翻新 **Pane 内** 的 `StdListContainer` 子树；左侧树、外层 `Splitpanes` 不动。表格区同样删除 `#pagination`，改为 §4.1 写法。

---

### 4.4 翻新检查清单（完成后逐项勾选）

```
- [ ] 已删除标准 template #pagination
- [ ] StdListAreaTable 已绑定 list-form / list-total / load-list
- [ ] el-table 仍有 height="100%" 与 v-loading
- [ ] 查询/操作/列/弹窗逻辑与事件未改
- [ ] 空 StdListAreaAction 已移除
- [ ] 未改动 *Service.ts / *Api.ts
```

---

## 5. 陷阱（Traps）

❌ 翻新时修改 loadList / resetList 实现
```ts
const loadList = async () => { /* 重写请求逻辑 */ };
```

✅ 只改模板分页接入，Service 保持原样
```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
```

---

❌ 删除分页后未传 loadList
```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal">
```

✅ 三者齐全
```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
```

---

❌ 标准分页仍保留 #pagination（重复分页）
```vue
<StdListAreaTable :load-list="loadList">
  <el-table />
  <template #pagination><el-pagination /></template>
</StdListAreaTable>
```

✅ 二选一，标准场景只用内置
```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
  <el-table height="100%" />
</StdListAreaTable>
```

---

❌ 非典型页强行翻新
```vue
<div class="custom-page">
  <el-table /><el-pagination />
</div>
```

✅ 先落地 StdListContainer 三区，再删自定义分页；否则不翻新

---

❌ 翻新 StdListLayout 却保留 #pagination 且不传 Table 三属性
```vue
<StdListLayout>
  <template #table>...</template>
  <template #pagination>...</template>
</StdListLayout>
```

✅ 改为 StdListContainer 拼装 + 内置分页（§4.2）

---

## 6. 源码索引

| 文件 | 职责 |
| --- | --- |
| `src/main/resources/web-ui/src/views/qf/QfCc.vue` | 旧写法参考（手写 `#pagination`） |
| `src/main/resources/web-ui/src/views/ticket/TicketPlan.vue` | 新写法参考（内置分页） |
| `src/main/resources/web-ui/src/views/arch/ArchInfo.vue` | 新写法参考（Splitpanes + 内置分页） |
| `src/main/resources/web-ui/src/views/dbsec/DbsecHazard.vue` | 新写法参考（`:list-form` 无 `#pagination`） |
| `src/main/resources/web-ui/src/soa/std-series/StdListAreaTable.vue` | 内置分页实现 |
| `.cursor/skills/web-ui/std-list-container/SKILL.md` | Container 接入 |
| `.cursor/skills/web-ui/std-list-area-query/SKILL.md` | Query 接入 |
| `.cursor/skills/web-ui/std-list-area-action/SKILL.md` | Action 接入 |
| `.cursor/skills/web-ui/std-list-area-table/SKILL.md` | Table 接入 |
