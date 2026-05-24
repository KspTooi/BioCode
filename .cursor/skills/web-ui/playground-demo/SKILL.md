---
name: playground-demo
description: 在 playground 模块为组件/功能创建交互演示页。当需要"组件演示/Playground/演示页/创建演示/Pg页面"时使用本 Skill，按现有 playground 风格编写 Vue 页并注册路由。
---

# Playground 演示页创建指南

## 1. 选型

| 业务场景 | 布局模式 | 关键结构 |
| --- | --- | --- |
| 选择器/表单类组件（InputXxx、ModalXxx） | 表单卡片模式 | `el-card` + `el-form` + 已选ID/VO 回显 |
| 树/上传等复杂交互组件 | 双栏面板模式 | `demo-body` = 左侧操作面板 + 右侧 `demo-info-panel` |
| 所有演示页 | 统一外壳 | `StdPgLayout` 三 Tab：演示 / props / emits |

统一使用 `StdPgLayout`，无替代方案。

## 2. 快速接入

1. 在 `src/main/resources/web-ui/src/views/playground/` 新建 `PgXxx.vue`（`Pg` + 组件名 PascalCase）
2. 用 `StdPgLayout` 包裹，默认插槽写演示区，`#props` / `#emits` 写文档表格
3. 在 `src/main/resources/web-ui/src/views/playground/route/PlayGroundRouteRegister.ts` 追加 `RouteEntryPo.build` 条目
4. `path` 用 `pg-kebab-case`，`name` 用 `{中文名}演示`，`biz` 固定 `"playground"`
5. 无需改 `AdminMain.ts`（已注册 `PlayGroundRouteRegister`）

## 3. 参数契约

### 文件命名

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| Vue 文件名 | `Pg{Component}.vue` | 是 | — | 如 `PgStdUpload.vue`、`PgInputOrgTree.vue` |
| 路由 path | `pg-{kebab-case}` | 是 | — | 如 `pg-std-upload`、`pg-input-org-tree` |
| 路由 name | `string` | 是 | — | 菜单显示名，格式 `{组件中文名}演示` |
| biz | `string` | 是 | `"playground"` | 固定值，不可改 |

### StdPgLayout 插槽

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| 默认插槽 | — | 是 | — | 「演示」Tab 内容 |
| `#props` | — | 是 | — | Props 文档表格 |
| `#emits` | — | 是 | — | 事件 / v-model / 插槽 / expose 文档表格 |

### 演示区数据表（script 中定义）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `propsTableData` | `Array<{name,type,required,default,desc}>` | 是 | — | Props Tab 数据源 |
| `emitsTableData` | `Array<{name,payload,desc}>` | 是 | — | Emits Tab 主表数据源 |
| `vModelTableData` | 同上结构 | 否 | — | 有 v-model 绑定时追加 |
| `slotsTableData` | `Array<{name,params,desc}>` | 否 | — | 有插槽时追加 |
| `exposeTableData` | `Array<{name,params,desc}>` | 否 | — | 有 defineExpose 方法时追加 |

## 4. 模板（Templates）

### 路由注册

在 `PlayGroundRouteRegister.ts` 的 `doRegister()` 返回数组末尾追加：

```ts
RouteEntryPo.build({
  biz: "playground",
  path: "pg-xxx-component",
  name: "Xxx组件演示",
  component: () => import("@/views/playground/PgXxxComponent.vue"),
  meta: {},
}),
```

### 最小骨架（表单卡片模式）

适用：选择器、简单表单组件。每个能力场景一个 `el-card`。

```vue
<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择Xxx">
            <XxxComponent v-model="ids" @on-submit-entity="(vos) => (entities = vos)" />
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in ids" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="ids.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
          <el-form-item label="VO数据">
            <el-tag v-for="vo in entities" :key="vo.id" type="success" style="margin-right: 4px">{{ vo.name }}</el-tag>
            <span v-if="entities.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="150" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>

    <template #emits>
      <el-table :data="emitsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="事件名" width="220" />
        <el-table-column prop="payload" label="参数" width="250" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import XxxComponent from "@/views/xxx/public/XxxComponent.vue";

const ids = ref<string[]>([]);
const entities = ref<XxxVo[]>([]);

const propsTableData = [
  { name: "placeholder", type: "string", required: "否", default: '"请选择"', desc: "占位符" },
];

const emitsTableData = [
  { name: "on-submit-entity", payload: "data: XxxVo[]", desc: "提交时返回实体列表" },
];
</script>
```

