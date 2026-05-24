---
name: std-list-area-query
description: 列表页查询区 StdListAreaQuery。当需要列表查询表单、查询条件区、持久化提示、教程说明、hasTutorial 时使用本 Skill，避免查询区样式与指示器接错。
---

# StdListAreaQuery 接入指南

## 1. 选型

| 业务诉求 | 配置 | 关键区别 |
| --- | --- | --- |
| 普通查询表单 | 仅默认插槽 | 底部虚线分隔，无额外交互 |
| 查询条件会持久化到本地 | `show-persist-tip` | 左上角显示持久化提示标识 |
| 页面需附带使用说明 | `has-tutorial` + `#tutorial` 插槽 | 左上角问号，点击展开/收起教程 |
| 查询项超过 3～4 个 | 内嵌 `StdQueryCollapse` | 折叠多余表单项，见 §4 多条件模板 |

统一放在 `StdListContainer` 或 `StdListLayout` 的 `#query` 插槽内，无替代组件。

---

## 2. 快速接入

1. `import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue"`
2. 置于 `StdListContainer` 内第一个子区域（操作区、表格区之前）
3. 默认插槽放 `el-form`，`:model` 绑定 `listForm`
4. 查询/重置按钮放表单右侧（`class="flex justify-between"` + 右侧 `el-form-item`）
5. 多条件折叠场景在表单内包 `StdQueryCollapse`

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `showPersistTip` | `boolean` | 否 | `false` | 显示查询条件持久化提示（`StdIndicatorQueryPersist`） |
| `hasTutorial` | `boolean` | 否 | `false` | 显示教程入口图标，可展开 `#tutorial` 内容 |

### 插槽

| 插槽 | 必填 | 说明 |
| --- | --- | --- |
| `default` | 是 | 查询表单主体（通常为 `el-form`） |
| `tutorial` | 否 | 教程内容，仅 `hasTutorial` 为 true 且用户展开时显示 |

---

## 4. 模板（Templates）

### 基础查询区

```vue
<StdListAreaQuery>
  <el-form :model="listForm" inline class="flex justify-between">
    <div>
      <el-form-item label="名称">
        <el-input v-model="listForm.name" placeholder="请输入名称" clearable />
      </el-form-item>
    </div>
    <el-form-item>
      <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
      <el-button :disabled="listLoading" @click="resetList">重置</el-button>
    </el-form-item>
  </el-form>
</StdListAreaQuery>
```

### 持久化提示 + 教程

```vue
<StdListAreaQuery show-persist-tip has-tutorial>
  <template #tutorial>
    <el-alert type="info" :closable="false" title="查询说明：支持按名称、状态筛选。" />
  </template>
  <el-form :model="listForm" inline>
    <!-- 表单项 -->
  </el-form>
</StdListAreaQuery>
```

### 多条件 + StdQueryCollapse

```vue
<StdListAreaQuery>
  <el-form :model="listForm" inline class="flex justify-between">
    <StdQueryCollapse>
      <div>
        <el-form-item label="企业">
          <el-input v-model="listForm.orgName" clearable />
        </el-form-item>
        <!-- 更多表单项 -->
      </div>
      <template #actions>
        <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
        <el-button :disabled="listLoading" @click="resetList">重置</el-button>
      </template>
    </StdQueryCollapse>
  </el-form>
</StdListAreaQuery>
```

### 经 StdListLayout 使用

```vue
<StdListLayout show-persist-tip has-tutorial>
  <template #query>
    <el-form :model="listForm" inline>...</el-form>
  </template>
  <template #tutorial>
    <el-alert type="info" :closable="false" title="使用说明" />
  </template>
</StdListLayout>
```

---

## 5. 陷阱（Traps）

❌ 有教程内容但未开 hasTutorial
```vue
<StdListAreaQuery>
  <template #tutorial><el-alert title="说明" /></template>
</StdListAreaQuery>
```

✅ 必须同时设置 has-tutorial
```vue
<StdListAreaQuery has-tutorial>
  <template #tutorial><el-alert title="说明" /></template>
</StdListAreaQuery>
```

---

❌ 查询按钮与表单项混在同一 flex 行且无折叠
```vue
<el-form inline>
  <el-form-item v-for="i in 10">...</el-form-item>
  <el-form-item><el-button>查询</el-button></el-form-item>
</el-form>
```

✅ 多条件用 StdQueryCollapse，按钮放 #actions
```vue
<StdQueryCollapse>
  <div><!-- 表单项 --></div>
  <template #actions><el-button @click="loadList">查询</el-button></template>
</StdQueryCollapse>
```

---

❌ listForm 未绑定 :model
```vue
<el-form inline><el-input v-model="listForm.name" /></el-form>
```

✅ 必须 :model="listForm"
```vue
<el-form :model="listForm" inline>...</el-form>
```

---

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/std-series/StdListAreaQuery.vue` — 查询区容器
- `src/main/resources/web-ui/src/soa/std-series/StdIndicatorQueryPersist.vue` — 持久化提示标识
- `src/main/resources/web-ui/src/soa/std-series/StdIndicatorTutorial.vue` — 教程切换标识
- `src/main/resources/web-ui/src/soa/std-series/StdQueryCollapse.vue` — 查询折叠
- `src/main/resources/web-ui/src/views/playground/PgStdListAreaQuery.vue` — Playground 演示
