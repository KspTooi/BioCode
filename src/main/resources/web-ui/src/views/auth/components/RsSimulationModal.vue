<template>
  <el-dialog
    :model-value="props.visible"
    title="数据权限模拟器"
    width="1040px"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    @close="onClose"
  >
    <!-- 顶部说明区 (高度固定) -->
    <div class="sim-header">
      <el-icon class="sim-header-icon"><InfoFilled /></el-icon>
      <span>点击左侧组织树节点设置虚拟用户位置，右侧选择 RS 等级，系统实时计算并高亮可见节点范围。</span>
    </div>

    <!-- 主体两栏 (固定高度,内部自行滚动) -->
    <div class="sim-body">
      <!-- 左侧：组织树 -->
      <div class="sim-col sim-col-left">
        <div class="panel-title">
          <span>选择模拟节点</span>
          <span class="panel-sub">点击节点设置虚拟用户位置</span>
        </div>
        <div class="tree-search-wrap">
          <el-input v-model="filterText" placeholder="搜索机构/部门" clearable size="small" @input="onFilterInput">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <!-- el-scrollbar 替代原生滚动条 -->
        <el-scrollbar v-loading="treeLoading" class="tree-scrollbar">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            :filter-node-method="filterNode"
            :expand-on-click-node="false"
            node-key="id"
            default-expand-all
            class="sim-tree"
            @node-click="onNodeClick"
          >
            <template #default="{ data }">
              <span class="sim-node" :class="nodeState(data.id)">
                <el-icon class="sim-node-icon">
                  <component :is="resolveIcon(iconMap[data.kind])" />
                </el-icon>
                <span class="sim-node-label">{{ data.name }}</span>
                <!-- 企业类型标签 -->
                <el-tag class="sim-kind-tag" :type="kindTagType(data.kind) as any" size="small" disable-transitions>{{
                  kindLabel(data.kind)
                }}</el-tag>
                <span v-if="selectedOrg?.id === data.id" class="sim-tag">当前用户所在</span>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>

      <!-- 分隔线 -->
      <div class="sim-divider" />

      <!-- 右侧：控制面板 -->
      <div class="sim-col sim-col-right">
        <div class="panel-title">
          <span>模拟配置</span>
        </div>
        <!-- 右侧内容区使用 el-scrollbar 承载,防止内容撑开弹窗 -->
        <el-scrollbar class="control-scrollbar">
          <!-- RS 等级选择器(卡片列表) -->
          <div class="control-section">
            <div class="section-label">数据权限等级</div>
            <div class="rs-list">
              <el-tooltip
                v-for="opt in RS_OPTIONS"
                :key="opt.value"
                :content="opt.disabled ? '依赖组配置，模拟器不支持' : ''"
                placement="right"
                :disabled="!opt.disabled"
              >
                <div
                  class="rs-item"
                  :class="{ active: rsLevel === opt.value, 'rs-item-disabled': opt.disabled }"
                  @click="onRsChange(opt)"
                >
                  <div class="rs-item-left">
                    <span class="rs-badge" :class="`rs-badge-${opt.type || 'default'}`">{{ opt.value }}</span>
                    <span class="rs-name">{{ opt.label }}</span>
                  </div>
                  <span class="rs-desc">{{ opt.desc }}</span>
                </div>
              </el-tooltip>
            </div>
          </div>

          <!-- 当前节点信息 -->
          <div class="control-section">
            <div class="section-label">当前模拟节点</div>
            <div v-if="selectedOrg" class="node-info-card">
              <div class="node-info-row">
                <span class="node-info-key">名称</span>
                <span class="node-info-val">{{ selectedOrg.name }}</span>
              </div>
              <div class="node-info-row">
                <span class="node-info-key">类型</span>
                <el-tag :type="kindTagType(selectedOrg.kind) as any" size="small" disable-transitions>
                  {{ kindLabel(selectedOrg.kind) }}
                </el-tag>
              </div>
              <div class="node-info-row">
                <span class="node-info-key">节点ID</span>
                <span class="node-info-val node-id-text">{{ selectedOrg.id }}</span>
              </div>
            </div>
            <div v-else class="node-hint">请先点击左侧组织树节点</div>
          </div>

          <!-- 可见性统计 -->
          <div class="control-section">
            <div class="section-label">模拟结果</div>
            <div v-if="!selectedOrg" class="node-hint">请先选择模拟节点</div>
            <div v-else-if="simulating" class="node-hint">计算中...</div>
            <div v-else class="result-card">
              <div class="result-row">
                <span class="result-key">可见范围</span>
                <span v-if="allMode" class="result-val result-all">
                  <el-icon><SuccessFilled /></el-icon>
                  全集团可见
                </span>
                <span v-else-if="visibleSet.size === 0" class="result-val result-none">
                  <el-icon><CircleCloseFilled /></el-icon>
                  无可见节点
                </span>
                <span v-else class="result-val result-partial">
                  <el-icon><CircleCheckFilled /></el-icon>
                  {{ visibleSet.size }} 个节点可见
                </span>
              </div>
              <div class="result-row">
                <span class="result-key">当前等级</span>
                <el-tag :type="currentRsOption?.type as any" size="small" disable-transitions>
                  {{ currentRsOption?.label }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <template #footer>
      <el-button @click="onClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { ElMessage, type ElTree } from "element-plus";
import { Search, InfoFilled, SuccessFilled, CircleCloseFilled, CircleCheckFilled } from "@element-plus/icons-vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";
import OrgApi from "@/views/core/api/OrgApi";
import AdminGroupApi from "@/views/auth/api/GroupApi";
import ComIconService from "@/soa/com-series/service/ComIconService.ts";

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const { resolveIcon } = ComIconService.useIconService();

// 节点kind对应的图标
const iconMap: Record<number, string> = {
  0: "ep:office-building",
  1: "mdi:domain",
  2: "mdi:sitemap",
  3: "mdi:account-group",
};

// kind对应的中文标签
const kindLabel = (kind: number): string => {
  const labels: Record<number, string> = {
    0: "企业",
    1: "子企业",
    2: "部门",
    3: "班组",
  };
  return labels[kind] ?? "未知";
};

// kind对应的tag颜色
const kindTagType = (kind: number): string => {
  const map: Record<number, string> = {
    0: "primary",
    1: "success",
    2: "warning",
    3: "info",
  };
  return map[kind] ?? "info";
};

// RS等级选项配置(含desc说明字段)
const RS_OPTIONS = [
  { value: 0, label: "全集团", desc: "本租户全部数据", type: "primary", disabled: false },
  { value: 10, label: "本公司及下级", desc: "所属公司 + 下级公司全树", type: "success", disabled: false },
  { value: 20, label: "仅本公司", desc: "本公司直属,不含子公司", type: "info", disabled: false },
  { value: 30, label: "本部门及下级", desc: "所属部门 + 下级部门全树", type: "warning", disabled: false },
  { value: 40, label: "仅本部门", desc: "只能看本部门一个节点", type: "danger", disabled: false },
  { value: 50, label: "仅本人", desc: "只能看自己创建的数据", type: "", disabled: false },
  { value: 60, label: "指定组织", desc: "依赖组配置,模拟器不支持", type: "info", disabled: true },
  { value: 100, label: "拒绝所有", desc: "无任何数据可见", type: "danger", disabled: false },
];

// 树相关状态
const treeData = ref<GetOrgTreeVo[]>([]);
const treeLoading = ref(false);
const filterText = ref("");
const treeRef = ref<InstanceType<typeof ElTree>>();

const treeProps = {
  children: "children",
  label: "name",
};

// 模拟相关状态
const selectedOrg = ref<GetOrgTreeVo | null>(null);
const rsLevel = ref<number>(0);
const simulating = ref(false);
const allMode = ref(false);
const visibleSet = ref<Set<string>>(new Set());

// 当前选中RS选项
const currentRsOption = computed(() => RS_OPTIONS.find((o) => o.value === rsLevel.value));

/**
 * 计算节点的显示状态
 * selected: 模拟用户所在节点
 * visible: 可见节点(命中visibleOrgIds或allMode=true)
 * hidden: 其它节点(有模拟结果但未命中)
 */
const nodeState = (id: string): string => {
  if (selectedOrg.value?.id === id) {
    return "selected";
  }
  if (!selectedOrg.value) {
    return "";
  }
  if (allMode.value) {
    return "visible";
  }
  if (visibleSet.value.has(id)) {
    return "visible";
  }
  return "hidden";
};

/**
 * 加载组织树数据
 */
const loadTreeData = async (): Promise<void> => {
  treeLoading.value = true;
  try {
    treeData.value = await OrgApi.getOrgTree({});
  } catch (error: any) {
    ElMessage.error(error.message || "加载组织树失败");
  } finally {
    treeLoading.value = false;
  }
};

/**
 * 树过滤输入处理
 */
const onFilterInput = (val: string): void => {
  treeRef.value?.filter(val);
};

/**
 * 树节点过滤方法
 */
const filterNode = (value: string, data: GetOrgTreeVo): boolean => {
  if (!value) {
    return true;
  }
  return data.name.includes(value);
};

/**
 * 点击树节点 切换模拟用户位置并重新模拟
 */
const onNodeClick = async (data: GetOrgTreeVo): Promise<void> => {
  selectedOrg.value = data;
  await runSimulate();
};

/**
 * 点击RS等级卡片 切换等级(disabled项拦截)
 */
const onRsChange = (opt: (typeof RS_OPTIONS)[number]): void => {
  if (opt.disabled) {
    return;
  }
  rsLevel.value = opt.value;
};

/**
 * 调用后端 simulateRs 接口计算可见节点
 */
const runSimulate = async (): Promise<void> => {
  if (!selectedOrg.value) {
    return;
  }

  simulating.value = true;
  allMode.value = false;
  visibleSet.value = new Set();

  try {
    const result = await AdminGroupApi.simulateRs({
      orgId: selectedOrg.value.id,
      rsLevel: rsLevel.value,
    });
    allMode.value = result.allMode;
    visibleSet.value = new Set(result.visibleOrgIds);
  } catch (error: any) {
    ElMessage.error(error.message || "模拟计算失败");
    allMode.value = false;
    visibleSet.value = new Set();
  } finally {
    simulating.value = false;
  }
};

/**
 * 关闭弹窗 重置所有状态
 */
const onClose = (): void => {
  selectedOrg.value = null;
  visibleSet.value = new Set();
  allMode.value = false;
  filterText.value = "";
  rsLevel.value = 0;
  emit("close");
};

// 弹窗打开时加载组织树
watch(
  () => props.visible,
  async (newVal) => {
    if (!newVal) {
      return;
    }
    await loadTreeData();
  }
);

// RS等级变化时重新模拟
watch(rsLevel, async () => {
  await runSimulate();
});
</script>

<style scoped>
/* 顶部说明栏 */
.sim-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  margin-bottom: 14px;
  background-color: var(--el-color-primary-light-9);
  border-radius: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
  flex-shrink: 0;
}

