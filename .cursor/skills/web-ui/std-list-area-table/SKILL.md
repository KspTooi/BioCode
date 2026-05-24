---
name: std-list-area-table
description: 列表页表格区 StdListAreaTable。当需要列表表格、分页、listForm 双向绑定、loadList 翻页、自定义分页插槽时使用本 Skill，避免表格高度与分页接错。
---

# StdListAreaTable 接入指南

## 1. 选型

| 业务诉求 | 用法 | 关键区别 |
| --- | --- | --- |
| 使用内置分页（改页码自动 loadList） | `v-model:list-form` + `:list-total` + `:load-list`，不写 `#pagination` | 默认 `el-pagination` 已接好 |
| 自定义分页布局或事件 | `#pagination` 插槽 + 手写 `el-pagination` | 覆盖内置分页 |
| 仅展示表格、分页由外层处理 | 只写默认插槽，不传 `loadList` | 少见，优先用内置或插槽 |

`listForm` 类型为 `PageQuery`（`pageNum`、`pageSize`），通常来自 `XxxService.useXxxList()`。

---

## 2. 快速接入

1. `import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue"`
2. 置于 `StdListAreaAction` 之后，作为 Container 内最后一个可见区域
3. `el-table` 必须设 `height="100%"`，并加 `v-loading="listLoading"`
4. 内置分页：`v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList"`
5. 自定义分页：使用 `#pagination` 插槽，在 `@size-change` / `@current-change` 中调用 `loadList`

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `listForm`（v-model:list-form） | `PageQuery` | 否 | — | 分页参数 `{ pageNum, pageSize }` |
| `listTotal` | `number` | 否 | — | 总条数，传给分页 `total` |
| `loadList` | `() => any` | 否 | — | 内置分页 size/current 变化时回调 |

### 插槽

| 插槽 | 必填 | 说明 |
| --- | --- | --- |
| `default` | 是 | 表格主体（`el-table`） |
| `pagination` | 否 | 覆盖默认分页；未提供时使用内置 `el-pagination` |

---

## 4. 模板（Templates）

### 内置分页（推荐）

```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
  <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
    <el-table-column type="index" label="序号" width="60" align="center" />
    <el-table-column prop="name" label="名称" min-width="120" />
    <!-- 更多列 -->
  </el-table>
</StdListAreaTable>
```

```ts
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import type PageQuery from "@/commons/model/PageQuery";
import XxxService from "@/views/xxx/service/XxxService.ts";

const { listForm, listData, listTotal, listLoading, loadList } = XxxService.useXxxList();
// listForm 已含 pageNum、pageSize
```

### 自定义分页插槽

```vue
<StdListAreaTable :list-form="listForm" :list-total="listTotal" :load-list="loadList">
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

### 经 StdListLayout

```vue
<StdListLayout>
  <template #table>
    <el-table :data="listData" stripe border height="100%">...</el-table>
  </template>
  <template #pagination>
    <el-pagination v-model:current-page="listForm.pageNum" ... />
  </template>
</StdListLayout>
```

### 带多选

```vue
<StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
  <el-table
    v-loading="listLoading"
    :data="listData"
    stripe
    border
    height="100%"
    @selection-change="(val: XxxVo[]) => (listSelected = val)"
  >
    <el-table-column type="selection" width="40" />
  </el-table>
</StdListAreaTable>
```

---

## 5. 陷阱（Traps）

❌ el-table 未设 height="100%"
```vue
<el-table :data="listData" stripe border />
```

✅ 必须 height="100%" 才能撑满 flex 区域
```vue
<el-table :data="listData" stripe border height="100%" />
```

---

❌ 自定义分页但未在事件中 loadList
```vue
<el-pagination v-model:current-page="listForm.pageNum" :total="listTotal" />
```

✅ 翻页必须触发 loadList
```vue
@current-change="(val) => { listForm.pageNum = val; loadList(); }"
```

---

❌ 同时使用内置分页 props 又写空 #pagination
```vue
<StdListAreaTable :load-list="loadList">
  <el-table ... />
  <template #pagination></template>
</StdListAreaTable>
```

✅ 二选一：要么不传 #pagination 用内置，要么插槽内完整 pagination
```vue
<StdListAreaTable v-model:list-form="listForm" :load-list="loadList">
  <el-table height="100%" />
</StdListAreaTable>
```

---

❌ listForm.pageNum 未从 1 初始化
```ts
const listForm = reactive({ pageNum: 0, pageSize: 10 });
```

✅ pageNum 从 1 开始
```ts
const listForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });
```

---

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/std-series/StdListAreaTable.vue` — 表格+分页区
- `src/main/resources/web-ui/src/commons/model/PageQuery.ts` — 分页参数类型
- `src/main/resources/web-ui/src/soa/std-series/StdListLayout.vue` — `#table` / `#pagination` 封装
- `src/main/resources/web-ui/src/views/playground/PgStdListAreaTable.vue` — Playground 演示
