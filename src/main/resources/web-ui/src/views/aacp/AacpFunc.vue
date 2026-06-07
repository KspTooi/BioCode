<template>
  <StdListLayout show-persist-tip>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="微函数名称">
              <el-input v-model="listForm.name" placeholder="请输入微函数名称" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="微函数标识">
              <el-input v-model="listForm.code" placeholder="请输入微函数标识" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="意图词">
              <el-input v-model="listForm.description" placeholder="请输入意图词" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8" :offset="16" style="display: flex; justify-content: flex-end">
            <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
            <el-button :disabled="listLoading" @click="resetList">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">新增微函数</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" border row-key="id" height="100%">
        <el-table-column label="微函数名称" prop="name" />
        <el-table-column label="微函数标识" prop="code" width="160" />
        <el-table-column label="意图词" prop="description" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

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
  </StdListLayout>

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
        <el-input v-model="modalForm.name" placeholder="请输入微函数名称" />
      </el-form-item>
      <el-form-item label="微函数标识" prop="code">
        <el-input v-model="modalForm.code" placeholder="请输入微函数标识" />
      </el-form-item>
      <el-form-item label="意图词" prop="description">
        <el-input v-model="modalForm.description" placeholder="请输入意图词" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="入参规范" prop="schema">
        <el-input v-model="modalForm.schema" placeholder="请输入入参规范(JSON)" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="调用目标Bean" prop="target">
        <el-input v-model="modalForm.target" placeholder="请输入调用目标Bean" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="modalForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
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
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { FormInstance } from "element-plus";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import AacpFuncService from "@/views/aacp/service/AacpFuncService.ts";

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  AacpFuncService.useAacpFuncList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpFuncService.useAacpFuncModal(modalFormRef, loadList);
</script>
