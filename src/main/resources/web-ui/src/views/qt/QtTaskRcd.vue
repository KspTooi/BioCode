<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery show-persist-tip>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="任务名">
            <el-input v-model="listForm.taskName" placeholder="输入任务名" clearable />
          </el-form-item>
          <el-form-item label="分组名">
            <el-input v-model="listForm.groupName" placeholder="输入分组名" clearable />
          </el-form-item>
          <el-form-item label="运行状态">
            <el-select v-model="listForm.status" placeholder="选择运行状态" clearable class="!w-[180px]">
              <el-option label="正常" :value="0" />
              <el-option label="失败" :value="1" />
              <el-option label="超时" :value="2" />
              <el-option label="已调度" :value="3" />
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
    <StdListAreaAction>
      <el-button type="danger" :disabled="listSelected.length === 0" @click="removeListBatch"> 批量删除 </el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="groupName" label="任务分组" min-width="120" show-overflow-tooltip />
        <el-table-column prop="target" label="调用目标" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="运行状态" min-width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="danger">失败</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">超时</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="info">已调度</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="运行开始时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="endTime" label="运行结束时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="costTime" label="耗时(MS)" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="150">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal(scope.row)"> 查看 </el-button>
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

    <!-- 查看详情模态框 -->
    <el-dialog
      v-model="modalVisible"
      title="查看调度日志详情"
      width="700px"
      :close-on-click-modal="false"
      @close="resetModal()"
    >
      <el-descriptions v-if="modalVisible" v-loading="modalLoading" :column="2" border>
        <el-descriptions-item label="任务名称" :span="2">{{ modalForm.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务分组" :span="2">{{ modalForm.groupName }}</el-descriptions-item>
        <el-descriptions-item label="调用目标" :span="2">{{ modalForm.target }}</el-descriptions-item>
        <el-descriptions-item label="运行状态" :span="2">
          <el-tag v-if="modalForm.status === 0" type="success">正常</el-tag>
          <el-tag v-else-if="modalForm.status === 1" type="danger">失败</el-tag>
          <el-tag v-else-if="modalForm.status === 2" type="warning">超时</el-tag>
          <el-tag v-else-if="modalForm.status === 3" type="info">已调度</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运行开始时间">{{ modalForm.startTime }}</el-descriptions-item>
        <el-descriptions-item label="运行结束时间">{{ modalForm.endTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时(MS)" :span="2">{{ modalForm.costTime }}</el-descriptions-item>
        <el-descriptions-item label="调用目标参数" :span="2">
          <el-input v-model="modalForm.targetParam" type="textarea" :rows="4" readonly placeholder="无" />
        </el-descriptions-item>
        <el-descriptions-item label="调用目标返回内容" :span="2">
          <el-input v-model="modalForm.targetResult" type="textarea" :rows="6" readonly placeholder="无" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="modalVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { markRaw } from "vue";
import { View, Delete } from "@element-plus/icons-vue";
import QtTaskRcdService from "@/views/qt/service/QtTaskRcdService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const {
  listForm,
  listData,
  listTotal,
  listLoading,
  listSelected,
  loadList,
  resetList,
  removeList,
  removeListBatch,
  onSelectionChange,
} = QtTaskRcdService.useQtTaskRcdList();

// 模态框打包
const { modalVisible, modalLoading, modalForm, openModal, resetModal } = QtTaskRcdService.useQtTaskRcdModal();
</script>

<style scoped></style>
