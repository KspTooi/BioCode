---
name: org-tree
description: 在 views 模块中接入组织机构树组件 OrgTree（支持裁剪根节点、显示层级标签、节点过滤与禁用、搜索、各种事件绑定）。当需要"展示组织树/组织架构树/左侧部门树"时使用本 Skill，避免重读源码导致配置错误。
---

# 组织机构树组件接入指南（OrgTree）

`OrgTree` 是对通用高级树组件 `StdAdvTree` 的轻量级封装，专用于渲染和操作组织机构树。它自动调用 `OrgApi.getOrgTree` 加载数据，支持裁剪显示分支、层级标签（企业/子企业/部门）、搜索过滤、节点排除与禁用，以及复选框多选或单选等功能。

---

## 核心选型与引用

```ts
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";
```

---

## 属性（Props）与事件（Emits）参考

### 1. 独有属性（Props）

| Prop | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `cropOrgId` | `string \| null` | `null` | **裁剪根组织ID**。如果设置，则整棵树只显示该组织及其下级，且自动将 `v-model` (当前选中ID) 设为该ID。同时，设置此属性时会**强制隐藏**全部根节点 `nr` |
| `nr` | `boolean` | `false` | **是否显示虚拟"全部"根节点**（受 `cropOrgId` 影响，若有裁剪则不显示） |
| `nrValue` | `string \| null` | `null` | 虚拟"全部"根节点的对应值（一般传 `null` 或特定字符串） |
| `showKindTag` | `boolean` | `false` | **是否显示层级标签**。若为 `true`，节点名称右侧会显示 `企业` (kind: 0)、`子企业` (kind: 1)、`部门` (kind: 2) 的 `el-tag` 标签 |
| `excludeNodeMethod` | `(node: GetOrgTreeVo) => boolean` | `undefined` | **节点排除过滤器**。对于树中的每个节点，若此方法返回 `false`，则该节点及其所有子节点会被**排除**（隐藏） |
| `checkEnableMethod` | `(node: GetOrgTreeVo) => boolean` | `undefined` | **勾选禁用过滤器**。若此方法返回 `false`，则复选框置灰禁用，不可勾选 |

### 2. 透传属性（以下属性全量透传到底层 `StdAdvTree` 组件）

| Prop | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `search` | `boolean` | `false` | 是否显示顶部的搜索输入框 |
| `searchPlaceholder` | `string` | `"搜索"` | 搜索框占位文字 |
| `search-cascade` | `boolean` | `false` | 是否级联搜索。若开启，匹配节点的祖先及所有后代均会保留显示 |
| `search-fields` | `string[]` | `['name']` | 搜索匹配的字段，默认仅匹配节点名称 |
| `check` | `boolean` | `false` | **是否开启勾选框**（多选/单选复选框的总开关） |
| `checkMultiple` | `boolean` | `true` | 是否支持多选（配合 `check: true` 使用，为 `false` 时变为单选复选框） |
| `checkCascade` | `boolean` | `false` | 勾选时子节点是否联动级联选中（仅在 `checkMultiple: true` 时有效） |
| `readonly` | `boolean` | `false` | 只读模式。开启后整棵树变为只读状态，无法进行点击、勾选或操作 |
| `nrTitle` | `string` | `"全部"` | 虚拟"全部"根节点的显示标题 |
| `nrIcon` | `string` | - | 虚拟"全部"根节点的显示图标，例如 `"ep:office-building"` |

### 3. v-model 双向绑定

| 绑定名 | 类型 | 说明 |
| --- | --- | --- |
| `v-model` | `string \| number \| null` | **当前点击选中的节点唯一标识**（双向绑定），支持 `null`（代表选中根节点或未选中） |
| `v-model:checked-nks` | `(string \| number)[]` | **已勾选的节点Key数组**（双向绑定，配合 `check` 复选框使用） |

### 4. 事件（Emits）

| 事件名 | 回调参数 | 说明 |
| --- | --- | --- |
| `@on-select` | `(node: GetOrgTreeVo) => void` | 点击普通组织节点时触发，返回完整的节点实体数据 |
| `@on-root-select` | `(value: string \| null) => void` | 点击虚拟"全部"根节点时触发，返回虚拟根节点的值（`nrValue`） |
| `@on-search` | `(value: string) => void` | 搜索框输入并触发过滤时响应，返回当前搜索关键字 |
| `@on-refresh` | `(value: string) => void` | 刷新事件（若开启了 searchRefresh 按钮） |
| `@on-exception` | `(error: Error) => void` | 后端树数据请求失败时触发，可在此接收错误并展示提示 |

### 5. 暴露出（Exposed）的方法

* `loadTreeData()`: 主动触发从后端重新拉取组织树数据（如增删改操作后需要局部刷新时）。

---

## 典型使用场景代码示例

### 场景一：最常用的左侧组织树列表（带搜索过滤与虚拟全部节点）

通常放置在页面左侧，用于联动右侧的表格进行数据查询。

