<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <!-- 复选框演示 -->
      <el-card header="复选框功能演示" style="margin-bottom: 20px">
        <div class="demo-body">
          <div class="demo-tree-panel">
            <div class="panel-header">
              <span class="panel-title">部门树</span>
              <el-switch v-model="checkEnabled" active-text="复选框" size="small" />
            </div>

            <div class="panel-controls">
              <el-checkbox v-model="cascadeEnabled" size="small" :disabled="!checkEnabled">级联选择</el-checkbox>
              <el-checkbox v-model="clickCheckEnabled" size="small" :disabled="!checkEnabled">点击节点选中</el-checkbox>
              <el-checkbox v-model="checkStrictlySingle" size="small" :disabled="!checkEnabled">单选模式</el-checkbox>
              <el-checkbox v-model="searchCascadeEnabled" size="small">搜索级联</el-checkbox>
              <el-checkbox v-model="expandOnClickEnabled" size="small" :disabled="clickCheckEnabled">点击节点展开</el-checkbox>
              <div class="control-btns">
                <el-button size="small" :disabled="!checkEnabled" @click="onCheckAll">全选</el-button>
                <el-button size="small" :disabled="!checkEnabled" @click="onCheckClear">清空</el-button>
              </div>
            </div>

            <div class="tree-area">
              <StdAdvTree
                ref="demoTreeRef"
                v-model:checked-nks="checkedKeys"
                v-model:checked-half-nks="halfCheckedKeys"
                :data="treeData"
                :check="checkEnabled"
                :check-cascade="cascadeEnabled"
                :check-on-node-click="clickCheckEnabled"
                :check-multiple="!checkStrictlySingle"
                :search-cascade="searchCascadeEnabled"
                :expand-on-click="expandOnClickEnabled"
                search
                search-placeholder="搜索部门"
                ni="icon"
              />
            </div>
          </div>

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
              <div class="info-label">当前设置</div>
              <div class="info-settings">
                <div class="setting-row">
                  <span>复选框:</span>
                  <el-tag :type="checkEnabled ? 'success' : 'info'" size="small">{{ checkEnabled ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>级联:</span>
                  <el-tag :type="cascadeEnabled ? 'success' : 'info'" size="small">{{ cascadeEnabled ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>点击选中:</span>
                  <el-tag :type="clickCheckEnabled ? 'success' : 'info'" size="small">{{ clickCheckEnabled ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>单选:</span>
                  <el-tag :type="checkStrictlySingle ? 'warning' : 'info'" size="small">{{
                    checkStrictlySingle ? "开" : "关"
                  }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>搜索级联:</span>
                  <el-tag :type="searchCascadeEnabled ? 'success' : 'info'" size="small">{{
                    searchCascadeEnabled ? "开" : "关"
                  }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>点击展开:</span>
                  <el-tag :type="expandOnClickEnabled ? 'success' : 'info'" size="small">{{
                    expandOnClickEnabled ? "开" : "关"
                  }}</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      <!-- 节点操作按钮演示 -->
      <el-card header="节点操作按钮演示" style="margin-bottom: 20px">
        <div class="demo-body">
          <div class="demo-tree-panel">
            <div class="panel-header">
              <span class="panel-title">部门树</span>
              <el-switch v-model="actionEnabled" active-text="操作按钮" size="small" />
            </div>

            <div class="panel-controls">
              <el-checkbox v-model="actionAlwaysShow" size="small" :disabled="!actionEnabled">始终显示</el-checkbox>
              <el-checkbox v-model="actionShowAdd" size="small" :disabled="!actionEnabled">新增</el-checkbox>
              <el-checkbox v-model="actionShowEdit" size="small" :disabled="!actionEnabled">编辑</el-checkbox>
              <el-checkbox v-model="actionShowRemove" size="small" :disabled="!actionEnabled">删除</el-checkbox>
            </div>

            <div class="tree-area">
              <StdAdvTree
                :data="treeData"
                :action="actionEnabled"
                :action-always-show="actionAlwaysShow"
                :action-mode="actionMode"
                ni="icon"
                @on-add="onActionAdd"
                @on-edit="onActionEdit"
                @on-remove="onActionRemove"
              />
            </div>
          </div>

          <div class="demo-info-panel">
            <div class="panel-header">
              <span class="panel-title">操作日志</span>
              <el-button size="small" text type="primary" @click="actionLogs = []">清空</el-button>
            </div>

            <div class="info-block">
              <div class="info-label">当前设置</div>
              <div class="info-settings">
                <div class="setting-row">
                  <span>action:</span>
                  <el-tag :type="actionEnabled ? 'success' : 'info'" size="small">{{ actionEnabled ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>actionAlwaysShow:</span>
                  <el-tag :type="actionAlwaysShow ? 'success' : 'info'" size="small">{{ actionAlwaysShow ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>actionMode:</span>
                  <code>{{ actionMode.join(", ") || "无" }}</code>
                </div>
              </div>
            </div>

            <div class="info-block">
              <div class="info-label">操作事件日志</div>
              <div class="info-keys">
                <el-tag v-for="(log, i) in actionLogs" :key="i" size="small" type="primary" class="key-tag">
                  {{ log }}
                </el-tag>
                <span v-if="actionLogs.length === 0" class="empty-hint">悬停节点行或开启「始终显示」后点击操作按钮</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      <!-- 自定义根节点插槽 -->
      <el-card header="自定义根节点插槽" style="margin-bottom: 20px">
        <div class="demo-body">
          <div class="demo-tree-panel">
            <div class="panel-header">
              <span class="panel-title">部门树</span>
              <el-switch v-model="nrEnabled" active-text="虚拟根节点" size="small" />
            </div>

            <div class="panel-controls">
              <span style="font-size: 12px; color: var(--el-text-color-secondary)">标题:</span>
              <el-input v-model="nrTitle" size="small" style="width: 120px" :disabled="!nrEnabled" />
              <span style="font-size: 12px; color: var(--el-text-color-secondary)">值:</span>
              <el-input v-model="nrValue" size="small" style="width: 120px" :disabled="!nrEnabled" />
            </div>

            <div class="tree-area">
              <StdAdvTree
                ref="nrTreeRef"
                v-model="nrSelectedNk"
                :data="nrTreeData"
                :nr="nrEnabled"
                :nr-title="nrTitle"
                :nr-value="nrValue"
                @on-root-select="onRootSelect"
                @on-select="onNrNodeSelect"
              >
                <template #root-actions>
                  <el-button type="primary" size="small" :icon="PlusIcon" circle @click="onAddRootNode" />
                </template>
              </StdAdvTree>
            </div>
          </div>

          <div class="demo-info-panel">
            <div class="panel-header">
              <span class="panel-title">选中信息</span>
              <el-button size="small" text type="primary" @click="nrSelectedNk = null">清空选中</el-button>
            </div>

            <div class="info-block">
              <div class="info-label">当前选中节点</div>
              <div class="info-value" style="font-size: 16px; word-break: break-all">{{ nrSelectedNk || "无" }}</div>
            </div>

            <div class="info-block">
              <div class="info-label">根节点点击日志</div>
              <div class="info-keys">
                <el-tag v-for="(log, i) in nrClickLogs" :key="i" size="small" type="warning" class="key-tag">
                  {{ log }}
                </el-tag>
                <span v-if="nrClickLogs.length === 0" class="empty-hint">无</span>
              </div>
            </div>

            <div class="info-block">
              <div class="info-label">当前设置</div>
              <div class="info-settings">
                <div class="setting-row">
                  <span>nr:</span>
                  <el-tag :type="nrEnabled ? 'success' : 'info'" size="small">{{ nrEnabled ? "开" : "关" }}</el-tag>
                </div>
                <div class="setting-row">
                  <span>nrTitle:</span>
                  <code>{{ nrTitle }}</code>
                </div>
                <div class="setting-row">
                  <span>nrValue:</span>
                  <code>{{ nrValue }}</code>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="280" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="180" />
        <el-table-column prop="desc" label="说明" min-width="220" />
      </el-table>
    </template>

    <template #emits>
      <el-table :data="emitsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="事件名" width="200" />
        <el-table-column prop="payload" label="参数" width="250" />
        <el-table-column prop="desc" label="说明" min-width="220" />
      </el-table>

      <el-divider content-position="left">v-model 双向绑定</el-divider>

      <el-table :data="vModelTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="绑定名" width="260" />
        <el-table-column prop="type" label="类型" width="240" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">暴露方法（defineExpose）</el-divider>

      <el-table :data="exposeTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="方法名" width="180" />
        <el-table-column prop="params" label="参数" width="200" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">插槽</el-divider>

      <el-table :data="slotsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="插槽名" width="180" />
        <el-table-column prop="params" label="参数" width="280" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { Plus as PlusIcon } from "@element-plus/icons-vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

const demoTreeRef = ref<InstanceType<typeof StdAdvTree>>();

const checkEnabled = ref(true);
const cascadeEnabled = ref(true);
const clickCheckEnabled = ref(false);
const checkStrictlySingle = ref(false);
const searchCascadeEnabled = ref(false);
const expandOnClickEnabled = ref(false);

const checkedKeys = ref<(string | number)[]>(["102", "103"]);
const halfCheckedKeys = ref<(string | number)[]>([]);

watch(checkStrictlySingle, () => {
  clearChecked();
});

const clearChecked = (): void => {
  demoTreeRef.value?.checkClear();
};

const onCheckAll = (): void => {
  demoTreeRef.value?.checkAll();
};

const onCheckClear = (): void => {
  demoTreeRef.value?.checkClear();
};

const actionEnabled = ref(true);
const actionAlwaysShow = ref(false);
const actionShowAdd = ref(true);
const actionShowEdit = ref(true);
const actionShowRemove = ref(true);
const actionLogs = ref<string[]>([]);

const actionMode = computed((): Array<"add" | "edit" | "remove"> => {
  const modes: Array<"add" | "edit" | "remove"> = [];
  if (actionShowAdd.value) {
    modes.push("add");
  }
  if (actionShowEdit.value) {
    modes.push("edit");
  }
  if (actionShowRemove.value) {
    modes.push("remove");
  }
  return modes;
});

const pushActionLog = (msg: string): void => {
  actionLogs.value.unshift(msg);
  if (actionLogs.value.length > 10) {
    actionLogs.value.pop();
  }
};

const onActionAdd = (node: any): void => {
  pushActionLog(`on-add: ${node.id} ${node.name}`);
};

const onActionEdit = (node: any): void => {
  pushActionLog(`on-edit: ${node.id} ${node.name}`);
};

const onActionRemove = (node: any): void => {
  pushActionLog(`on-remove: ${node.id} ${node.name}`);
};

const nrTreeRef = ref<InstanceType<typeof StdAdvTree>>();
const nrEnabled = ref(false);
const nrTitle = ref("全部");
const nrValue = ref("-1");
const nrSelectedNk = ref<string | number | null>(null);
const nrClickLogs = ref<string[]>([]);

const onRootSelect = (value: string): void => {
  nrClickLogs.value.unshift(`根节点选中: ${value}`);
  if (nrClickLogs.value.length > 10) {
    nrClickLogs.value.pop();
  }
};

const onNrNodeSelect = (node: any): void => {
  nrClickLogs.value.unshift(`节点选中: ${node.id}`);
  if (nrClickLogs.value.length > 10) {
    nrClickLogs.value.pop();
  }
};

const onAddRootNode = (): void => {
  nrClickLogs.value.unshift(`根节点 + 按钮点击`);
  if (nrClickLogs.value.length > 10) {
    nrClickLogs.value.pop();
  }
};

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

const nrTreeData = [
  {
    id: "1",
    name: "总公司",
    children: [
      {
        id: "101",
        name: "人事部",
        children: [
          { id: "10101", name: "招聘组" },
          { id: "10102", name: "薪酬组" },
        ],
      },
      {
        id: "102",
        name: "技术部",
        children: [
          { id: "10201", name: "前端组" },
          { id: "10202", name: "后端组" },
          { id: "10203", name: "测试组" },
        ],
      },
      { id: "103", name: "产品部" },
      { id: "104", name: "财务部" },
    ],
  },
];

const propsTableData = [
  { name: "data", type: "Array<any>", required: "是", default: "[]", desc: "树形数据源" },
  { name: "check", type: "boolean", required: "否", default: "false", desc: "是否可选择节点" },
  { name: "checkMultiple", type: "boolean", required: "否", default: "true", desc: "是否支持多选" },
  { name: "checkCascade", type: "boolean", required: "否", default: "true", desc: "级联选择，子节点选中向上级联父节点" },
  { name: "checkOnNodeClick", type: "boolean", required: "否", default: "true", desc: "点击节点行是否选中（与 expandOnClick 互斥，优先）" },
  { name: "checkDisableNks", type: "(string | number)[]", required: "否", default: "[]", desc: "禁用不可选的节点键数组" },
  { name: "checkEnableMethod", type: "(node: any) => boolean", required: "否", default: "undefined", desc: "自定义启用方法，返回 false 则禁用该节点" },
  { name: "search", type: "boolean", required: "否", default: "false", desc: "是否显示搜索框" },
  { name: "searchPlaceholder", type: "string", required: "否", default: '"搜索"', desc: "搜索框占位文本" },
  { name: "searchFields", type: "string[]", required: "否", default: '["name"]', desc: "搜索匹配的字段名" },
  { name: "searchCascade", type: "boolean", required: "否", default: "false", desc: "搜索时保留子节点可见" },
  { name: "searchRefresh", type: "boolean", required: "否", default: "false", desc: "搜索栏旁显示刷新按钮" },
  { name: "filterMethod", type: "(value: string, data: any, node?: any) => boolean", required: "否", default: "undefined", desc: "自定义节点过滤方法，与搜索过滤 AND 组合" },
  { name: "nr", type: "boolean", required: "否", default: "false", desc: "是否显示虚拟根节点行" },
  { name: "nrTitle", type: "string", required: "否", default: '"全部"', desc: "根节点显示文本" },
  { name: "nrIcon", type: "string", required: "否", default: "null", desc: "根节点图标" },
  { name: "nrValue", type: "string", required: "否", default: '"-1"', desc: "根节点代表的值" },
  { name: "ni", type: "string", required: "否", default: "undefined", desc: "节点数据中图标字段名" },
  { name: "nk", type: "string", required: "否", default: '"id"', desc: "节点数据中唯一键字段名" },
  { name: "nt", type: "string", required: "否", default: '"name"', desc: "节点数据显示标签字段名" },
  { name: "nc", type: "string", required: "否", default: '"children"', desc: "节点数据中子节点数组字段名" },
  { name: "loading", type: "boolean", required: "否", default: "false", desc: "是否显示加载中" },
  { name: "expandOnDefault", type: "boolean", required: "否", default: "true", desc: "是否默认展开所有节点" },
  { name: "expandOnClick", type: "boolean", required: "否", default: "false", desc: "点击节点文本是否展开/折叠（checkOnNodeClick 优先）" },
  { name: "action", type: "boolean", required: "否", default: "false", desc: "节点操作按钮总开关" },
  { name: "actionMode", type: 'Array<"add" | "edit" | "remove">', required: "否", default: '["add", "edit", "remove"]', desc: "显示哪些操作按钮" },
  { name: "actionAlwaysShow", type: "boolean", required: "否", default: "false", desc: "操作按钮始终显示，不依赖悬停或选中节点" },
];

const emitsTableData = [
  { name: "on-select", payload: "(node: any)", desc: "点击选择树节点时触发" },
  { name: "on-root-select", payload: "(value: string)", desc: "点击虚拟根节点时触发" },
  { name: "on-add", payload: "(node: any)", desc: "点击节点添加操作按钮时触发" },
  { name: "on-edit", payload: "(node: any)", desc: "点击节点编辑操作按钮时触发" },
  { name: "on-remove", payload: "(node: any)", desc: "点击节点删除操作按钮时触发" },
  { name: "on-search", payload: "(value: string)", desc: "搜索文本变化时触发（300ms 防抖）" },
  { name: "on-refresh", payload: "(value: string)", desc: "点击搜索栏刷新按钮时触发" },
];

const vModelTableData = [
  { name: "v-model", type: "string | number | null", desc: "当前选中节点键（双向绑定）" },
  { name: "v-model:checked-nks", type: "(string | number)[]", desc: "当前已选复选框节点键数组（双向绑定）" },
  { name: "v-model:checked-half-nks", type: "(string | number)[]", desc: "当前半选复选框节点键数组（双向绑定）" },
];

const exposeTableData = [
  { name: "reset", params: "—", desc: "重置树状态" },
  { name: "filter", params: "—", desc: "触发过滤" },
  { name: "checkAll", params: "—", desc: "全选所有节点" },
  { name: "checkClear", params: "—", desc: "清空所有选中" },
  { name: "treeRef", params: "—", desc: "底层 Element Plus Tree 组件引用" },
];

const slotsTableData = [
  { name: "root-actions", params: "—", desc: "虚拟根节点操作区插槽，可放置自定义按钮" },
  { name: "icon", params: "{ node, data }", desc: "自定义节点图标" },
  { name: "label", params: "{ node, data }", desc: "自定义节点标签文本" },
  { name: "append", params: "{ node, data }", desc: "节点标签后追加内容" },
  { name: "actions", params: "{ node, data }", desc: "节点操作按钮区，需配合 action prop 启用" },
];
</script>

<style scoped>
.demo-body {
  display: flex;
  gap: 16px;
  min-height: 400px;
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
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.control-btns {
  margin-left: auto;
  display: flex;
  gap: 6px;
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
