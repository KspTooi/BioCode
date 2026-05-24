<!--
  * 用户选择模态框
  * 所有属性都会透传给el-dialog组件 具体参考el-dialog组件的属性 @see DialogProps
-->
<template>
  <el-dialog
    v-model="modalVisible"
    :title="props.title"
    :width="props.width"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="core-user-select-modal"
    v-bind="$attrs"
  >
    <div v-loading="listLoading" class="modal-body">
      <splitpanes class="custom-theme">
        <pane size="20" min-size="10" max-size="40">
          <div style="height: 100%; box-sizing: border-box">
            <OrgTree
              v-model="draftCheckedOrgId"
              :crop-org-id="props.cropOrgId"
              :nr="true"
              :nr-value="null"
              nr-title="全部组织机构"
              nr-icon="ep:office-building"
              :search="true"
              search-placeholder="请输入组织机构"
              :search-cascade="true"
              :show-header="true"
              @on-select="loadList()"
              @on-root-select="
                draftCheckedOrgId = null;
                loadList();
              "
            />
          </div>
        </pane>

        <pane size="80">
          <div class="right-content">
            <StdListAreaQuery>
              <el-form :model="listForm" size="small">
                <el-row class="gap-4">
                  <el-form-item label="登录账号">
                    <el-input v-model="listForm.username" placeholder="请输入登录账号" clearable />
                  </el-form-item>
                  <el-form-item label="用户姓名">
                    <el-input v-model="listForm.nickname" placeholder="请输入用户姓名" clearable />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input v-model="listForm.phone" placeholder="请输入手机号" clearable />
                  </el-form-item>

                  <el-form-item style="margin-left: auto">
                    <el-button type="primary" :disabled="listLoading" @click="loadList()">查询</el-button>
                    <el-button :disabled="listLoading" @click="resetList">重置</el-button>
                  </el-form-item>
                </el-row>
              </el-form>
            </StdListAreaQuery>

            <StdListAreaTable>
              <el-table
                ref="tableRef"
                :data="listData"
                stripe
                border
                style="cursor: pointer"
                row-key="id"
                height="100%"
                @row-click="(row: GetUserListVo) => stccRef?.onElRowCheck(row)"
              >
                <StdTableCheckColumn
                  ref="stccRef"
                  v-model="draftCheckUids"
                  :data="listData"
                  :mode="props.mode"
                  width="40"
                  :readonly="props.readonly"
                />
                <el-table-column prop="username" label="登录账号" min-width="120" show-overflow-tooltip />
                <el-table-column prop="nickname" label="用户姓名" min-width="120" show-overflow-tooltip />
                <el-table-column label="性别" min-width="80" show-overflow-tooltip>
                  <template #default="scope">
                    <span v-if="scope.row.gender === 0">男</span>
                    <span v-if="scope.row.gender === 1">女</span>
                    <span v-if="scope.row.gender === 2">不愿透露</span>
                  </template>
                </el-table-column>
                <el-table-column prop="orgName" label="企业" min-width="120" show-overflow-tooltip>
                  <template #default="scope">
                    <span v-if="scope.row.orgName">{{ scope.row.orgName }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="deptName" label="部门" min-width="150" show-overflow-tooltip>
                  <template #default="scope">
                    <span v-if="scope.row.deptName">{{ scope.row.deptName }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="phone" label="手机号" min-width="120" show-overflow-tooltip />
                <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
                <el-table-column label="状态" min-width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                      {{ scope.row.status === 1 ? "正常" : "封禁" }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <template #pagination>
                <el-pagination
                  v-model:current-page="listForm.pageNum"
                  v-model:page-size="listForm.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="listTotal"
                  background
                  size="small"
                  @size-change="loadList()"
                  @current-change="loadList()"
                />
              </template>
            </StdListAreaTable>
          </div>
        </pane>
      </splitpanes>
    </div>

    <template #footer>
      <div v-if="!props.readonly" class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="draftCheckUids.length < 1 || isOverMax" @click="onModalSubmit"
          >保存({{ draftCheckUids.length }})</el-button
        >
      </div>
      <div v-else class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { useTemplateRef } from "vue";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import OrgTree from "@/views/core/public/OrgTree.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ModalUserSelectorService, {
  type ModalUserSelectorEmits,
  type ModalUserSelectorProps,
} from "@/views/core/public/service/ModalUserSelectorService.ts";
import type { GetUserListVo } from "@/views/core/api/UserApi";
import StdTableCheckColumn from "@/soa/std-series/StdTableCheckColumn.vue";

const props = withDefaults(defineProps<ModalUserSelectorProps>(), {
  title: "选择用户",
  width: "80%",
  mode: "multiple",
  readonly: false,
  max: null,
});

const emit = defineEmits<ModalUserSelectorEmits>();

const stccRef = useTemplateRef<InstanceType<typeof StdTableCheckColumn>>("stccRef");

//弹窗显隐控制 外部用v-model绑定
const modalVisible = defineModel<boolean>({ default: false });

//当前选中组织ID 外部用v-model:current-org-id绑定
const bindCheckedOrgId = defineModel<string | null>("currentOrgId", { default: null });

//当前已勾选的用户IDS 外部用v-model:checked-user-ids绑定
const bindCheckedUids = defineModel<string[]>("checkedUserIds", { default: () => [] });

//用户选择模态框打包
const { listForm, listData, listTotal, listLoading, draftCheckUids, isOverMax, draftCheckedOrgId, loadList, resetList, onModalSubmit } =
  ModalUserSelectorService.useUserSelect(props, emit, modalVisible, bindCheckedOrgId, bindCheckedUids);
</script>

<style scoped>
.core-user-select-modal :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.modal-body {
  height: 600px;
  display: flex;
  flex-direction: column;
  border-radius: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.right-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 10px;
  box-sizing: border-box;
  overflow: hidden;
}

.dialog-footer {
  padding-top: 10px;
}

/* 直角风格适配 */
:deep(.el-dialog) {
  border-radius: 0;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

:deep(.splitpanes.custom-theme) {
  border: none;
}

:deep(.splitpanes.custom-theme .splitpanes__pane) {
  background-color: transparent;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter) {
  background-color: var(--el-border-color-extra-light);
  width: 1px;
  border: none;
  cursor: col-resize;
  position: relative;
  transition: background-color 0.2s;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter:hover) {
  background-color: var(--el-color-primary);
  width: 3px;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter:after) {
  content: "";
  position: absolute;
  left: -5px;
  right: -5px;
  top: 0;
  bottom: 0;
  z-index: 1;
}

:deep(.splitpanes__pane) {
  transition: none !important;
}
</style>

<style>
.core-user-select-modal .el-dialog__footer {
  border-top: none !important;
  padding-top: 0 !important;
}

.core-user-select-modal .modal-body {
  padding: 0 !important;
}

.core-user-select-modal.el-dialog {
  min-width: 1150px !important;
}
</style>
