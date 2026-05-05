<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="租户名称">
            <el-input v-model="listForm.name" placeholder="输入租户名称" clearable />
          </el-form-item>
          <el-form-item label="到期时间">
            <el-date-picker
              v-model="listForm.expireTime"
              type="datetime"
              placeholder="选择到期时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="正常" :value="0" />
              <el-option label="停用" :value="1" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">创建租户</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="租户名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="ruCount" label="用户总数" min-width="80" align="center" />
        <el-table-column prop="adminUsername" label="管理员账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="expireTime" label="到期时间" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.expireTime">{{ scope.row.expireTime }}</span>
            <span v-if="!scope.row.expireTime" class="text-gray-400">无限制</span>
          </template>
        </el-table-column>
        <el-table-column prop="isSystem" label="内置" min-width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isSystem === 1" type="warning">是</el-tag>
            <el-tag v-if="scope.row.isSystem === 0" type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="info">停用</el-tag>
            <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="260">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              :icon="EditIcon"
              :disabled="scope.row.isSystem === 1"
              @click="openRpModal(scope.row)"
            >
              管理菜单包
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :icon="DeleteIcon"
              :disabled="scope.row.isSystem === 1"
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
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑租户' : '创建租户'"
      width="600px"
      :close-on-click-modal="false"
      @close="
        resetModal();
        loadList();
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
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入租户名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireTime">
          <el-date-picker
            v-model="modalForm.expireTime"
            type="datetime"
            placeholder="请选择到期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            clearable
            :disabled="modalMode === 'edit' && modalForm.isSystem === 1"
            :disabled-date="
              (date) => {
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                return date.getTime() < today.getTime();
              }
            "
          />
        </el-form-item>
        <el-form-item label="管理员账号" :prop="modalMode === 'add' ? '_adminUsername' : undefined">
          <el-input
            :maxlength="40"
            v-model="modalForm._adminUsername"
            placeholder="请输入管理员账号"
            :disabled="modalMode === 'edit'"
            clearable
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="modalMode === 'add'" label="管理员密码" prop="_adminPassword">
          <el-input
            :maxlength="40"
            v-model="modalForm._adminPassword"
            placeholder="请输入管理员密码"
            show-password
            clearable
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入备注"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status" :disabled="modalMode === 'edit' && modalForm.isSystem === 1">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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

    <RootRpModal :visible="rpModalVisible" :data="rpModalRow" @close="rpModalVisible = false" @success="loadList" />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import type { GetCoreRootListVo } from "@/views/core/api/CoreRootApi.ts";
import CoreRootService from "@/views/core/service/CoreRootService.ts";
import RootRpModal from "@/views/core/components/RootRpModal.vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = CoreRootService.useCoreRootList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  CoreRootService.useCoreRootModal(modalFormRef, loadList);

const rpModalVisible = ref(false);
const rpModalRow = ref<GetCoreRootListVo | null>(null);

const openRpModal = (row: GetCoreRootListVo): void => {
  rpModalRow.value = row;
  rpModalVisible.value = true;
};
</script>

<style scoped></style>
