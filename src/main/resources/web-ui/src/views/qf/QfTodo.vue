<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="任务到达时间">
            <el-input v-model="listForm.createTime" placeholder="输入任务到达时间" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2"> </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="nodeName" label="当前节点名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="120" show-overflow-tooltip />
        <el-table-column prop="memberId" label="办理成员ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="initiatorId" label="发起人ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="initiatorName" label="发起人名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="initiatorTime" label="发起时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="任务到达时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="onApprove(scope.row)" :icon="EditIcon"> 审批 </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
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
          background
        />
      </template>
    </StdListAreaTable>

    <!-- 新增/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑待办事项' : '新增待办事项'"
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" @click="submitModal" :loading="modalLoading">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审批模态框 -->
    <QfApproveModal v-model:visible="approveModalVisible" :todo-id="approveTodoId" />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import QfTodoService from "@/views/qf/service/QfTodoService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import type { GetQfTodoListVo } from "@/views/qf/api/QfTodoApi.ts";
import QfApproveModal from "@/views/qf/public/QfApproveModal.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

//审批模态框
const approveModalVisible = ref(false); // 审批模态框是否显示
const approveTodoId = ref<string | null>(null); // 审批待办事项ID

const onApprove = (row: GetQfTodoListVo): void => {
  approveTodoId.value = row.id;
  approveModalVisible.value = true;
};

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = QfTodoService.useQfTodoList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QfTodoService.useQfTodoModal(modalFormRef, loadList);
</script>

<style scoped></style>
