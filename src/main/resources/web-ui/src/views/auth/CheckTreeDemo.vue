<template>
  <div class="check-tree-demo">
    <div class="demo-header">
      <h2>StdAdvTree 复选框功能演示</h2>
    </div>

    <div class="demo-body">
      <!-- 左侧树 -->
      <div class="demo-tree-panel">
        <div class="panel-header">
          <span class="panel-title">部门树</span>
          <el-switch v-model="checkEnabled" active-text="复选框" size="small" />
        </div>

        <div class="panel-controls">
          <el-checkbox v-model="cascadeEnabled" size="small" :disabled="!checkEnabled">级联选择</el-checkbox>
          <el-checkbox v-model="clickCheckEnabled" size="small" :disabled="!checkEnabled">点击节点选中</el-checkbox>
          <el-checkbox v-model="checkStrictlySingle" size="small" :disabled="!checkEnabled">单选模式</el-checkbox>
        </div>

        <div class="tree-area">
          <StdAdvTree
            ref="demoTreeRef"
            :data="treeData"
            :check="checkEnabled"
            :check-cascade="cascadeEnabled"
            :check-on-node-click="clickCheckEnabled"
            :check-multiple="!checkStrictlySingle"
            :init-value-check="initCheckedKeys"
            search
            search-placeholder="搜索部门"
            ni="icon"
            @update:model-value-check="onCheckedKeysChange"
          />
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="demo-info-panel">
        <div class="panel-header">
          <span class="panel-title">选中信息</span>
          <el-button size="small" text type="primary" @click="clearChecked">清空选中</el-button>
        </div>

        <div class="info-block">
          <div class="info-label">已选节点数</div>
          <div class="info-value">{{ checkedKeys.length }}</div>
        </div>

        <div class="info-block">
          <div class="info-label">半选节点数</div>
          <div class="info-value">{{ halfCheckedKeys.length }}</div>
        </div>

        <div class="info-block">
          <div class="info-label">已选节点 Keys</div>
          <div class="info-keys">
            <el-tag v-for="key in checkedKeys" :key="key" size="small" type="primary" class="key-tag">
              {{ key }}
            </el-tag>
            <span v-if="checkedKeys.length === 0" class="empty-hint">无</span>
          </div>
        </div>

        <div class="info-block">
          <div class="info-label">半选节点 Keys</div>
          <div class="info-keys">
            <el-tag v-for="key in halfCheckedKeys" :key="key" size="small" type="warning" class="key-tag">
              {{ key }}
            </el-tag>
            <span v-if="halfCheckedKeys.length === 0" class="empty-hint">无</span>
          </div>
        </div>

        <div class="info-block">
          <div class="info-label">已选节点详情</div>
          <div class="info-detail-list">
            <div v-for="node in checkedNodes" :key="node.id" class="info-detail-row">
              {{ node.name }}
            </div>
            <span v-if="checkedNodes.length === 0" class="empty-hint">无</span>
          </div>
        </div>

        <div class="info-block">
          <div class="info-label">当前设置</div>
          <div class="info-settings">
            <div class="setting-row">
              <span>复选框:</span>
              <el-tag :type="checkEnabled ? 'success' : 'info'" size="small">{{ checkEnabled ? '开' : '关' }}</el-tag>
            </div>
            <div class="setting-row">
              <span>级联:</span>
              <el-tag :type="cascadeEnabled ? 'success' : 'info'" size="small">{{ cascadeEnabled ? '开' : '关' }}</el-tag>
            </div>
            <div class="setting-row">
              <span>点击选中:</span>
              <el-tag :type="clickCheckEnabled ? 'success' : 'info'" size="small">{{ clickCheckEnabled ? '开' : '关' }}</el-tag>
            </div>
            <div class="setting-row">
              <span>单选:</span>
              <el-tag :type="checkStrictlySingle ? 'warning' : 'info'" size="small">{{ checkStrictlySingle ? '开' : '关' }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

const demoTreeRef = ref<InstanceType<typeof StdAdvTree>>();

const checkEnabled = ref(true);
const cascadeEnabled = ref(true);
const clickCheckEnabled = ref(false);
const checkStrictlySingle = ref(false);

const checkedKeys = ref<string[]>([]);
const halfCheckedKeys = ref<string[]>([]);
const checkedNodes = ref<any[]>([]);

const onCheckedKeysChange = (keys: string[] | number[]): void => {
  checkedKeys.value = keys as string[];
  const treeRef = demoTreeRef.value?.getTreeRef();
  if (treeRef) {
    halfCheckedKeys.value = treeRef.getHalfCheckedKeys() as string[];
    checkedNodes.value = treeRef.getCheckedNodes();
  }
};

const clearChecked = (): void => {
  const treeRef = demoTreeRef.value?.getTreeRef();
  if (treeRef) {
    treeRef.setCheckedKeys([]);
    checkedKeys.value = [];
    halfCheckedKeys.value = [];
    checkedNodes.value = [];
  }
};

// 初始选中 "技术部" 和 "产品部"
const initCheckedKeys = ref(["102", "103"]);

const treeData = [
  {
    id: "1",
    name: "总公司",
    icon: "ep:office-building",
    children: [
      {
        id: "101",
        name: "人事部",
        icon: "ep:user-filled",
        children: [
          { id: "10101", name: "招聘组", icon: "ep:user" },
          { id: "10102", name: "薪酬组", icon: "ep:money" },
        ],
      },
      {
        id: "102",
        name: "技术部",
        icon: "ep:monitor",
        children: [
          { id: "10201", name: "前端组", icon: "ep:platform" },
          { id: "10202", name: "后端组", icon: "ep:cpu" },
          { id: "10203", name: "测试组", icon: "ep:set-up" },
        ],
      },
      {
        id: "103",
        name: "产品部",
        icon: "ep:present",
        children: [
          { id: "10301", name: "产品一组", icon: "ep:guide" },
          { id: "10302", name: "产品二组", icon: "ep:guide" },
        ],
      },
      {
        id: "104",
        name: "财务部",
        icon: "ep:coin",
        children: [
          { id: "10401", name: "会计组", icon: "ep:collection" },
          { id: "10402", name: "审计组", icon: "ep:search" },
        ],
      },
    ],
  },
];

// 切换单选模式时清空已选
watch(checkStrictlySingle, () => {
  clearChecked();
});
</script>

<style scoped>
.check-tree-demo {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: var(--el-bg-color);
  box-sizing: border-box;
}

.demo-header {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.demo-header h2 {
  margin: 0;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.demo-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.demo-tree-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}

.demo-info-panel {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}

.panel-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.panel-controls {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.tree-area {
  flex: 1;
  min-height: 0;
}

.info-block {
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.info-block:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.info-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.info-keys {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.key-tag {
  margin: 0;
}

.empty-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.info-detail-list {
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-detail-row {
  font-size: 12px;
  color: var(--el-text-color-regular);
  padding: 4px 8px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
}

.info-settings {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-regular);
}
</style>
