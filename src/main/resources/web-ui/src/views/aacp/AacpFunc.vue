<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="微函数名称">
            <el-input v-model="listForm.name" placeholder="输入微函数名称" clearable />
          </el-form-item>
          <el-form-item label="微函数标识">
            <el-input v-model="listForm.code" placeholder="输入微函数标识" clearable />
          </el-form-item>
          <el-form-item label="意图词">
            <el-input v-model="listForm.description" placeholder="输入意图词" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">新增微函数</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :loadList="loadList" :listTotal="listTotal">
      <el-table :data="listData" v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="微函数名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="微函数标识" width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="意图词" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="140">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑微函数' : '新增微函数'"
      width="600px"
      :close-on-click-modal="false"
      @close="resetModal"
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
          <el-input v-model="modalForm.description" placeholder="请输入意图词" type="textarea" :rows="3" :maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="入参规范" prop="schema">
          <el-input v-model="modalForm.schema" placeholder="请输入入参规范(JSON)" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="调用目标Bean" prop="target">
          <el-input v-model="modalForm.target" placeholder="请输入调用目标Bean" :maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="modalForm.remark" placeholder="请输入备注" type="textarea" :rows="3" :maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "新增" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { FormInstance } from "element-plus";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import AacpFuncService from "@/views/aacp/service/AacpFuncService.ts";

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  AacpFuncService.useAacpFuncList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpFuncService.useAacpFuncModal(modalFormRef, loadList);
</script>

<style scoped></style>
