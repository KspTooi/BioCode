---
name: std-list-area-action
description: 列表页操作按钮区 StdListAreaAction。当需要列表页新增/删除/导出按钮区、操作区、批量操作栏时使用本 Skill，避免操作区间距与布局不一致。
---

# StdListAreaAction 接入指南

## 1. 选型

| 业务诉求 | 用法 | 关键区别 |
| --- | --- | --- |
| 列表页顶部操作按钮（新增、导出、批量删除） | `StdListAreaAction` 默认插槽 | 统一下边距 15px，与查询区、表格区分隔 |
| 按钮需横向排列并带间距 | 在组件上写 `class="flex gap-2"` | 组件本身不设 flex，由使用者控制 |
| 暂无操作按钮 | 可省略整个 `StdListAreaAction` | 不必留空标签占位 |

统一置于查询区之后、表格区之前；经 `StdListLayout` 时对应 `#actions` 插槽。

---

## 2. 快速接入

1. `import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue"`
2. 放在 `StdListAreaQuery` 之后、`StdListAreaTable` 之前
3. 默认插槽内放 `el-button`，绑定 `:disabled="listLoading"` 或选中态
4. 多按钮横向排列：`<StdListAreaAction class="flex gap-2">`
5. 无操作按钮时不渲染本组件

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| — | — | — | — | 无 Props |

### 插槽

| 插槽 | 必填 | 说明 |
| --- | --- | --- |
| `default` | 是 | 操作按钮等内容，通常为 `el-button` |

---

## 4. 模板（Templates）

### 基础操作区

```vue
<StdListAreaAction>
  <el-button type="primary" @click="openModal('add', null)">新增</el-button>
  <el-button
    type="danger"
    :disabled="listSelected.length === 0"
    :loading="listLoading"
    @click="removeListBatch(listSelected)"
  >
    批量删除
  </el-button>
</StdListAreaAction>
```

### flex 横向间距

```vue
<StdListAreaAction class="flex gap-2">
  <el-button type="primary" @click="openModal('add', null)">创建</el-button>
  <el-button type="danger" :disabled="selections.length === 0" @click="removeList(selections)">
    批量删除
  </el-button>
</StdListAreaAction>
```

### 经 StdListLayout

```vue
<StdListLayout>
  <template #actions>
    <el-button type="primary" @click="onAdd">新增</el-button>
    <el-button @click="onExport">导出</el-button>
  </template>
</StdListLayout>
```

### 分组按钮（竖线分隔）

```vue
<StdListAreaAction>
  <el-button type="primary">新增</el-button>
  <el-button>编辑</el-button>
  <el-divider direction="vertical" />
  <el-button type="danger">删除</el-button>
</StdListAreaAction>
```

---

## 5. 陷阱（Traps）

❌ 在表格外层再包一层带 margin 的 div
```vue
<div style="margin-bottom: 15px">
  <el-button type="primary">新增</el-button>
</div>
```

✅ 使用 StdListAreaAction
```vue
<StdListAreaAction>
  <el-button type="primary">新增</el-button>
</StdListAreaAction>
```

---

❌ 把查询/重置按钮放在操作区
```vue
<StdListAreaAction>
  <el-button type="primary" @click="loadList">查询</el-button>
</StdListAreaAction>
```

✅ 查询/重置属于 StdListAreaQuery
```vue
<StdListAreaQuery>
  <el-form>
    <el-form-item><el-button @click="loadList">查询</el-button></el-form-item>
  </el-form>
</StdListAreaQuery>
```

---

❌ 空操作区仍保留组件
```vue
<StdListAreaAction><!-- 无按钮 --></StdListAreaAction>
```

✅ 无按钮则删除该节点
```vue
<!-- 直接进入 StdListAreaTable -->
```

---

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/std-series/StdListAreaAction.vue` — 操作按钮区容器
- `src/main/resources/web-ui/src/soa/std-series/StdListLayout.vue` — `#actions` 插槽封装
- `src/main/resources/web-ui/src/views/playground/PgStdListAreaAction.vue` — Playground 演示
