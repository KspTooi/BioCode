<template>
  <div class="list-layout">
    <splitpanes class="custom-theme">
      <!-- 左侧树形列表：占满整个左侧 -->
      <pane size="20" min-size="10" max-size="40">
        <div class="pt-2 px-1" style="height: 100%; box-sizing: border-box">
          <OrgTree @on-select="onSelectOrg" />
        </div>
      </pane>

      <!-- 右侧内容区 -->
      <pane size="80">
        <StdListContainer>
          <StdListAreaQuery show-persist-tip>
            <el-form :model="listForm" inline class="flex justify-between">
              <div>
                <el-form-item label="用户名">
                  <el-input v-model="listForm.username" placeholder="输入用户名" clearable />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="listForm.nickname" placeholder="输入昵称" clearable />
                </el-form-item>
                <el-form-item v-has-super label="租户">
                  <el-input v-model="listForm.rootName" placeholder="输入租户" clearable />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="listForm.status" placeholder="选择状态" clearable style="width: 180px">
                    <el-option label="正常" :value="0" />
                    <el-option label="封禁" :value="1" />
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item>
                <el-button type="primary" :disabled="listLoading" @click="loadList(orgId)">查询</el-button>
                <el-button :disabled="listLoading" @click="resetList(orgId)">重置</el-button>
              </el-form-item>
            </el-form>
          </StdListAreaQuery>

          <StdListAreaAction class="flex gap-3">
            <el-button v-hasCode="['core:user:add']" type="success" @click="openModal('add', null)">创建用户</el-button>
            <el-dropdown @command="onBatchAction">
              <el-button v-hasCode="['core:user:batch_edit']" type="primary" :disabled="!canBatchAction">
                批量操作<template v-if="canBatchAction">({{ batchCount }})</template>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template v-if="canBatchAction" #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="enable" icon="Check">批量启用</el-dropdown-item>
                  <el-dropdown-item command="disable" icon="Close">批量封禁</el-dropdown-item>
                  <el-dropdown-item command="remove" icon="Delete">批量删除</el-dropdown-item>
                  <el-dropdown-item command="changeDept" icon="ArrowRight">变更部门</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-hasCode="['core:user:import']" type="primary" :icon="UploadIcon" @click="importWizardRef?.openModal()">
              导入用户
            </el-button>
          </StdListAreaAction>

          <StdListAreaTable>
            <el-table
              v-loading="listLoading"
              :data="listData"
              stripe
              border
              height="100%"
              @selection-change="onSelectionChange"
            >
              <el-table-column type="selection" width="40" />
              <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
              <el-table-column prop="username" label="用户名" min-width="150" />
              <el-table-column prop="nickname" label="昵称" min-width="150" />
              <el-table-column label="性别" min-width="100">
                <template #default="scope">
                  <span v-if="scope.row.gender === 0">男</span>
                  <span v-if="scope.row.gender === 1">女</span>
                  <span v-if="scope.row.gender === 2">不愿透露</span>
                </template>
              </el-table-column>
              <el-table-column v-has-super prop="rootName" label="租户" min-width="160" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.rootName">{{ scope.row.rootName }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="orgName" label="企业" min-width="160" :v-show="orgId == null" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.orgName">{{ scope.row.orgName }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="deptName" label="部门" min-width="150">
                <template #default="scope">
                  <span v-if="scope.row.deptName">{{ scope.row.deptName }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" min-width="120" />
              <el-table-column prop="email" label="邮箱" min-width="160" />
              <el-table-column label="状态" min-width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 0 ? "正常" : "封禁" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="180" />
              <el-table-column
                prop="lastLoginTime"
                label="最后登录时间"
                min-width="180"
                :formatter="(_, __, value) => value ?? '-'"
              />
              <el-table-column label="操作" fixed="right" min-width="180">
                <template #default="scope">
                  <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    :disabled="scope.row.isSystem === 1"
                    :icon="DeleteIcon"
                    @click="removeList(scope.row)"
                  >
                    删除
                  </el-button>
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
                @size-change="
                  (val: number) => {
                    listForm.pageSize = val;
                    loadList(orgId);
                  }
                "
                @current-change="
                  (val: number) => {
                    listForm.pageNum = val;
                    loadList(orgId);
                  }
                "
              />
            </template>
          </StdListAreaTable>
        </StdListContainer>
      </pane>
    </splitpanes>

    <!-- 导入向导 -->
    <ImportWizardModal
      ref="importWizardRef"
      url="/user/importUser"
      template-code="core_user"
      @on-success="loadList"
      @on-close="loadList"
    />

    <!-- 部门选择器 (用于批量变更部门等操作) -->
    <CoreOrgDeptSelectModal ref="deptSelectModalRef" v-model="deptSelectVisible" title="选择目标部门" />

    <!-- 用户编辑/创建模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑用户' : '创建用户'"
      width="500px"
      :close-on-click-modal="false"
      @close="
        resetModal();
        loadList(orgId);
      "
    >
      <el-form
        v-if="modalVisible"
        ref="modalFormRef"
        :model="modalForm"
        :rules="modalRules"
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            show-word-limit
            :maxlength="20"
            v-model="modalForm.username"
            :disabled="modalMode === 'edit'"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="modalFormPassword"
            type="password"
            show-word-limit
            :maxlength="128"
            show-password
            :placeholder="modalMode === 'add' ? '请输入密码' : '不修改密码请留空'"
          />
          <div v-if="modalMode === 'edit'" class="form-tip">不修改密码请留空</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input show-word-limit :maxlength="50" v-model="modalForm.nickname" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="所属组织机构" prop="orgId">
          <el-tree-select
            v-model="modalForm.orgId"
            :data="orgTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children', disabled: 'disabled' }"
            placeholder="请选择所属组织机构（可选）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="modalForm.gender">
            <el-radio :label="0">男</el-radio>
            <el-radio :label="1">女</el-radio>
            <el-radio :label="2">不愿透露</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input show-word-limit :maxlength="64" v-model="modalForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input show-word-limit :maxlength="64" v-model="modalForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status" placeholder="请选择状态">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">封禁</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="modalMode === 'edit'" label="系统用户">
          <el-tag v-if="modalForm.isSystem === 1" type="warning">是</el-tag>
          <span v-if="modalForm.isSystem === 0">否</span>
        </el-form-item>
        <el-form-item label="所属用户组" prop="groupIds">
          <el-select
            v-model="selectedGroupIds"
            :disabled="modalMode === 'edit' && modalForm.isSystem === 1"
            multiple
            placeholder="请选择用户组"
            style="width: 100%"
          >
            <el-option v-for="group in groupOptions" :key="group.id" :label="group.name" :value="group.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button v-hasCode="['core:user:edit']" type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from "vue";
import { Edit, Delete, Upload } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import UserManagerService from "@/views/core/service/UserManagerService.ts";
import OrgTree from "@/views/core/components/OrgTree.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";
import ImportWizardModal from "@/soa/com-series/ImportWizardModal.vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import CoreOrgDeptSelectModal from "@/views/core/components/public/CoreOrgDeptSelectModal.vue";
import UserAuthService from "@/views/auth/service/UserAuthService";

//按钮级权限打包
const { vHasCode, vHasSuper } = UserAuthService.usePreAuthorize();

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const UploadIcon = markRaw(Upload);

const importWizardRef = ref<InstanceType<typeof ImportWizardModal>>();

/**
 * 选择组织
 * @param org 组织
 */
const onSelectOrg = (org: GetOrgTreeVo | null): void => {
  loadList(org?.id ?? null);
  orgId.value = org?.id ?? null;
};

const orgId = ref<string | null>(null);

// 列表打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = UserManagerService.useUserList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

const _loadList = (): void => {
  loadList(orgId.value);
};

// 模态框打包
const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalFormPassword,
  selectedGroupIds,
  groupOptions,
  modalRules,
  openModal,
  resetModal,
  submitModal,
  orgTreeOptions,
} = UserManagerService.useUserModal(modalFormRef, _loadList, orgId);

// 部门选择器逻辑
const deptSelectModalRef = ref<InstanceType<typeof CoreOrgDeptSelectModal>>();
const deptSelectVisible = ref(false);

// 批量操作打包
const { onBatchAction, onSelectionChange, canBatchAction, batchCount } = UserManagerService.useBatchAction(
  _loadList,
  deptSelectModalRef
);
</script>

<style scoped>
.list-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
}

/* 自定义无边框主题 */
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

.form-tip {
  font-size: 12px;
  color: var(--el-color-info);
  margin-top: 5px;
}
</style>
