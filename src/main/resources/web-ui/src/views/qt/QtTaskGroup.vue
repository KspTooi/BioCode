<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery show-persist-tip>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="分组名">
              <el-input v-model="listForm.name" placeholder="输入分组名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="分组备注">
              <el-input v-model="listForm.remark" placeholder="输入分组备注" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <!-- 占位列 -->
          </el-col>
          <el-col :span="4" :offset="2">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" style="margin-left: 12px" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction>
      <el-button type="success" @click="openModal('add', null)">创建任务分组</el-button>
      <el-button type="danger" :disabled="listSelected?.length === 0" @click="removeListBatch">批量删除</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="分组名" min-width="150" show-overflow-tooltip />
        <el-table-column
          prop="remark"
          label="分组备注"
          min-width="200"
          show-overflow-tooltip
          :formatter="(_, __, value) => value ?? '-'"
        />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
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
      :title="modalMode === 'edit' ? '编辑任务分组' : '创建任务分组'"
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
        <el-form-item label="分组名" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入分组名" maxlength="80" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="分组备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            type="textarea"
            placeholder="请输入分组备注"
            maxlength="1000"
            show-word-limit
            :rows="4"
            clearable
          />
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
import QtTaskGroupService from "@/views/qt/service/QtTaskGroupService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const {
  listForm,
  listData,
  listSelected,
  listTotal,
  listLoading,
  loadList,
  resetList,
  removeList,
  removeListBatch,
  onSelectionChange,
} = QtTaskGroupService.useQtTaskGroupList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QtTaskGroupService.useQtTaskGroupModal(modalFormRef, loadList);
</script>

<style scoped></style>
