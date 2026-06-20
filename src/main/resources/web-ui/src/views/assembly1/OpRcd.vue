<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="输出方案名称">
            <el-input v-model="listForm.opName" placeholder="输入输出方案名称" clearable />
          </el-form-item>
          <el-form-item label="数据源名称">
            <el-input v-model="listForm.dsName" placeholder="输入数据源名称" clearable />
          </el-form-item>
          <el-form-item label="数据源表名">
            <el-input v-model="listForm.dsTableName" placeholder="输入数据源表名" clearable />
          </el-form-item>
          <el-form-item label="模型名称">
            <el-input v-model="listForm.modelName" placeholder="输入模型名称" clearable />
          </el-form-item>
          <el-form-item label="业务域">
            <el-input v-model="listForm.bizDomain" placeholder="输入业务域" clearable />
          </el-form-item>
          <el-form-item label="操作人账号">
            <el-input v-model="listForm.creatorUsername" placeholder="输入操作人账号" clearable />
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
      <el-button type="primary" @click="openModal('add', null)">创建输出方案执行记录</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="opName" label="输出方案名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsName" label="数据源名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsTableName" label="数据源表名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="bizDomain" label="业务域" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="durationMs" label="耗时MS" min-width="120" show-overflow-tooltip />
        <el-table-column prop="creatorUsername" label="操作人账号" min-width="120" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑输出方案执行记录' : '创建输出方案执行记录'"
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
import OpRcdService from "@/views/assembly1/service/OpRcdService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = OpRcdService.useOpRcdList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  OpRcdService.useOpRcdModal(modalFormRef, loadList);
</script>

<style scoped></style>
