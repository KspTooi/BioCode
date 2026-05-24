---
name: std-list-container
description: 列表页最外层容器 StdListContainer。当需要新建列表页、手写列表布局、列表页根容器、nopadding 无内边距时使用本 Skill，避免漏包容器导致高度/内边距异常。
---

# StdListContainer 接入指南

## 1. 选型

| 业务诉求 | 使用方案 | 关键区别 |
| --- | --- | --- |
| 标准 CRUD 列表页（查询+操作+表格+分页） | `StdListLayout`（推荐） | 插槽分发，少写 4 层 import |
| 需自定义子区域结构、嵌套 Splitpanes 等 | `StdListContainer` + 子区域组件手动拼装 | 完全自控 DOM 顺序 |
| 列表页需贴边、外层已有 padding | `StdListContainer` + `nopadding` | 移除默认 20px 内边距 |

**强制规则**：列表页最外层必须是 `StdListContainer`（直接或经 `StdListLayout` 包裹），禁止裸 `div` 作根节点。

---

## 2. 快速接入

1. `import StdListContainer from "@/soa/std-series/StdListContainer.vue"`
2. 模板根节点用 `<StdListContainer>` 包裹整页
3. 按顺序放入：`StdListAreaQuery` → `StdListAreaAction` → `StdListAreaTable`
4. `el-dialog` 等弹窗放在 `StdListContainer` 内、与上述区域同级（表格区之后）
5. 外层路由/父级须有确定高度（如 `height: 100%`），否则内部 `flex: 1` 表格区无法撑满

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `nopadding` | `boolean` | 否 | `false` | 为 `true` 时移除默认 20px 内边距 |

### 插槽

| 插槽 | 必填 | 说明 |
| --- | --- | --- |
| `default` | 是 | 列表页全部可见内容（查询区、操作区、表格区、弹窗等） |

---

## 4. 模板（Templates）

### 标准列表页（手动拼装）

```vue
<template>
  <StdListContainer>
    <StdListAreaQuery>
      <!-- 查询表单 -->
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button type="primary" @click="openModal('add', null)">新增</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <!-- 列定义 -->
      </el-table>
    </StdListAreaTable>

    <el-dialog v-model="modalVisible" title="编辑" width="600px">
      <!-- 表单 -->
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import XxxService from "@/views/xxx/service/XxxService.ts";

const { listForm, listData, listTotal, listLoading, loadList } = XxxService.useXxxList();
</script>
```

### 嵌套 Splitpanes（右侧列表）

```vue
<Splitpanes>
  <Pane size="15"><!-- 左侧树 --></Pane>
  <Pane min-size="75">
    <StdListContainer>
      <StdListAreaQuery>...</StdListAreaQuery>
      <StdListAreaAction>...</StdListAreaAction>
      <StdListAreaTable>...</StdListAreaTable>
    </StdListContainer>
  </Pane>
</Splitpanes>
```

### nopadding（外层已有间距）

```vue
<StdListContainer nopadding>
  <StdListAreaQuery>...</StdListAreaQuery>
</StdListContainer>
```

---

## 5. 陷阱（Traps）

❌ 根节点用普通 div
```vue
<div class="page">
  <StdListAreaQuery>...</StdListAreaQuery>
</div>
```

✅ 必须用 StdListContainer
```vue
<StdListContainer>
  <StdListAreaQuery>...</StdListAreaQuery>
</StdListContainer>
```

---

❌ 弹窗放在 Container 外
```vue
</StdListContainer>
<el-dialog v-model="modalVisible" />
```

✅ 弹窗与查询/表格同级，放在 Container 内
```vue
  </StdListAreaTable>
  <el-dialog v-model="modalVisible" />
</StdListContainer>
```

---

❌ 父级无高度，表格无法撑满
```vue
<div><!-- 无 height --><StdListContainer>...</StdListContainer></div>
```

✅ 父级链路上有 `height: 100%` 或 flex 占满
```vue
<Pane min-size="75"><StdListContainer>...</StdListContainer></Pane>
```

---

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/std-series/StdListContainer.vue` — 列表页根容器
- `src/main/resources/web-ui/src/soa/std-series/StdListLayout.vue` — 基于 Container 的插槽化封装（推荐优先）
- `src/main/resources/web-ui/src/views/playground/PgStdListContainer.vue` — Playground 演示
