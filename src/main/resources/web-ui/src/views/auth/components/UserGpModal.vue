<template>
  <el-dialog
    :model-value="props.visible"
    title="管理权限"
    width="1000px"
    :close-on-click-modal="false"
    class="modal-centered"
    @close="resetModal"
  >
    <div class="modal-content">
      <el-tabs v-model="tabState.tab.value">
        <el-tab-pane :label="'菜单视图(' + menuViewList.menuTotalCount.value + ')'" name="menu">
          <div class="menu-filter-container">
            <div class="filter-item">
              <el-input v-model="menuViewList.menuFilterKeyword.value" placeholder="根据名称或所需权限过滤" clearable>
                <template #prefix>
                  <Icon icon="mdi:magnify" :width="16" :height="16" />
                </template>
              </el-input>
            </div>
            <div class="filter-item">
              <el-select v-model="menuViewList.menuFilterHasPermission.value" placeholder="是否已授权">
                <el-option label="全部" :value="null" />
                <el-option label="已授权" :value="1" />
                <el-option label="未授权" :value="0" />
              </el-select>
            </div>
            <div class="filter-item">
              <el-button type="primary" @click="menuViewList.loadList">查询</el-button>
              <el-button @click="menuViewList.resetList">重置</el-button>
            </div>
          </div>
          <div class="list-table">
            <el-table
              ref="listTableRef"
              v-loading="menuViewList.listLoading.value"
              :data="menuViewList.listData.value"
              border
              row-key="id"
              default-expand-all
              max-height="500"
              height="500"
              :row-class-name="menuViewList.getMenuRowClassName"
              @selection-change="(val: GetGroupPermissionMenuViewVo[]) => (menuViewList.listSelected.value = val)"
              @row-click="menuViewList.onRowClick"
            >
              <el-table-column type="selection" width="40" :selectable="menuViewList.isMenuRowSelectable" />
              <el-table-column label="菜单名称" prop="name" show-overflow-tooltip width="360">
                <template #default="scope">
                  <div style="display: flex; align-items: center; gap: 8px; display: inline">
                    <Icon
                      v-if="scope.row.icon"
                      :icon="scope.row.icon"
                      :width="16"
                      :height="16"
                      style="vertical-align: middle; display: inline"
                    />
                    {{ scope.row.name }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="类型" prop="kind" width="60">
                <template #default="scope">
                  <span v-if="scope.row.kind === 0" style="color: var(--el-color-info)">目录</span>
                  <span v-if="scope.row.kind === 1" style="color: var(--el-color-success)">菜单</span>
                  <span v-if="scope.row.kind === 2" style="color: var(--el-color-primary)">按钮</span>
                </template>
              </el-table-column>
              <el-table-column label="所需权限" prop="permissionCode" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.kind === 0" style="color: #999; font-size: 12px">不适用</span>
                  <span
                    v-else
                    :style="{
                      color:
                        scope.row.missingPermission === 1
                          ? '#f56c6c'
                          : scope.row.missingPermission === 2
                            ? '#e6a23c'
                            : scope.row.missingPermission === 0
                              ? '#67c23a'
                              : '',
                    }"
                  >
                    {{ scope.row.permissionCode }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="状态" prop="hasPermission" width="85">
                <template #default="scope">
                  <span v-if="scope.row.kind === 0" style="color: #999">不适用</span>
                  <span v-else-if="!scope.row.permissionCode" style="color: #999">不适用</span>
                  <span v-else-if="scope.row.hasPermission === 1" style="color: #67c23a">已授权</span>
                  <span v-else-if="scope.row.hasPermission === 0" style="color: #f56c6c">未授权</span>
                  <span v-else-if="scope.row.hasPermission === 2" style="color: #e6a23c">部分授权</span>
                  <span v-else style="color: #999">未知</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <span v-if="scope.row.kind === 0" style="color: #999">不适用</span>
                  <span v-else-if="!scope.row.permissionCode" style="color: #999">不适用</span>
                  <el-button
                    v-else-if="scope.row.hasPermission === 0 || scope.row.hasPermission === 2"
                    type="primary"
                    link
                    @click="grandAndRevoke(scope.row, 0)"
                  >
                    授权
                  </el-button>
                  <el-button v-else-if="scope.row.hasPermission === 1" type="danger" link @click="grandAndRevoke(scope.row, 1)">
                    取消授权
                  </el-button>
                  <span v-else style="color: #999">未知</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane
          :label="nodeViewList.listNodeTotal.value > 0 ? '节点视图(' + nodeViewList.listNodeTotal.value + ')' : '节点视图'"
          name="node"
        >
          <div class="node-filter-container">
            <div class="filter-item">
              <el-input v-model="nodeViewList.listNodeForm.keyword" placeholder="根据名称或权限代码过滤" clearable>
                <template #prefix>
                  <Icon icon="mdi:magnify" :width="16" :height="16" />
                </template>
              </el-input>
            </div>
            <div class="filter-item">
              <el-select v-model="nodeViewList.listNodeForm.hasPermission" placeholder="是否已授权">
                <el-option label="全部" :value="null" />
                <el-option label="已授权" :value="1" />
                <el-option label="未授权" :value="0" />
              </el-select>
            </div>
            <div class="filter-item">
              <el-button type="primary" @click="nodeViewList.loadNodeList">查询</el-button>
              <el-button @click="nodeViewList.resetNodeList">重置</el-button>
            </div>
          </div>
          <div class="list-table">
            <el-table
              ref="listNodeTableRef"
              v-loading="nodeViewList.listNodeLoading.value"
              :data="nodeViewList.listNodeData.value"
              border
              row-key="id"
              max-height="450"
              height="450"
              @selection-change="(val: GetGroupPermissionNodeVo[]) => (nodeViewList.listNodeSelected.value = val)"
              @row-click="nodeViewList.onNodeRowClick"
            >
              <el-table-column type="selection" width="40" />
              <el-table-column
                prop="code"
                label="权限代码"
                min-width="150"
                show-overflow-tooltip
                :show-overflow-tooltip-props="{
                  effect: 'dark',
                  placement: 'top',
                  enterable: false,
                }"
              />
              <el-table-column
                prop="name"
                label="权限名称"
                min-width="150"
                show-overflow-tooltip
                :show-overflow-tooltip-props="{
                  effect: 'dark',
                  placement: 'top',
                  enterable: false,
                }"
              />
              <el-table-column
                prop="remark"
                label="权限描述"
                min-width="200"
                show-overflow-tooltip
                :show-overflow-tooltip-props="{
                  effect: 'dark',
                  placement: 'top',
                  enterable: false,
                }"
              />
              <el-table-column label="状态" prop="hasPermission" width="75">
                <template #default="scope">
                  <span v-if="scope.row.hasPermission === 1" style="color: #67c23a">已授权</span>
                  <span v-if="scope.row.hasPermission === 0" style="color: #f56c6c">未授权</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button v-if="scope.row.hasPermission === 0" type="primary" link @click="grandAndRevoke(scope.row, 0)">
                    授权
                  </el-button>
                  <el-button v-if="scope.row.hasPermission === 1" type="danger" link @click="grandAndRevoke(scope.row, 1)">
                    取消授权
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="nodeViewList.listNodeForm.pageNum"
                v-model:page-size="nodeViewList.listNodeForm.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="nodeViewList.listNodeTotal.value"
                background
                @size-change="
                  (val: number) => {
                    nodeViewList.listNodeForm.pageSize = val;
                    nodeViewList.loadNodeList();
                  }
                "
                @current-change="
                  (val: number) => {
                    nodeViewList.listNodeForm.pageNum = val;
                    nodeViewList.loadNodeList();
                  }
                "
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="batchGrant">批量授权</el-button>
        <el-button type="danger" @click="batchRevoke">批量取消授权</el-button>
        <el-button @click="resetModal">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { GetGroupListVo, GetGroupPermissionMenuViewVo, GetGroupPermissionNodeVo } from "@/views/auth/api/GroupApi.ts";