### 双栏面板模式骨架

适用：树、上传等需控件面板 + 状态面板的组件。双栏区须带 scoped style，类名：`.demo-body`、`.demo-upload-panel`（或 `.demo-tree-panel`）、`.demo-info-panel`、`.panel-header`、`.panel-controls`、`.info-block`。

```vue
<el-card header="基础能力演示" style="margin-bottom: 20px">
  <div class="demo-body">
    <div class="demo-upload-panel">
      <div class="panel-header">
        <span class="panel-title">组件操作区</span>
      </div>
      <div class="panel-controls">
        <el-checkbox v-model="xxxEnabled" size="small">开关项</el-checkbox>
      </div>
      <div class="upload-area">
        <XxxComponent v-model="data" />
      </div>
    </div>
    <div class="demo-info-panel">
      <div class="panel-header">
        <span class="panel-title">状态信息</span>
        <el-button size="small" text type="primary" @click="data = []">清空</el-button>
      </div>
      <div class="info-block">
        <div class="info-label">当前值</div>
        <div class="info-value">{{ data?.length ?? 0 }}</div>
      </div>
    </div>
  </div>
</el-card>
```

### emits Tab 扩展（v-model / 插槽 / expose）

```vue
<template #emits>
  <el-table :data="emitsTableData" stripe border style="width: 100%">
    <el-table-column prop="name" label="事件名" width="220" />
    <el-table-column prop="payload" label="参数" width="250" />
    <el-table-column prop="desc" label="说明" min-width="200" />
  </el-table>

  <el-divider content-position="left">v-model 双向绑定</el-divider>
  <el-table :data="vModelTableData" stripe border style="width: 100%">
    <el-table-column prop="name" label="绑定名" width="240" />
    <el-table-column prop="type" label="类型" width="200" />
    <el-table-column prop="desc" label="说明" min-width="200" />
  </el-table>

  <el-divider content-position="left">插槽</el-divider>
  <el-table :data="slotsTableData" stripe border style="width: 100%">
    <el-table-column prop="name" label="插槽名" width="160" />
    <el-table-column prop="params" label="参数" width="280" />
    <el-table-column prop="desc" label="说明" min-width="200" />
  </el-table>
</template>
```

### 多场景组织约定

- 每个独立能力/场景独占一个 `el-card`，`header` 写清场景名（如「裁剪演示」「#button 插槽演示」）
- 卡片间用 `style="margin-bottom: 20px"` 或 `margin-top: 20px` 分隔
- 演示区顶部控件：`el-checkbox` / `el-input-number` / `el-switch` 控制组件 props，变更时 `watch` 清空已选数据
- 裁剪/联动场景：左侧 `OrgTree`（宽 260px）选根节点，右侧放目标组件

## 5. 陷阱（Traps）

❌ 不用 StdPgLayout，自己写 el-tabs
```vue
<el-tabs><el-tab-pane label="演示">...</el-tab-pane></el-tabs>
```

✅ 必须用 StdPgLayout
```vue
<StdPgLayout><div style="padding: 24px">...</div><template #props>...</template></StdPgLayout>
```

---

❌ 路由 path 不用 pg- 前缀
```ts
path: "std-upload-demo",
```

✅ 统一 pg- 前缀
```ts
path: "pg-std-upload",
```

---

❌ 新建独立 RouteRegister 类
```ts
export default class MyDemoRouteRegister extends GenricRouteRegister { ... }
```

✅ 只往 PlayGroundRouteRegister 追加条目
```ts
// PlayGroundRouteRegister.ts 的 doRegister() 数组内追加
```

---

❌ props/emits 文档写死在模板里
```vue
<el-table-column prop="name" /><!-- 硬编码每行 -->
```

✅ 用 script 数组驱动
```ts
const propsTableData = [{ name: "xxx", type: "string", required: "否", default: "...", desc: "..." }];
```

---

❌ 数组 v-model 初始化为 undefined
```ts
const ids = ref<string[]>();
```

✅ 必须初始化为空数组
```ts
const ids = ref<string[]>([]);
```
