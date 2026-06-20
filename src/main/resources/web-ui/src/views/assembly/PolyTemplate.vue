<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="模板名称">
            <el-input v-model="listForm.name" placeholder="输入模板名称" clearable />
          </el-form-item>
          <el-form-item label="模板代码">
            <el-input v-model="listForm.code" placeholder="输入模板代码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="选择状态" clearable>
              <el-option label="全部" :value="null" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">创建聚合模板</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模板名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模板代码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="seq" label="排序" min-width="65" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="80" show-overflow-tooltip>
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑聚合模板' : '创建聚合模板'"
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
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入模板名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="模板代码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入模板代码" clearable :maxlength="16" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="modalForm.status" placeholder="选择状态">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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
import PolyTemplateService from "@/views/assembly/service/PolyTemplateService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  PolyTemplateService.usePolyTemplateList();

const modalFormRef = ref<FormInstance>();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PolyTemplateService.usePolyTemplateModal(modalFormRef, loadList);
</script>

<style scoped></style>