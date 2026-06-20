<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="应用名称">
            <el-input v-model="listForm.name" placeholder="输入应用名称" clearable />
          </el-form-item>
          <el-form-item label="应用代码">
            <el-input v-model="listForm.code" placeholder="输入应用代码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
              <el-option label="禁用" :value="0" />
              <el-option label="启用" :value="1" />
            </el-select>
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
      <el-button type="primary" @click="openModal('add', null)">创建应用</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="应用名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="应用代码" min-width="120" show-overflow-tooltip />
        <el-table-column label="是否公开" min-width="90" align="center">
          <template #default="scope">
            <span v-if="scope.row.isPublic === 0" class="text-slate-500">不公开</span>
            <span v-if="scope.row.isPublic === 1" class="text-indigo-600">公开</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-if="scope.row.status === 0" type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑应用' : '创建应用'"
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
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入应用名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="应用代码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入应用代码" clearable :maxlength="16" show-word-limit />
        </el-form-item>
        <el-form-item label="访问密钥" prop="appKey" v-if="modalMode === 'edit'">
          <el-input v-model="modalForm.appKey" placeholder="访问密钥" clearable disabled />
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-radio-group v-model="modalForm.isPublic">
            <el-radio :value="0">不公开</el-radio>
            <el-radio :value="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="IP白名单" prop="ips">
          <el-input v-model="modalForm.ips" placeholder="多个IP用逗号分隔" clearable :maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="modalForm.remark" placeholder="请输入备注" clearable type="textarea" :rows="3" :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import AacpAppService from "@/views/aacp/service/AacpAppService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpAppService.useAacpAppList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpAppService.useAacpAppModal(modalFormRef, loadList);
</script>

<style scoped></style>