import { ElMessage, type TableInstance } from "element-plus";
import { ref, watch } from "vue";
import { Icon } from "@iconify/vue";
import UserGpModalService from "@/views/auth/service/UserGpModalService";

const props = defineProps<{
  visible: boolean;
  row?: GetGroupListVo | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
}>();

// 表格引用
const listTableRef = ref<TableInstance>();
const listNodeTableRef = ref<TableInstance>();

// 根据当前tab加载对应的列表
const loadListByTab = async (): Promise<void> => {
  if (tabState.tab.value === "menu") {
    await menuViewList.loadList();
  }
  if (tabState.tab.value === "node") {
    await nodeViewList.loadNodeList();
  }
};

// Tab状态管理
const tabState = UserGpModalService.useGpTab(loadListByTab);

// 菜单视图列表
const menuViewList = UserGpModalService.useGpMenuViewList(props, listTableRef);

// 节点视图列表
const nodeViewList = UserGpModalService.useGpNodeViewList(props, listNodeTableRef);

// 权限操作
const permissionOps = UserGpModalService.useGpPermissionOperations(
  props,
  tabState.tab,
  menuViewList.listSelected,
  nodeViewList.listNodeSelected,
  loadListByTab
);

/**
 * 重置模态框
 */
const resetModal = (): void => {
  menuViewList.clearSelection();
  nodeViewList.clearSelection();
  menuViewList.menuFilterKeyword.value = "";
  menuViewList.menuFilterHasPermission.value = null;
  emit("close");
};

/**
 * 授权或取消授权
 */
const grandAndRevoke = async (row: GetGroupPermissionMenuViewVo | GetGroupPermissionNodeVo, type: number): Promise<void> => {
  await permissionOps.grandAndRevoke(row, type, ElMessage);
};

/**
 * 批量授权
 */
const batchGrant = async (): Promise<void> => {
  await permissionOps.batchGrant(ElMessage);
};

/**
 * 批量取消授权
 */
const batchRevoke = async (): Promise<void> => {
  await permissionOps.batchRevoke(ElMessage);
};

// 监听 visible 变化，加载数据
watch(
  () => props.visible,
  async (newVal) => {
    if (newVal) {
      await loadListByTab();
    }
  }
);

// 监听 tab 变化，加载对应数据
watch(tabState.tab, async () => {
  await loadListByTab();
});

// 监听筛选关键字变化，清空选择
watch(menuViewList.menuFilterKeyword, () => {
  if (tabState.tab.value === "menu" && menuViewList.groupDetails.value) {
    menuViewList.onFilterKeywordChange();
  }
});

// 监听 row 变化，更新节点列表的 groupId
watch(
  () => props.row,
  () => {
    if (props.row) {
      nodeViewList.updateGroupId(props.row.id);
    }
  }
);
</script>

<style scoped>
:deep(.modal-centered) {
  margin: 0 auto;
  top: 50%;
  transform: translateY(-50%);
}

:deep(.el-table__body tr) {
  cursor: pointer;
}

:deep(.el-table__body tr.disabled-row) {
  cursor: not-allowed;
}

.menu-filter-container,
.node-filter-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.menu-filter-container .filter-item,
.node-filter-container .filter-item {
  display: flex;
  align-items: center;
}

.menu-filter-container .filter-item .el-input,
.node-filter-container .filter-item .el-input {
  width: 300px;
}

.menu-filter-container .filter-item .el-select,
.node-filter-container .filter-item .el-select {
  width: 150px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  width: 100%;
}
</style>
