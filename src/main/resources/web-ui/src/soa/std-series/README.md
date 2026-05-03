# Std-Series 标准列表页组件系列

## 概述

Std-Series 是一套用于构建标准列表页面的原子化布局组件，提供统一的样式规范和灵活的组合方式。

## 设计原则

- **原子化**：每个组件只负责局部样式和布局，不负责业务逻辑
- **可组合**：可以自由组合使用，适应不同的页面布局需求
- **样式统一**：统一管理间距、边框、背景色等样式规范

## 组件列表

### 1. StdListContainer.vue

最外层容器组件，提供统一的 padding 和宽度限制。

```vue
<StdListContainer>
  <!-- 内容 -->
</StdListContainer>
```

### 2. StdListAreaQuery.vue

查询表单区域容器，提供底部间距。

```vue
<StdListAreaQuery>
  <el-form>
    <!-- 查询表单 -->
  </el-form>
</StdListAreaQuery>
```

### 3. StdListAreaAction.vue

操作按钮区域容器，提供顶部虚线分隔和间距。

```vue
<StdListAreaAction>
  <el-button type="success">创建</el-button>
</StdListAreaAction>
```

### 4. StdListAreaTable.vue

表格区域容器，提供底部间距和横向滚动。

```vue
<StdListAreaTable>
  <el-table :data="list">
    <!-- 表格列 -->
  </el-table>
</StdListAreaTable>
```

### 5. StdListLayout.vue

快捷布局组件，内部组合了上述所有组件，提供插槽式使用方式。

```vue
<StdListLayout>
  <template #query>
    <!-- 查询表单 -->
  </template>
  
  <template #actions>
    <!-- 操作按钮 -->
  </template>
  
  <template #table>
    <!-- 表格 -->
  </template>
</StdListLayout>
```

## 使用场景

### 场景A：标准列表页（推荐使用 StdListLayout）

适用于简单的 CRUD 列表页，使用 `StdListLayout` 快速搭建。

```vue
<template>
  <StdListLayout>
    <template #query>
      <el-form :model="queryForm">
        <el-input v-model="queryForm.name" />
      </el-form>
    </template>
    
    <template #actions>
      <el-button type="success" @click="create">创建</el-button>
    </template>
    
    <template #table>
      <el-table :data="list">
        <el-table-column prop="name" label="名称" />
      </el-table>
    </template>
  </StdListLayout>
</template>
```

### 场景B：复杂布局（使用原子组件自由组合）

适用于需要左右分栏、嵌套布局等复杂场景。

```vue
<template>
  <splitpanes>
    <pane>
      <!-- 左侧树 -->
    </pane>
    <pane>
      <StdListContainer>
        <StdListAreaQuery>
          <!-- 查询表单 -->
        </StdListAreaQuery>
        
        <StdListAreaAction>
          <!-- 操作按钮 -->
        </StdListAreaAction>
        
        <StdListAreaTable>
          <!-- 表格 -->
        </StdListAreaTable>
      </StdListContainer>
    </pane>
  </splitpanes>
</template>
```

## 样式规范

| 组件 | 样式说明 |
|------|---------|
| StdListContainer | padding: 20px, 横向滚动支持 |
| StdListAreaQuery | margin-bottom: 15px |
| StdListAreaAction | border-top: 2px dashed, padding-top: 15px, margin-bottom: 15px |
| StdListAreaTable | margin-bottom: 20px, 横向滚动支持 |

## 参考示例

- 标准布局：`@/views/core/OrgManager.vue`
- 复杂布局：`@/views/core/UserManager.vue`

---

## StdAdvTree 高级树组件

受控、字段无关的通用树组件。数据与 loading 完全由父组件注入，组件内部不发任何请求。

### Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `Array<any>` | `[]` | 树数据源 |
| `loading` | `boolean` | `false` | 是否显示 loading 遮罩 |
| `search` | `boolean` | `false` | 是否显示搜索框 |
| `searchPlaceholder` | `string` | `'搜索'` | 搜索框占位文字 |
| `nk` | `string` | `'id'` | 节点唯一标识字段名 |
| `nt` | `string` | `'name'` | 节点显示文字字段名 |
| `nc` | `string` | `'children'` | 节点子集字段名 |
| `defaultExpandAll` | `boolean` | `true` | 是否默认展开全部 |
| `expandOnClickNode` | `boolean` | `false` | 点击节点本身是否展开/折叠 |
| `showActions` | `boolean` | `false` | 是否显示 hover 操作按钮区（总开关） |
| `showAdd` | `boolean` | `false` | 是否显示新增图标按钮 |
| `showEdit` | `boolean` | `false` | 是否显示编辑图标按钮 |
| `showRemove` | `boolean` | `false` | 是否显示删除图标按钮 |

### Emits

| 事件 | 参数 | 说明 |
|------|------|------|
| `on-select` | `node: any` | 节点被点击/选中时触发 |
| `on-add` | `node: any` | 点击内置新增按钮时触发 |
| `on-edit` | `node: any` | 点击内置编辑按钮时触发 |
| `on-remove` | `node: any` | 点击内置删除按钮时触发 |
| `on-search` | `value: string` | 搜索框输入时触发 |

### Slots

| 插槽 | 作用域 | 说明 |
|------|--------|------|
| `#actions` | `{ node, data }` | 自定义节点右侧操作区，提供后会覆盖 `showXxx` 内置按钮 |

### Expose

| 方法 | 说明 |
|------|------|
| `reset()` | 清空搜索框与选中状态 |
| `filter(val)` | 手动触发搜索过滤 |
| `getTreeRef()` | 获取底层 `ElTree` 实例 |

### 示例 A：最小用法（只读展示 + 搜索 + 选中回调）

```vue
<template>
  <StdAdvTree
    :data="treeData"
    :loading="loading"
    search
    search-placeholder="搜索部门"
    nt="label"
    @on-select="onSelect"
  />
</template>

<script setup lang="ts">
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

const loading = ref(false);
const treeData = ref([]);

const onSelect = (node: any) => {
  console.log("选中节点", node);
};
</script>
```

### 示例 B：使用 `#actions` 插槽自定义节点操作按钮

```vue
<template>
  <StdAdvTree
    :data="treeData"
    search
    nk="id"
    nt="name"
    nc="children"
    @on-select="onSelect"
  >
    <template #actions="{ data }">
      <span class="hidden group-hover:flex items-center gap-0.5 text-[20px]">
        <el-icon
          class="p-0.5 rounded hover:bg-[var(--el-color-primary-light-7)] cursor-pointer"
          title="新建子节点"
          @click.stop="onCreate(data)"
        >
          <Plus />
        </el-icon>
        <el-icon
          class="p-0.5 rounded hover:bg-red-50 hover:text-red-500 cursor-pointer"
          title="删除节点"
          @click.stop="onDelete(data)"
        >
          <Delete />
        </el-icon>
      </span>
    </template>
  </StdAdvTree>
</template>
```
