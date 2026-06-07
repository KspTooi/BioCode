<template>
  <StdListLayout show-persist-tip>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="能力包名称">
              <el-input v-model="listForm.name" placeholder="请输入能力包名称" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="类型">
              <el-select v-model="listForm.kind" placeholder="请选择" clearable style="width: 200px">
                <el-option label="微函数" :value="0" />
              </el-select>
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
      <el-button type="success" @click="openModal('add', null)">创建能力包</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" border row-key="id" height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="能力包名称" prop="name" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <span v-show="scope.row.kind === 0">微函数</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
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
    :title="modalMode === 'edit' ? '编辑能力包' : '创建能力包'"
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
      <el-form-item label="能力包名称" prop="name">
        <el-input v-model="modalForm.name" placeholder="请输入能力包名称" :maxlength="40" show-word-limit />
      </el-form-item>
      <el-form-item label="类型" prop="kind">
        <el-select v-model="modalForm.kind" placeholder="请选择类型">
          <el-option label="微函数" :value="0" />
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
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { View, Delete, Plus } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import AacpCapabilityService from "@/views/aacp/service/AacpCapabilityService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  AacpCapabilityService.useAacpCapabilityList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpCapabilityService.useAacpCapabilityModal(modalFormRef, loadList);
</script>