```vue
<template>
  <div class="org-tree-wrapper">
    <OrgTree
      v-model="currentOrgId"
      :search="true"
      :search-cascade="true"
      search-placeholder="输入组织机构名称过滤..."
      :nr="true"
      nr-title="全部组织机构"
      nr-icon="ep:office-building"
      :nr-value="null"
      :show-kind-tag="true"
      @on-select="handleOrgSelect"
      @on-root-select="handleRootSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

const currentOrgId = ref<string | null>(null);

const handleOrgSelect = (node: GetOrgTreeVo): void => {
  console.log("选中了普通节点:", node);
  // 执行加载该组织下用户/资产等列表逻辑
};

const handleRootSelect = (val: string | null): void => {
  console.log("选中了虚拟全部根节点:", val);
  // 执行加载全部列表逻辑
};
</script>

<style scoped>
.org-tree-wrapper {
  height: 100%;
  padding: 12px;
  box-sizing: border-box;
}
</style>
```

---

### 场景二：带复选框的组织多选树（排除某些节点 + 禁用特定节点）

适用于分配权限、管辖范围时需要勾选多个组织的弹窗或表单中。

```vue
<template>
  <OrgTree
    v-model:checked-nks="checkedOrgIds"
    :check="true"
    :check-multiple="true"
    :check-cascade="false"
    :search="true"
    :exclude-node-method="excludeDepartmentNodes"
    :check-enable-method="disableFrozenNodes"
  />
</template>

<script setup lang="ts">
import { ref } from "vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

const checkedOrgIds = ref<string[]>([]);

// 过滤规则：排除类型为"部门"(kind === 2)的节点，整棵树只保留企业(0)和子企业(1)
const excludeDepartmentNodes = (node: GetOrgTreeVo): boolean => {
  if (node.kind === 2) {
    return false; // 返回 false 表示排除该节点
  }
  return true;
};

// 禁用规则：禁用状态为已停用/已锁定的组织节点勾选 (假设 node.status === 0 表示禁用)
const disableFrozenNodes = (node: GetOrgTreeVo): boolean => {
  if (node.status === 0) {
    return false; // 返回 false 表示禁用勾选
  }
  return true;
};
</script>
```

---

### 场景三：裁剪至当前用户管辖范围的子组织树

当由于业务安全限制，不允许用户查看完整的集团组织架构，只允许其查看自身企业及下级企业/部门时：

```vue
<template>
  <!-- 只展示 cropOrgId 及下属节点，nr 会被自动置为 false -->
  <OrgTree
    v-model="selectedOrgId"
    :crop-org-id="userManageOrgId"
    :search="true"
  />
</template>

<script setup lang="ts">
import { ref } from "vue";
import OrgTree from "@/views/core/public/OrgTree.vue";

// 假设当前用户管辖的根组织 ID，通常由接口返回或登录状态中获取
const userManageOrgId = ref("100234857201");
const selectedOrgId = ref<string>("");
</script>
```

---

## 常见开发陷阱与防范

1. **`excludeNodeMethod` 与 `checkEnableMethod` 的返回值语义**：
   * **注意**：这两个方法是**保留/启用过滤器**。返回 `false` 代表**排除（隐藏节点）/ 禁用（勾选框置灰）**。不要写成相反逻辑。
   * **示例**：排除已删除节点，方法应该写成：`const isKeep = (node) => node.deleted !== 1;` 并在 prop 中传递。

2. **多选绑定**：
   * 使用复选框获取已选数组时，必须使用双向绑定 `v-model:checked-nks`，它的默认值建议初始化为 `[]`，不可为 `undefined`。
   * 即使在 `check: true` 模式下，直接点击行依然会修改 `v-model`（当前高亮行ID）的值，勾选才会修改 `v-model:checked-nks` 的数组值，请根据业务场景分清你是想要“高亮单选”还是“复选框勾选”。

3. **`cropOrgId` 传入后虚拟根节点失效**：
   * 组件内部逻辑在 `cropOrgId` 存在时会直接强制将 `nr`（是否显示虚拟根节点）置为 `false`。这是设计如此（既然树已被强制裁剪到某一特定节点作为主根，再展示包含全系统的大“全部”节点就不合逻辑）。

4. **主动重载数据**：
   * 组织架构发生变更（如新增了部门、编辑了企业名称）后，若需要树自动同步，可以给组件声明一个 `ref` 并在保存成功后调用 `loadTreeData()` 方法。

---

## 相关源码路径

* 组织机构树实现：`src/main/resources/web-ui/src/views/core/public/OrgTree.vue`
* 关联树基础 API：`src/main/resources/web-ui/src/views/core/api/OrgApi.ts`
* 底层高级树组件：`src/main/resources/web-ui/src/soa/std-series/StdAdvTree.vue`
* 底层高级树 Service：`src/main/resources/web-ui/src/soa/std-series/service/StdAdvTreeService.ts`
