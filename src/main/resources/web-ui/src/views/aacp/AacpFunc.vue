<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="微函数名称">
            <el-input v-model="listForm.name" placeholder="请输入微函数名称" clearable />
          </el-form-item>
          <el-form-item label="微函数标识">
            <el-input v-model="listForm.code" placeholder="请输入微函数标识" clearable />
          </el-form-item>
          <el-form-item label="意图词">
            <el-input v-model="listForm.description" placeholder="请输入意图词" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button type="success" @click="openModal('add', null)">创建微函数</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" border stripe height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="微函数名称" prop="name" />
        <el-table-column label="微函数标识" prop="code" width="160" />
        <el-table-column label="意图词" prop="description" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑微函数' : '创建微函数'"
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
        label-width="130px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="微函数名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入微函数名称" :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="微函数标识" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入微函数标识" :maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="意图词" prop="description">
          <el-input
            v-model="modalForm.description"
            placeholder="请输入意图词"
            type="textarea"
            :rows="3"
            :maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="入参规范" prop="schema">
          <el-input v-model="modalForm.schema" placeholder="请输入入参规范(JSON)" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="调用目标Bean" prop="target">
          <el-select
            v-model="modalForm.target"
            v-loading="microFuncListLoading"
            placeholder="请选择已注册微函数"
            clearable
            filterable
            allow-create
            style="width: 100%"
          >
            <el-option v-for="item in microFuncListData" :key="item.code" :label="item.code" :value="item.code">
              <span>{{ item.code }}</span>
              <span class="text-gray-400 text-sm ml-2">{{ item.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
            :maxlength="500"
            show-word-limit
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
import { View, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import AacpFuncService from "@/views/aacp/service/AacpFuncService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpFuncService.useAacpFuncList();

const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  microFuncListData,
  microFuncListLoading,
  openModal,
  resetModal,
  submitModal,
} = AacpFuncService.useAacpFuncModal(modalFormRef, loadList);
</script>

<style scoped></style>