.sim-header-icon {
  color: var(--el-color-primary);
  font-size: 16px;
  flex-shrink: 0;
}

/* 主体两栏容器 — 固定高度,绝不被内容撑开 */
.sim-body {
  display: flex;
  height: 520px;
  gap: 0;
  overflow: hidden;
}

/* 分隔线 */
.sim-divider {
  width: 1px;
  background-color: var(--el-border-color-lighter);
  flex-shrink: 0;
  margin: 0 16px;
}

/* 左右两列公共样式 */
.sim-col {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.sim-col-left {
  flex: 14;
  min-width: 0;
}

.sim-col-right {
  flex: 10;
  min-width: 0;
}

/* 面板标题 */
.panel-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  border-left: 3px solid var(--el-color-primary);
  padding-left: 8px;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.panel-sub {
  font-size: 12px;
  font-weight: normal;
  color: var(--el-text-color-secondary);
}

/* 树搜索框 */
.tree-search-wrap {
  margin-bottom: 8px;
  flex-shrink: 0;
}

/* 树滚动容器 */
.tree-scrollbar {
  flex: 1;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  min-height: 0;
}

/* 自定义树节点 */
.sim-node {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  padding: 1px 4px;
  border-radius: 3px;
  transition:
    background-color 0.12s,
    color 0.12s;
  cursor: pointer;
}

.sim-node.selected {
  background-color: var(--el-color-primary);
  color: #fff;
  padding: 1px 6px;
}

.sim-node.selected .sim-node-icon {
  color: #fff;
}

.sim-node.selected .sim-kind-tag {
  opacity: 0.85;
}

.sim-node.visible {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.sim-node.visible .sim-node-icon {
  color: var(--el-color-primary);
}

.sim-node.hidden {
  opacity: 0.4;
}

.sim-node-icon {
  font-size: 14px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.sim-node-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 企业类型标签 — 紧凑型 */
.sim-kind-tag {
  flex-shrink: 0;
  font-size: 10px !important;
  height: 16px !important;
  line-height: 14px !important;
  padding: 0 4px !important;
}

/* "当前用户所在"标记 */
.sim-tag {
  font-size: 10px;
  background-color: var(--el-color-warning-light-5);
  color: var(--el-color-warning-dark-2);
  padding: 0 5px;
  border-radius: 2px;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 16px;
}

/* 右侧滚动容器 */
.control-scrollbar {
  flex: 1;
  min-height: 0;
}

/* 控制面板各区块 */
.control-section {
  margin-bottom: 18px;
  padding-right: 4px;
}

.section-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* ——— RS 等级卡片列表 ——— */
.rs-list {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}

.rs-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 10px;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  transition: background-color 0.12s;
  gap: 8px;
}

.rs-item:last-child {
  border-bottom: none;
}

.rs-item:hover:not(.rs-item-disabled) {
  background-color: var(--el-fill-color-light);
}

.rs-item.active {
  background-color: var(--el-color-primary-light-9);
  border-left: 3px solid var(--el-color-primary);
  padding-left: 7px;
}

.rs-item-disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.rs-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 等级数字徽章 */
.rs-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  font-size: 11px;
  font-weight: bold;
  font-family: monospace;
  color: #fff;
  flex-shrink: 0;
}

.rs-badge-primary {
  background-color: var(--el-color-primary);
}
.rs-badge-success {
  background-color: var(--el-color-success);
}
.rs-badge-info {
  background-color: var(--el-color-info);
}
.rs-badge-warning {
  background-color: var(--el-color-warning);
}
.rs-badge-danger {
  background-color: var(--el-color-danger);
}
.rs-badge-default {
  background-color: var(--el-text-color-disabled);
}

.rs-name {
  font-size: 13px;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}

.rs-desc {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

/* ——— 节点信息卡片 ——— */
.node-info-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.node-info-row {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  font-size: 13px;
}

.node-info-row:last-child {
  border-bottom: none;
}

.node-info-key {
  width: 50px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.node-info-val {
  color: var(--el-text-color-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-id-text {
  font-family: monospace;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.node-hint {
  font-size: 13px;
  color: var(--el-text-color-placeholder);
  padding: 10px 0;
  text-align: center;
}

/* ——— 模拟结果卡片 ——— */
.result-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.result-row {
  display: flex;
  align-items: center;
  padding: 7px 10px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  font-size: 13px;
  gap: 8px;
}

.result-row:last-child {
  border-bottom: none;
}

.result-key {
  width: 50px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.result-val {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.result-all {
  color: var(--el-color-success);
}
.result-none {
  color: var(--el-color-danger);
}
.result-partial {
  color: var(--el-color-primary);
}

/* ——— 树节点样式覆盖 ——— */
:deep(.el-tree-node__content) {
  height: 28px;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-tree-node__expand-icon) {
  font-size: 16px;
}

:deep(.sim-tree) {
  background: transparent;
  padding: 4px 0;
}
</style>
