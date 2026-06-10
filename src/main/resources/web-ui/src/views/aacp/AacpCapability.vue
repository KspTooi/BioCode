<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="能力包名称">
            <el-input v-model="listForm.name" placeholder="请输入能力包名称" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="listForm.kind" placeholder="请选择" clearable>
              <el-option label="微函数" :value="0" />
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
      <el-button type="success" @click="openModal('add', null)">创建能力包</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" border stripe height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="能力包名称" prop="name" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <span v-show="scope.row.kind === 0">微函数</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        <el-table-column label="微函数" prop="funcCount" width="80" align="center" />
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
      :title="modalMode === 'edit' ? '编辑能力包' : '创建能力包'"
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
        <el-form-item label="绑定微函数" prop="funcIds">
          <el-select v-model="modalForm.funcIds" multiple filterable :loading="funcLoading" placeholder="请选择微函数">
            <el-option v-for="item in funcOptions" :key="item.id" :label="item.name" :value="item.id" />
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
import AacpCapabilityService from "@/views/aacp/service/AacpCapabilityService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  AacpCapabilityService.useAacpCapabilityList();

const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  funcOptions,
  funcLoading,
  openModal,
  resetModal,
  submitModal,
} = AacpCapabilityService.useAacpCapabilityModal(modalFormRef, loadList);
</script>

<style scoped></style>
