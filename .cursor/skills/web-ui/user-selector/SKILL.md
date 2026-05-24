---
name: user-selector
description: 在 views 模块中接入用户选择器组件 ModalUserSelector。当需要"选用户/选人员/人员选择/选负责人/指定用户/用户选择器"时使用本 Skill，避免重新阅读源码并选错 props/事件。
---

# 用户选择器接入指南（ModalUserSelector）

用户选择一律走 `ModalUserSelector`，禁止自己写带组织树的用户列表弹窗。

## Import

```ts
import ModalUserSelector from "@/views/core/public/ModalUserSelector.vue";
import type { GetUserListVo } from "@/views/core/api/UserApi";
```

## Props

| Prop | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `title` | `string` | `"选择用户"` | 弹窗标题 |
| `width` | `string \| number` | `"80%"` | 弹窗宽度 |
| `mode` | `"single" \| "multiple"` | `"multiple"` | 单选/多选 |
| `readonly` | `boolean` | `false` | 只读模式（只展示，不允许提交） |
| `max` | `number \| null` | `null` | 最大可选数量，超出后提交按钮禁用 |
| `cropOrgId` | `string \| null` | `null` | 裁剪根组织ID，仅显示该组织及下级 |

## v-model 绑定（共三个）

| 绑定名 | 类型 | 说明 |
| --- | --- | --- |
| `v-model`（默认） | `boolean` | 弹窗显隐 |
| `v-model:current-org-id` | `string \| null` | 当前选中的左侧组织节点ID |
| `v-model:checked-user-ids` | `string[]` | 已勾选用户ID数组（单选时也是数组，取 `[0]`） |

## 事件

| 事件 | 参数 | 说明 |
| --- | --- | --- |
| `on-submit` | `string[]` | 提交时返回用户ID数组 |
| `on-submit-entity` | `GetUserListVo[]` | 提交时返回用户完整实体 |
| `on-close` | - | 弹窗关闭 |

## 典型用法（多选）

```vue
<template>
  <el-button @click="userModalVisible = true">选择用户</el-button>
  <ModalUserSelector
    v-model="userModalVisible"
    v-model:checked-user-ids="form.userIds"
    :max="10"
    @on-submit-entity="onUserPicked"
  />
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import ModalUserSelector from "@/views/core/public/ModalUserSelector.vue";
import type { GetUserListVo } from "@/views/core/api/UserApi";

const userModalVisible = ref(false);
const form = reactive({ userIds: [] as string[] });

const onUserPicked = (vos: GetUserListVo[]): void => {
  console.log("已选用户：", vos);
};
</script>
```

## 典型用法（单选 + 裁剪到指定组织）

```vue
<ModalUserSelector
  v-model="visible"
  v-model:checked-user-ids="selectedIds"
  mode="single"
  :crop-org-id="currentOrgId"
  title="选择负责人"
  width="900px"
  @on-submit="(ids) => (form.ownerId = ids[0] ?? '')"
/>
```

## 常见陷阱

- 单选模式下 `v-model:checked-user-ids` 仍是 `string[]`，取值用 `ids[0] ?? ""`，不是直接赋字符串。
- 提交时组件会重新调用后端 `getUserList` 校验ID有效性，后端不存在的ID会被自动剔除，不要假设提交结果与传入完全一致。
- 组件默认 `append-to-body` + `destroy-on-close`，不要在外层再用 `v-if` 控制销毁。
- 编辑场景回显：必须在数据加载完成后再给 `v-model:checked-user-ids` 赋值，否则首次渲染为空。

## 源码

- `src/main/resources/web-ui/src/views/core/public/ModalUserSelector.vue`
- `src/main/resources/web-ui/src/views/core/public/service/ModalUserSelectorService.ts`
