<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="服务器名称">
            <el-input v-model="listForm.name" placeholder="请输入服务器名称" clearable />
          </el-form-item>
          <el-form-item label="唯一编码">
            <el-input v-model="listForm.code" placeholder="请输入唯一编码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择" clearable>
              <el-option label="离线" :value="0" />
              <el-option label="在线" :value="1" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button type="success" @click="openModal('add', null)">创建智能体枢纽</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" border stripe height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="服务器名称" prop="name" />
        <el-table-column label="唯一编码" prop="code" width="130" />
        <el-table-column label="通信协议" width="110" align="center">
          <template #default="scope">
            <span v-show="scope.row.networkKind === 0">HTTP+SSE</span>
          </template>
        </el-table-column>
        <el-table-column label="鉴权类型" width="100" align="center">
          <template #default="scope">
            <span v-show="scope.row.authKind === 0">无</span>
            <span v-show="scope.row.authKind === 1">PSK</span>
          </template>
        </el-table-column>
        <el-table-column label="能力包" prop="capabilityCount" width="80" align="center" />
        <el-table-column label="微函数" prop="funcCount" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <span v-show="scope.row.status === 0" style="color: #999">离线</span>
            <span v-show="scope.row.status === 1" style="color: #67c23a">在线</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑智能体枢纽' : '创建智能体枢纽'"
      width="550px"
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
        label-width="110px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="服务器名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入服务器名称" :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="唯一编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入唯一编码" :maxlength="16" show-word-limit />
        </el-form-item>
        <el-form-item label="通信协议">
          <el-select v-model="modalForm.networkKind" placeholder="请选择通信协议">
            <el-option label="HTTP+SSE" :value="0" />
            <el-option label="WS" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="鉴权类型" prop="authKind">
          <el-select v-model="modalForm.authKind" placeholder="请选择鉴权类型">
            <el-option label="无" :value="0" />
            <el-option label="PSK" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="modalForm.authKind === 1" label="预共享密钥" prop="authPsk">
          <el-input
            v-model="modalForm.authPsk"
            placeholder="请输入预共享密钥"
            type="textarea"
            :rows="4"
            :maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="modalForm.status" placeholder="请选择状态">
            <el-option label="离线" :value="0" />
            <el-option label="在线" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定能力包" prop="capabilityIds">
          <el-select
            v-model="modalForm.capabilityIds"
            multiple
            filterable
            :loading="capabilityLoading"
            placeholder="请选择能力包"
          >
            <el-option v-for="item in capabilityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
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
import { View, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import AacpAgentHubService from "@/views/aacp/service/AacpAgentHubService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpAgentHubService.useAgentHubList();

const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  capabilityOptions,
  capabilityLoading,
  openModal,
  resetModal,
  submitModal,
} = AacpAgentHubService.useAgentHubModal(modalFormRef, loadList);
</script>

<style scoped></style>
