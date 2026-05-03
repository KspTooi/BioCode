<!--
 * @Author: KspTooi
 * @Date: 2026-04-29 16:36:26
 * @Description: 高级树组件，这个树组件提供更多丰富的高级功能，例如自定义搜索、刷新、根节点、节点操作等。
-->
<template>
  <div v-loading="loading" class="adv-tree-container">
    <div v-if="search || searchRefresh" class="filter-wrapper">
      <el-input
        v-if="search"
        v-model="filterText"
        :placeholder="searchPlaceholder || '搜索'"
        clearable
        :prefix-icon="SearchIcon"
        size="small"
        @input="onFilterInput"
      />
      <el-button
        v-if="searchRefresh"
        :class="['refresh-btn', { 'refresh-btn--full': !search }]"
        size="small"
        :icon="RefreshIcon"
        @click="emit('on-refresh', filterText)"
      >
        刷新
      </el-button>
    </div>

    <el-scrollbar class="tree-wrapper">
      <div v-if="nr" class="root-node" :class="{ 'is-active': isRootSelected }" @click="onRootClick">
        <span class="root-node-left flex items-center min-w-0">
          <el-icon v-if="nrIcon" class="node-pre-icon">
            <component :is="resolveIcon(nrIcon)" />
          </el-icon>
          <span class="node-label">{{ nrTitle || "全部" }}</span>
        </span>
        <span class="adv-tree-actions flex items-center" @click.stop>
          <slot name="root-actions" />
        </span>
      </div>

      <el-tree
        ref="treeRef"
        :data="data"
        :props="defaultProps"
        :filter-node-method="filterNode"
        :expand-on-click-node="expandOnClick"
        :node-key="nk ?? 'id'"
        highlight-current
        :default-expand-all="expandOnDefault"
        class="custom-tree"
        @node-click="onNodeClick"
      >
        <template #default="{ node, data: nodeData }">
          <span class="custom-tree-node flex-1 flex items-center justify-between pr-2 text-[13px]">
            <span class="node-left flex items-center min-w-0">
              <slot name="icon" :node="node" :data="nodeData">
                <el-icon v-if="ni && nodeData[ni]" class="node-pre-icon">
                  <component :is="resolveIcon(nodeData[ni])" />
                </el-icon>
              </slot>
              <span class="node-label">
                <slot name="label" :node="node" :data="nodeData">{{ node.label }}</slot>
              </span>
              <slot name="append" :node="node" :data="nodeData" />
            </span>
            <span class="adv-tree-actions flex items-center" @click.stop>
              <slot name="actions" :node="node" :data="nodeData">
                <template v-if="action">
                  <el-icon v-if="actionMode?.includes('add')" @click="emit('on-add', nodeData)">
                    <component :is="resolveIcon('ep:plus')" />
                  </el-icon>
                  <el-icon v-if="actionMode?.includes('edit')" @click="emit('on-edit', nodeData)">
                    <component :is="resolveIcon('ep:edit')" />
                  </el-icon>
                  <el-icon v-if="actionMode?.includes('remove')" class="action-danger" @click="emit('on-remove', nodeData)">
                    <component :is="resolveIcon('ep:delete')" />
                  </el-icon>
                </template>
              </slot>
            </span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { markRaw } from "vue";
import { Search, Refresh } from "@element-plus/icons-vue";
import type { StdAdvTreeProps, StdAdvTreeEmits } from "@/soa/std-series/service/StdAdvTree";
import StdAdvTreeService from "@/soa/std-series/service/StdAdvTree";
import ComIconService from "@/soa/com-series/service/ComIconService";

const SearchIcon = markRaw(Search);
const RefreshIcon = markRaw(Refresh);
const { resolveIcon } = ComIconService.useIconService();

const props = withDefaults(defineProps<StdAdvTreeProps>(), {
  modelValue: null,
  initValue: undefined,
  data: () => [],
  search: false,
  searchPlaceholder: "搜索",
  searchRefresh: false,
  nr: false,
  nrTitle: "全部",
  nrIcon: undefined,
  nrValue: "-1",
  ni: undefined,
  nk: "id",
  nt: "name",
  nc: "children",
  loading: false,
  expandOnDefault: true,
  expandOnClick: false,
  action: false,
  actionMode: () => ["add", "edit", "remove"],
});

const emit = defineEmits<StdAdvTreeEmits>();

const {
  treeRef,
  filterText,
  isRootSelected,
  defaultProps,
  filterNode,
  onFilterInput,
  onNodeClick,
  onRootClick,
  reset,
  filter,
  getTreeRef,
} = StdAdvTreeService.useStdAdvTree(props, emit);

