---
name: org-selector
description: 在 views 模块中接入组织机构选择器组件（InputOrgTree 多选、InputOrgTreeSingle 单选、ModalOrgTree 纯弹窗）。当需要"选组织/选机构/选部门/组织树/组织机构选择"时使用本 Skill，避免重新阅读源码并选错组件。
---

# 组织机构选择器接入指南

## 组件选型

| 业务诉求 | 使用组件 | v-model 类型 |
| --- | --- | --- |
| 只选一个组织（带输入框） | `InputOrgTreeSingle` | `string`（组织ID） |
| 选一个或多个组织（带输入框） | `InputOrgTree` | `string[]`（组织ID数组） |
| 自己控制触发，只需要弹窗 | `ModalOrgTree` | `boolean` + `checkedOrgIds: string[]` |

**强制规则**：单选场景必须用 `InputOrgTreeSingle`，禁止用 `InputOrgTree` 加 `mode="single"` 自行包装。

---

## 一、InputOrgTreeSingle（单选，最常用）

```ts
import InputOrgTreeSingle from "@/views/core/public/InputOrgTreeSingle.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";
```

**v-model**：`string`（单个组织ID，**required**）

**事件**：`on-submit-entity(vo: GetOrgTreeVo)` — 返回单个实体（已自动解包，非数组）

**Props**：透传 `InputOrgTree`，参见下一节。

```vue
<template>
  <el-form-item label="所属组织">
    <InputOrgTreeSingle
      v-model="form.orgId"
      placeholder="请选择所属组织"
      @on-submit-entity="(org) => (form.orgName = org?.name ?? '')"
    />
  </el-form-item>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import InputOrgTreeSingle from "@/views/core/public/InputOrgTreeSingle.vue";

const form = reactive({ orgId: "", orgName: "" });
</script>
```

---

## 二、InputOrgTree（多选）

```ts
import InputOrgTree from "@/views/core/public/InputOrgTree.vue";
import type { GetOrgListVo, GetOrgTreeVo } from "@/views/core/api/OrgApi";
```

**Props**：

| Prop | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `placeholder` | `string` | `"请选择组织机构"` | 输入框占位符 |
| `readonly` | `boolean` | `false` | 只读（按钮显示"查看"） |
| `disabled` | `boolean` | `false` | 禁用整个输入器 |
| `excludeNodeMethod` | `(node: GetOrgTreeVo) => boolean` | - | 返回 `false` 排除该节点 |
| `checkEnableMethod` | `(node: GetOrgTreeVo) => boolean` | - | 返回 `false` 禁用勾选该节点 |

**v-model 绑定**：

| 绑定名 | 类型 | 说明 |
| --- | --- | --- |
| `v-model`（默认） | `string[]` | 已勾选组织ID数组，**required**，不能传 `undefined` |
| `v-model:checked-org-names` | `string` | 已选名称（用"、"拼接，自动回填输入框） |

**事件**：`on-submit-entity(vos: GetOrgListVo[])` — 返回组织实体数组

```vue
<template>
  <el-form-item label="授权组织">
    <InputOrgTree
      v-model="form.orgIds"
      v-model:checked-org-names="form.orgNames"
      placeholder="请选择需要授权的组织"
      @on-submit-entity="(orgs) => (form.orgEntities = orgs)"
    />
  </el-form-item>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import InputOrgTree from "@/views/core/public/InputOrgTree.vue";
import type { GetOrgListVo } from "@/views/core/api/OrgApi";

const form = reactive({
  orgIds: [] as string[],
  orgNames: "",
  orgEntities: [] as GetOrgListVo[],
});
</script>
```

---

## 三、ModalOrgTree（纯弹窗，自控触发）

```ts
import ModalOrgTree from "@/views/core/public/ModalOrgTree.vue";
```

**v-model**：`boolean`（弹窗显隐）

**v-model:checked-org-ids**：`string[]`（已勾选组织ID数组）

其余属性均透传给内部 `OrgTree` 组件。

---

## 常见陷阱

- `InputOrgTree` 的默认 `v-model` 是 `string[]` 且 **required**，初始化必须给 `[]`，不能是 `undefined`。
- `excludeNodeMethod` / `checkEnableMethod` 返回 `false` 表示"排除/禁用"，语义与直觉相反，注意取反。
- 组件均默认 `append-to-body` + `destroy-on-close`，不要在外层再用 `v-if` 控制销毁。
- 编辑场景回显：必须在数据加载完成后再给 `v-model` 赋值，否则首次渲染为空。

## 源码

- `src/main/resources/web-ui/src/views/core/public/InputOrgTree.vue`
- `src/main/resources/web-ui/src/views/core/public/InputOrgTreeSingle.vue`
- `src/main/resources/web-ui/src/views/core/public/ModalOrgTree.vue`
- `src/main/resources/web-ui/src/views/core/public/service/InputOrgTreeService.ts`
