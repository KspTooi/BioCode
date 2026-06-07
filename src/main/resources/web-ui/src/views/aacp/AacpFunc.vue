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
        </el-row>
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="意图词">
              <el-input v-model="listForm.description" placeholder="请输入意图词" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="8" :offset="4" style="display: flex; justify-content: flex-end">
            <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
            <el-button :disabled="listLoading" @click="resetList">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建微函数</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" border row-key="id" height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="微函数名称" prop="name" />
        <el-table-column label="微函数标识" prop="code" width="160" />
        <el-table-column label="意图词" prop="description" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)">删除</el-button>
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
        <el-input v-model="modalForm.target" placeholder="请输入调用目标Bean" :maxlength="1000" show-word-limit />
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
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { View, Delete, Plus } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import AacpFuncService from "@/views/aacp/service/AacpFuncService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpFuncService.useAacpFuncList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpFuncService.useAacpFuncModal(modalFormRef, loadList);
</script>
