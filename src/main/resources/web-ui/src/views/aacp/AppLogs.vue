<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="应用ID">
            <el-input v-model="listForm.appId" placeholder="输入应用ID" clearable />
          </el-form-item>
          <el-form-item label="供应商ID">
            <el-input v-model="listForm.providerId" placeholder="输入供应商ID" clearable />
          </el-form-item>
          <el-form-item label="模型变体ID">
            <el-input v-model="listForm.modelId" placeholder="输入模型变体ID" clearable />
          </el-form-item>
          <el-form-item label="输入词元">
            <el-input v-model.number="listForm.inputToken" placeholder="输入输入词元" clearable />
          </el-form-item>
          <el-form-item label="输出词元">
            <el-input v-model.number="listForm.outputToken" placeholder="输入输出词元" clearable />
          </el-form-item>
          <el-form-item label="消耗金额">
            <el-input v-model="listForm.cost" placeholder="输入消耗金额" clearable />
          </el-form-item>
          <el-form-item label="发起时间">
            <el-input v-model="listForm.startTime" placeholder="输入发起时间" clearable />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-input v-model="listForm.endTime" placeholder="输入结束时间" clearable />
          </el-form-item>
          <el-form-item label="总耗时MS">
            <el-input v-model.number="listForm.durationMs" placeholder="输入总耗时MS" clearable />
          </el-form-item>
          <el-form-item label="首字响应时间">
            <el-input v-model.number="listForm.ttfbMs" placeholder="输入首字响应时间" clearable />
          </el-form-item>
          <el-form-item label="HTTP状态码">
            <el-input v-model="listForm.statusCode" placeholder="输入HTTP状态码" clearable />
          </el-form-item>
          <el-form-item label="客户端IP">
            <el-input v-model="listForm.clientIp" placeholder="输入客户端IP" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="primary" @click="openModal('add', null)">创建模型调用记录</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="appId" label="应用ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="providerId" label="供应商ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="modelId" label="模型变体ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="inputToken" label="输入词元" min-width="120" show-overflow-tooltip />
        <el-table-column prop="outputToken" label="输出词元" min-width="120" show-overflow-tooltip />
        <el-table-column prop="cost" label="消耗金额" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" label="发起时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="endTime" label="结束时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="durationMs" label="总耗时MS" min-width="120" show-overflow-tooltip />
        <el-table-column prop="ttfbMs" label="首字响应时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="statusCode" label="HTTP状态码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="clientIp" label="客户端IP" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑模型调用记录' : '创建模型调用记录'"
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
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button type="primary" @click="submitModal" :loading="modalLoading">
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
import AppLogsService from "@/views/aacp/service/AppLogsService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AppLogsService.useAppLogsList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AppLogsService.useAppLogsModal(modalFormRef, loadList);
</script>

<style scoped></style>
