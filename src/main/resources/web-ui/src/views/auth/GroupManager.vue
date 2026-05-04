<template>
  <StdListLayout>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="用户组名称" label-for="query-keyword">
              <el-input id="query-keyword" v-model="listForm.keyword" placeholder="输入用户组名称查询" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <!-- 占位，保持布局一致性 -->
            <el-form-item label="用户组状态" label-for="query-keyword">
              <el-select id="query-status" v-model="listForm.status" placeholder="请选择用户组状态" class="w-full">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <!-- 占位，保持布局一致性 -->
          </el-col>
          <el-col :span="3" :offset="3">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建用户组</el-button>
      <el-button
        type="danger"
        :disabled="listSelected.length === 0"
        :loading="listLoading"
        @click="removeListBatch(listSelected)"
      >
        批量删除
      </el-button>
    </template>

    <template #table>
      <el-table
        v-loading="listLoading"
        :data="listData"
        stripe
        border
        height="100%"
        @selection-change="(val: GetGroupListVo[]) => (listSelected = val)"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="用户组名称" min-width="120" />
        <el-table-column prop="code" label="用户组编码" min-width="120" />
        <el-table-column prop="memberCount" label="成员数量" min-width="100" />
        <el-table-column prop="permissionCount" label="权限数量" min-width="100" />
        <el-table-column prop="rowScope" label="数据权限" min-width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.rowScope === 0" type="primary">全集团</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 10" type="success">本公司+下级公司</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 20" type="info">仅本公司</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 30" type="warning">本部门+下级部门</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 40" type="danger">仅本部门</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 50" type="teal">仅本人</el-tag>
            <el-tag v-else-if="scope.row.rowScope === 60" type="purple">指定组织</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="seq" width="120">
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              :seq-field="'seq'"
              :get-detail-api="getGroupDetailForSeq"
              :edit-api="editGroupSeq"
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openPermissionEditModal(scope.row)">
              管理权限
            </el-button>
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openMenuModal(scope.row)">
              管理菜单
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :icon="DeleteIcon"
              :disabled="scope.row.isSystem === 1"
              @click="removeList(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

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
            loadList();
          }
        "
        @current-change="
          (val: number) => {
            listForm.pageNum = val;
            loadList();
          }
        "
      />
    </template>
  </StdListLayout>

  <UserGpModal
    :visible="modalPermissionEditVisible"
    :row="modalPermissionEditRow"
    @close="modalPermissionEditVisible = false"
  />

  <GroupMenuModal
    :visible="menuModalVisible"
    :data="menuModalRow"
    @close="menuModalVisible = false"
    @success="loadList"
  />

  <CoreOrgDeptSelectModal
    v-model="deptSelectModalVisible"
    multiple
    type="all"
    :default-selected="modalForm.deptIds"
    @confirm="onDeptSelectConfirm"
  />

  <!-- 用户组编辑/创建模态框 -->
  <el-dialog
    v-model="modalVisible"
    :title="modalMode === 'edit' ? '编辑用户组' : '创建用户组'"
    width="900px"
    :close-on-click-modal="false"
    @close="
      (async () => {
        await resetModal();
        loadList();
      })()
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
      <el-row :gutter="20">
        <!-- 左侧基础信息 -->
        <el-col :span="12">
          <div class="p-2.5">
            <div class="section-title text-sm font-bold mb-4 pl-2.5">基础信息</div>
            <el-form-item label="用户组编码" prop="code" label-for="group-code">
              <el-input
                id="group-code"
                show-word-limit
                :maxlength="32"
                v-model="modalForm.code"
                :disabled="modalMode === 'edit' && isSystemGroup"
                :placeholder="modalMode === 'edit' && isSystemGroup ? '系统用户组不可修改编码' : '请输入组编码'"
              />
            </el-form-item>
            <el-form-item label="用户组名称" prop="name" label-for="group-name">
              <el-input id="group-name" show-word-limit :maxlength="80" v-model="modalForm.name" placeholder="请输入组名称" />
            </el-form-item>
            <el-form-item label="排序号" prop="seq">
              <el-input-number v-model="modalForm.seq" :min="0" :max="655350" class="w-full" />
            </el-form-item>
            <el-form-item label="用户组状态" prop="status" label-for="group-status">
              <el-radio-group id="group-status" v-model="modalForm.status">
                <el-radio :value="1" :disabled="modalMode === 'edit' && isSystemGroup">启用</el-radio>
                <el-radio :value="0" :disabled="modalMode === 'edit' && isSystemGroup">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="用户组描述" prop="remark" label-for="group-remark">
              <el-input
                show-word-limit
                :maxlength="200"
                id="group-remark"
                v-model="modalForm.remark"
                type="textarea"
                :rows="4"
                placeholder="请输入描述"
              />
            </el-form-item>

            <div class="section-title text-sm font-bold mb-4 pl-2.5 mt-5">数据权限</div>
            <el-form-item label="权限范围" prop="rowScope" label-for="group-rowScope">
              <div class="flex items-center justify-between w-full">
                <el-select
                  id="group-rowScope"
                  v-model="modalForm.rowScope"
                  :disabled="modalMode === 'edit' && isSystemGroup"
                  placeholder="请选择数据权限"
                  class="w-full"
                  style="width: 180px"
                >
                  <el-option :value="0" label="全集团" />
                  <el-option :value="10" label="本公司+下级公司" />
                  <el-option :value="20" label="仅本公司" />
                  <el-option :value="30" label="本部门+下级部门" />
                  <el-option :value="40" label="仅本部门" />
                  <el-option :value="50" label="仅本人" />
                  <el-option :value="60" label="指定组织" />
                </el-select>
                <el-button type="primary" size="small" style="margin-left: 5px" @click="openRsSimulationModal"
                  >数据权限模拟器</el-button
                >
              </div>
            </el-form-item>
            <el-form-item v-if="modalForm.rowScope === 60" label="指定组织" prop="deptIds">
              <div class="flex items-center">
                <el-button type="primary" size="small" @click="openDeptSelect">选择组织</el-button>
                <span class="ml-2 text-gray-500">已选择 {{ modalForm.deptIds?.length || 0 }} 个组织</span>
              </div>
            </el-form-item>
          </div>
        </el-col>

        <!-- 右侧权限分配 -->
        <el-col :span="12">
          <div class="p-2.5">
            <div class="section-title text-sm font-bold mb-4 pl-2.5">功能权限分配</div>
            <div class="permission-wrapper rounded overflow-hidden">
              <div class="p-2.5 permission-header-bg">
                <el-input v-model="permissionSearch" placeholder="搜索权限码/名称" clearable size="small">
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <div class="mt-2 flex justify-between items-center">
                  <span class="text-xs text-gray-500">已选 {{ selectedPermissionIds.length }} 项</span>
                  <el-button-group>
                    <el-button type="primary" size="small" link @click="selectAllPermissions">全选</el-button>
                    <el-button type="primary" size="small" link @click="deselectAllPermissions">清空</el-button>
                  </el-button-group>
                </div>
              </div>
              <div class="h-[380px] overflow-y-auto p-2.5">
                <el-checkbox-group v-model="selectedPermissionIds">
                  <div v-for="permission in filteredPermissions" :key="permission.id" class="permission-row py-1.5">
                    <el-checkbox
                      :value="permission.id"
                      :disabled="modalMode === 'edit' && isSystemGroup && permission.code === '*:*:*'"
                    >
                      <div class="flex flex-col leading-snug ml-2">
                        <div class="text-[13px] perm-name-color">{{ permission.name }}</div>
                        <div class="text-[11px] font-mono perm-code-color">{{ permission.code }}</div>
                      </div>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
                <el-empty v-if="filteredPermissions.length === 0" :image-size="60" description="无匹配权限" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
        <el-button type="primary" :loading="modalLoading" @click="submitModal">
          {{ modalMode === "add" ? "创建" : "保存" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
  <RsSimulationModal :visible="rsSimulationModalVisible" @close="rsSimulationModalVisible = false" />
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import type { GetGroupListVo, EditGroupDto, GetGroupDetailsVo } from "@/views/auth/api/GroupApi.ts";
import AdminGroupApi from "@/views/auth/api/GroupApi.ts";
import UserGroupService from "@/views/auth/service/UserGroupService.ts";
import UserGpModal from "@/views/auth/components/UserGpModal.vue";
import CoreOrgDeptSelectModal from "@/views/core/components/public/CoreOrgDeptSelectModal.vue";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import RsSimulationModal from "@/views/auth/components/RsSimulationModal.vue";
import GroupMenuModal from "@/views/auth/components/GroupMenuModal.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const modalFormRef = ref<FormInstance>();

/**
 * 用户组列表打包
 */
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, removeListBatch } =
  UserGroupService.useUserGroupList();

/**
 * 用户组模态框打包
 */
const {
  modalVisible,
  modalMode,
  modalLoading,
  isSystemGroup,
  modalForm,
  modalRules,
  permissionSearch,
  selectedPermissionIds,
  filteredPermissions,
  openModal,
  resetModal,
  submitModal,
  selectAllPermissions,
  deselectAllPermissions,
  deptSelectModalVisible,
  openDeptSelect,
  onDeptSelectConfirm,
} = UserGroupService.useUserGroupModal(modalFormRef, loadList);

/**
 * 用户组权限模态框打包
 */
const {
  modalPermissionEditVisible,
  modalPermissionEditRow,
  openPermissionEditModal: openPermModal,
} = UserGroupService.useUserGroupPermissionModal();

const openPermissionEditModal = openPermModal;

/**
 * 选中的列表项
 */
const listSelected = ref<GetGroupListVo[]>([]);

/**
 * 获取组详情（供 ComSeqFixer 使用）
 */
const getGroupDetailForSeq = async (id: string): Promise<GetGroupDetailsVo> => {
  return await AdminGroupApi.getGroupDetails({ id });
};

const menuModalVisible = ref(false);
const menuModalRow = ref<GetGroupListVo | null>(null);

const openMenuModal = (row: GetGroupListVo): void => {
  menuModalRow.value = row;
  menuModalVisible.value = true;
};

const rsSimulationModalVisible = ref(false);
const openRsSimulationModal = (): void => {
  rsSimulationModalVisible.value = true;
};

/**
 * 编辑组排序（供 ComSeqFixer 使用）
 */
const editGroupSeq = async (id: string, dto: any): Promise<void> => {
  const editDto: EditGroupDto = {
    id: dto.id,
    code: dto.code,
    name: dto.name,
    remark: dto.remark,
    status: dto.status,
    seq: dto.seq,
    rowScope: dto.rowScope,
    deptIds: dto.deptIds ?? [],
    permissionIds: dto.permissions ? dto.permissions.filter((p: any) => p.has === 0).map((p: any) => p.id) : [],
  };
  const result = await AdminGroupApi.editGroup(editDto);
  if (result.code !== 0) {
    throw new Error(result.message);
  }
};
</script>

<style scoped>
/* 使用 CSS 变量的样式保留在这里 */
.section-title {
  color: var(--el-text-color-primary);
  border-left: 4px solid var(--el-color-primary);
}

.permission-wrapper {
  border: 1px solid var(--el-border-color-lighter);
}

.permission-header-bg {
  background-color: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.permission-row {
  border-bottom: 1px dashed var(--el-border-color-extra-light);
}

.permission-row:last-child {
  border-bottom: none;
}

.perm-name-color {
  color: var(--el-text-color-primary);
}

.perm-code-color {
  color: var(--el-text-color-secondary);
}

:deep(.el-checkbox) {
  height: auto;
  display: flex;
  align-items: center;
}

:deep(.el-checkbox__label) {
  padding-left: 0;
}
</style>