defineExpose({ reset, filter, getTreeRef });
</script>

<style scoped>
.adv-tree-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--el-bg-color);
  box-sizing: border-box;
  user-select: none;
}

/* ── 搜索区域 ── */
.filter-wrapper {
  flex-shrink: 0;
  padding: 10px 10px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-wrapper .el-input {
  flex: 1;
  min-width: 0;
}

.refresh-btn {
  flex-shrink: 0;
  border-color: transparent;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  transition:
    background-color 0.2s,
    color 0.2s;
}

.refresh-btn--full {
  flex: 1;
}

.refresh-btn:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-color: transparent;
}

:deep(.filter-wrapper .el-input__wrapper) {
  background-color: var(--el-fill-color-light);
  box-shadow: none;
  border: 1px solid transparent;
  transition:
    border-color 0.2s,
    background-color 0.2s;
}

:deep(.filter-wrapper .el-input__wrapper:hover),
:deep(.filter-wrapper .el-input__wrapper.is-focus) {
  border-color: var(--el-color-primary);
  background-color: var(--el-bg-color);
}

:deep(.filter-wrapper .el-input__prefix-inner .el-icon) {
  color: var(--el-text-color-placeholder);
}

/* ── 滚动区 ── */
.tree-wrapper {
  flex: 1;
  min-height: 0;
  padding: 6px 4px;
}

.custom-tree {
  background: transparent;
}

/* ── 节点行 ── */
:deep(.el-tree-node__content) {
  height: 32px;
  margin-bottom: 2px;
  transition: background-color 0.15s;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: linear-gradient(90deg, var(--el-color-primary-light-7) 0%, var(--el-color-primary-light-9) 100%);
  font-weight: 600;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content .node-label) {
  color: var(--el-color-primary);
}

/* 选中行顶部主题色横线 */
:deep(.el-tree-node.is-current > .el-tree-node__content)::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0px;
  right: 0px;
  height: 1.5px;
  background: linear-gradient(90deg, var(--el-color-primary) 0%, var(--el-color-primary-light-5) 100%);
}

:deep(.el-tree-node__content) {
  position: relative;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 15px;
  color: var(--el-text-color-secondary);
  transition: color 0.15s;
}

:deep(.el-tree-node__expand-icon:hover) {
  color: var(--el-color-primary);
}

/* ── 节点左侧内容区（图标 + 文字） ── */
.node-left {
  flex: 1;
  min-width: 0;
}

/* ── 根节点行 ── */
.root-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 8px 0 11px;
  border-radius: 3px;
  margin-bottom: 2px;
  cursor: pointer;
  font-size: 13px;
  transition: background-color 0.15s;
  position: relative;
}

.root-node:hover {
  background-color: var(--el-color-primary-light-9);
}

.root-node.is-active {
  background: linear-gradient(90deg, var(--el-color-primary-light-7) 0%, var(--el-color-primary-light-9) 100%);
  font-weight: 600;
}

.root-node.is-active::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1.5px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--el-color-primary) 0%, var(--el-color-primary-light-5) 100%);
}

.root-node.is-active .node-label {
  color: var(--el-color-primary);
}

.root-node-left {
  flex: 1;
  min-width: 0;
}

.root-node .adv-tree-actions {
  visibility: hidden;
}

.root-node:hover .adv-tree-actions,
.root-node.is-active .adv-tree-actions {
  visibility: visible;
}

/* ── 节点前置图标 ── */
.node-pre-icon {
  flex-shrink: 0;
  margin-right: 6px;
  font-size: 14px;
  color: var(--el-color-primary);
  transition: color 0.15s;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) .node-pre-icon {
  color: var(--el-color-primary);
}

/* ── 节点文字 ── */
.node-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 节点操作按钮 ── */
.adv-tree-actions {
  flex-shrink: 0;
  gap: 2px;
  visibility: hidden;
}

:deep(.el-tree-node__content:hover) .adv-tree-actions,
:deep(.el-tree-node.is-current > .el-tree-node__content) .adv-tree-actions {
  visibility: visible;
}

.adv-tree-actions .el-icon {
  color: var(--el-color-primary);
  font-size: 23px;
  font-weight: 800;
  padding: 3px;
  transition:
    background-color 0.15s,
    color 0.15s;
  cursor: pointer;
}

.adv-tree-actions .el-icon:hover {
  background-color: var(--el-color-primary-light-7);
}

.adv-tree-actions .action-danger {
  color: var(--el-text-color-secondary);
}

.adv-tree-actions .action-danger:hover {
  background-color: #fef2f2;
  color: #ef4444;
}
</style>
