<template>
  <div v-loading="loading" class="org-tree-container">
    <div class="filter-wrapper pr-2 pl-2">
      <el-input
        v-model="filterText"
        placeholder="搜索机构/部门"
        clearable
        :prefix-icon="SearchIcon"
        size="small"
        @input="onFilterInput"
      />
    </div>

    <el-scrollbar class="tree-wrapper">
      <div v-if="showHeader && !multiple" class="all-org-node" :class="{ 'is-active': isAllSelected }" @click="onSelectAll">
        <el-icon class="node-icon"><OfficeBuildingIcon /></el-icon>
        <span class="node-label">全部公司</span>
      </div>
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="defaultProps"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        :show-checkbox="multiple"
        node-key="id"
        highlight-current
        default-expand-all
        class="custom-tree"
        @node-click="onNodeClick"
        @check="onCheckChange"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node" :class="{ 'is-disabled': isNodeDisabled(data) }">
            <el-icon class="node-icon">
              <component :is="resolveIcon(iconMap[data.kind])" />
            </el-icon>
            <span class="node-label">{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted, nextTick } from "vue";
import type { ElTree } from "element-plus";
import { Search, OfficeBuilding } from "@element-plus/icons-vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";
import OrgTreeService from "@/views/core/service/OrgTreeService";
import ComIconService from "@/soa/com-series/service/ComIconService.ts";

const SearchIcon = markRaw(Search);
const OfficeBuildingIcon = markRaw(OfficeBuilding);

const emit = defineEmits<{
  (e: "on-select", node: GetOrgTreeVo | null): void;
  (e: "on-check", nodes: GetOrgTreeVo[]): void;
}>();

const props = withDefaults(
  defineProps<{
    showHeader?: boolean;
    multiple?: boolean;
    defaultCheckedKeys?: string[];
    selectKind?: "all" | "root" | "dept";
    /** 树加载完成后默认选中的组织节点 id（如当前登录用户所属企业 rootId） */
    initialSelectedOrgId?: string;
  }>(),
  {
    showHeader: true,
    multiple: false,
    defaultCheckedKeys: () => [],
    selectKind: "all",
    initialSelectedOrgId: undefined,
  }
);

const { resolveIcon } = ComIconService.useIconService();

const iconMap = {
  0: "ep:office-building",
  1: "mdi:domain",
  2: "mdi:sitemap",
  3: "mdi:account-group",
};

const treeRef = ref<InstanceType<typeof ElTree>>();
const isAllSelected = ref(true);
const currentSelectedKey = ref<string | number | null>(null);
const { treeData, loading, filterText, loadTreeData, onSelectOrg } = OrgTreeService.useOrgTree();

const defaultProps = {
  children: "children",
  label: "name",
};

const onSelectAll = (): void => {
  isAllSelected.value = true;
  currentSelectedKey.value = null;
  treeRef.value?.setCurrentKey(null);
  emit("on-select", null);
  onSelectOrg(null);
};

const onFilterInput = (val: string): void => {
  treeRef.value?.filter(val);
};

const filterNode = (value: string, data: GetOrgTreeVo): boolean => {
  if (!value) {
    return true;
  }
  return data.name.includes(value);
};

const isNodeDisabled = (data: GetOrgTreeVo): boolean => {
  if (props.selectKind === "all") {
    return false;
  }
  if (props.selectKind === "root") {
    return data.kind !== 1;
  }
  if (props.selectKind === "dept") {
    return data.kind === 1;
  }
  return false;
};

const onNodeClick = (data: GetOrgTreeVo): void => {
  if (props.multiple) {
    return;
  }
  if (isNodeDisabled(data)) {
    treeRef.value?.setCurrentKey(currentSelectedKey.value);
    return;
  }
  isAllSelected.value = false;
  currentSelectedKey.value = data.id;
  emit("on-select", data);
  onSelectOrg(data);
};

function findOrgById(nodes: GetOrgTreeVo[], id: string): GetOrgTreeVo | null {
  for (const n of nodes) {
    if (n.id === id) {
      return n;
    }
    if (n.children?.length) {
      const found = findOrgById(n.children, id);
      if (found) {
        return found;
      }
    }
  }
  return null;
}

/** 按 id 选中组织节点（用于默认选中当前企业等场景） */
const selectOrgById = (id: string): boolean => {
  const node = findOrgById(treeData.value, id);
  if (!node || isNodeDisabled(node)) {
    return false;
  }
  isAllSelected.value = false;
  currentSelectedKey.value = node.id;
  treeRef.value?.setCurrentKey(node.id);
  emit("on-select", node);
  onSelectOrg(node);
  return true;
};

const onCheckChange = (): void => {
  if (!treeRef.value) {
    return;
  }
  const checkedNodes = treeRef.value.getCheckedNodes() as GetOrgTreeVo[];
  emit("on-check", checkedNodes);
};

const reset = (): void => {
  onSelectAll();
  if (props.multiple && treeRef.value) {
    treeRef.value.setCheckedKeys([]);
  }
};

const getCheckedNodes = (): GetOrgTreeVo[] | undefined => {
  return treeRef.value?.getCheckedNodes() as GetOrgTreeVo[];
};

const setCheckedKeys = (keys: string[]): void => {
  treeRef.value?.setCheckedKeys(keys);
};

onMounted(async () => {
  await loadTreeData();
  if (props.initialSelectedOrgId) {
    await nextTick();
    selectOrgById(props.initialSelectedOrgId);
  }
});

defineExpose({
  reset,
  getCheckedNodes,
  setCheckedKeys,
  loadTreeData,
  selectOrgById,
});
</script>

<style scoped>
.org-tree-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--el-bg-color);
  box-sizing: border-box;
}

.filter-wrapper {
  margin-bottom: 8px;
}

.tree-wrapper {
  flex: 1;
  min-height: 0;
}

.custom-tree {
  background: transparent;
}

.all-org-node {
  display: flex;
  align-items: center;
  height: 28px;
  padding-left: 27.5px; /* Align with tree nodes (expand icon width) */
  cursor: pointer;
  margin-bottom: 1px;
  transition: background-color 0.2s;
  font-size: 13px;
}

.all-org-node:hover {
  background-color: var(--el-color-primary-light-9);
}

.all-org-node.is-active {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: bold;
}

.all-org-node.is-active .node-label {
  color: var(--el-color-primary);
}

.custom-tree-node {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.node-icon {
  margin-right: 6px;
  font-size: 14px;
  color: var(--el-color-primary);
}

.node-label {
  color: var(--el-text-color-regular);
}

.custom-tree-node.is-disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.custom-tree-node.is-disabled .node-icon,
.custom-tree-node.is-disabled .node-label {
  color: var(--el-text-color-disabled);
}

:deep(.el-input__wrapper) {
  border-radius: 0;
}

:deep(.el-tree-node__content) {
  height: 28px;
  border-radius: 0;
  margin-bottom: 1px;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-tree-node__content:has(.is-disabled):hover) {
  background-color: transparent;
  cursor: not-allowed;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: bold;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content .node-label) {
  color: var(--el-color-primary);
}

:deep(.el-tree-node__expand-icon) {
  font-size: 16px;
}
</style>
