<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="权限代码">
              <el-input v-model="listForm.code" placeholder="输入权限代码查询" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="权限名称">
              <el-input v-model="listForm.name" placeholder="输入权限名称查询" clearable />
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
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button type="primary" @click="openModal('add', null)">创建权限节点</el-button>
      <el-button
        type="danger"
        :disabled="listSelected.length === 0"
        :loading="listLoading"
        @click="removeListBatch(listSelected)"
      >
        批量删除
      </el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table
        v-loading="listLoading"
        :data="listData"
        stripe
        border
        height="100%"
        @selection-change="(val: GetPermissionListVo[]) => (listSelected = val)"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
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
          :formatter="(_, __, value) => value ?? '-'"
        />
        <el-table-column label="系统权限" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isSystem === 1 ? 'warning' : 'info'">
              {{ scope.row.isSystem === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
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
    </StdListAreaTable>

    <!-- 权限编辑/创建模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑权限节点' : '创建权限节点'"
      width="500px"
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
        <!-- 编辑时显示的只读信息 -->
        <template v-if="modalMode === 'edit'">
          <el-form-item label="创建时间">
            <el-input v-model="modalForm.createTime" disabled />
          </el-form-item>
          <el-form-item label="修改时间">
            <el-input v-model="modalForm.updateTime" disabled />
          </el-form-item>
          <el-form-item label="系统权限">
            <el-tag :type="modalForm.isSystem === 1 ? 'warning' : 'info'">
              {{ modalForm.isSystem === 1 ? "是" : "否" }}
            </el-tag>
          </el-form-item>
        </template>

        <!-- 可编辑字段 -->
        <el-form-item label="权限代码" prop="code">
          <el-input
            v-model="modalForm.code"
            :disabled="modalMode === 'edit' && modalForm.isSystem === 1"
            :placeholder="modalMode === 'edit' && modalForm.isSystem === 1 ? '系统权限不可修改代码' : '请输入权限代码'"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input
            v-model="modalForm.name"
            :maxlength="32"
            show-word-limit
            :disabled="modalMode === 'edit' && modalForm.isSystem === 1"
            :placeholder="modalMode === 'edit' && modalForm.isSystem === 1 ? '系统权限不可修改名称' : '请输入权限名称'"
          />
        </el-form-item>
        <el-form-item label="权限描述" prop="remark">
          <el-input
            v-model="modalForm.remark"
            show-word-limit
            :maxlength="200"
            type="textarea"
            :autosize="{ minRows: 3 }"
            placeholder="请输入权限描述"
          />
        </el-form-item>
        <el-form-item label="排序号" prop="seq">
          <el-input-number v-model="modalForm.seq" :min="0" />
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
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import type { GetPermissionListVo } from "@/views/auth/api/PermissionApi.ts";
import PermissionService from "@/views/auth/service/PermissionService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表部分
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, removeListBatch } =
  PermissionService.usePermissionList();

// 模态框部分
const modalFormRef = ref<FormInstance>();
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PermissionService.usePermissionModal(modalFormRef, loadList);

/**
 * 选中的列表项
 */
const listSelected = ref<GetPermissionListVo[]>([]);
</script>

<style scoped></style>
