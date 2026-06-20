<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="聚合模板ID">
            <el-input v-model="listForm.polyTemplateId" placeholder="输入聚合模板ID" clearable />
          </el-form-item>
          <el-form-item label="字段名">
            <el-input v-model="listForm.name" placeholder="输入字段名" clearable />
          </el-form-item>
          <el-form-item label="可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW">
            <el-input
              v-model="listForm.policyCrudJson"
              placeholder="输入可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW"
              clearable
            />
          </el-form-item>
          <el-form-item label="查询策略 0:等于">
            <el-input v-model.number="listForm.policyQuery" placeholder="输入查询策略 0:等于" clearable />
          </el-form-item>
          <el-form-item label="显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT">
            <el-input
              v-model.number="listForm.policyView"
              placeholder="输入显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT"
              clearable
            />
          </el-form-item>
          <el-form-item label="排序">
            <el-input v-model.number="listForm.seq" placeholder="输入排序" clearable />
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
      <el-button type="success" @click="openModal('add', null)">新增聚合模板字段</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="polyTemplateId" label="聚合模板ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="字段名" min-width="120" show-overflow-tooltip />
        <el-table-column
          prop="policyCrudJson"
          label="可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="policyQuery" label="查询策略 0:等于" min-width="120" show-overflow-tooltip />
        <el-table-column
          prop="policyView"
          label="显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="seq" label="排序" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #pagination>
        <el-pagination
          v-model:current-page="listForm.pageNum"
          v-model:page-size="listForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="listTotal"
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
          background
        />
      </template>
    </StdListAreaTable>

    <!-- 新增/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑聚合模板字段' : '新增聚合模板字段'"
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
        <el-form-item label="聚合模板ID" prop="polyTemplateId">
          <el-input v-model="modalForm.polyTemplateId" placeholder="请输入聚合模板ID" clearable />
        </el-form-item>
        <el-form-item label="字段名" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入字段名" clearable :maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW" prop="policyCrudJson">
          <el-input
            v-model="modalForm.policyCrudJson"
            placeholder="请输入可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW"
            clearable
          />
        </el-form-item>
        <el-form-item label="查询策略 0:等于" prop="policyQuery">
          <el-input v-model.number="modalForm.policyQuery" placeholder="请输入查询策略 0:等于" clearable />
        </el-form-item>
        <el-form-item label="显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT" prop="policyView">
          <el-input
            v-model.number="modalForm.policyView"
            placeholder="请输入显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT"
            clearable
          />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
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
import PolyTemplateFieldService from "@/views/assembly/service/PolyTemplateFieldService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  PolyTemplateFieldService.usePolyTemplateFieldList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PolyTemplateFieldService.usePolyTemplateFieldModal(modalFormRef, loadList);
</script>

<style scoped></style>
