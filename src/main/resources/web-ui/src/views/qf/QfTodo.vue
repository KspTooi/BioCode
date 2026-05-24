<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="节点名称">
            <el-input v-model="listForm.nodeName" placeholder="输入节点名称" clearable />
          </el-form-item>
          <el-form-item label="待办状态">
            <el-select v-model="listForm.status" placeholder="选择状态" clearable style="width: 120px">
              <el-option label="待办" :value="0" />
              <el-option label="已办" :value="1" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button :disabled="listLoading" type="primary" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="nodeName" label="当前节点" min-width="120" show-overflow-tooltip />
        <el-table-column prop="bizFormName" label="业务表单" min-width="120" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="initiatorName" label="发起人" min-width="100" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'" size="small">
              {{ scope.row.status === 0 ? "待办" : "已办" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="任务到达时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              :icon="EditIcon"
              link
              type="primary"
              size="small"
              @click="onApproveRoute(scope.row)"
            >
              办理
            </el-button>
            <!-- <el-button
              v-if="scope.row.status === 0"
              :icon="EditIcon"
              link
              type="primary"
              size="small"
              @click="onApprove(scope.row)"
            >
              审批
            </el-button> -->
            <el-button :icon="DeleteIcon" link type="danger" size="small" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 审批模态框 -->
    <QfApproveModal v-model:visible="approveModalVisible" :todo-id="approveTodoId" @approved="loadList()" />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import QfTodoService from "@/views/qf/service/QfTodoService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import type { GetQfTodoListVo } from "@/views/qf/api/QfTodoApi.ts";
import QfApproveModal from "@/views/qf/public/QfApproveModal.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

const approveModalVisible = ref(false);
const approveTodoId = ref<string | null>(null);

const onApprove = (row: GetQfTodoListVo): void => {
  approveTodoId.value = row.id;
  approveModalVisible.value = true;
};

const { cdrcRedirectWithNewTab } = ComDirectRouteContext.useDirectRouteContext();

/**
 * 跳转至流程办理页面
 * @param row 待办事项
 */
const onApproveRoute = (row: GetQfTodoListVo): void => {
  cdrcRedirectWithNewTab("qfApprove", row.id, "流程办理", {
    ...row,
  });
};

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = QfTodoService.useQfTodoList();
</script>

<style scoped></style>
