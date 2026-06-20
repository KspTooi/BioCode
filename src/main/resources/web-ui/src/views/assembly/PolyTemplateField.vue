<template>
  <StdListContainer>
    <StdListAreaAction class="flex gap-2">
      <el-button v-if="cdrcCanReturn" type="primary" @click="cdrcReturn">{{ cdrcReturnName }}</el-button>
      <el-button type="success" @click="openModal('add', null)">创建聚合模板字段</el-button>
    </StdListAreaAction>

    <div class="template-info-bar">
      <div class="template-info-item">
        <span class="template-info-label">模板名称</span>
        <span class="template-info-value">{{ templateRow?.name ?? "—" }}</span>
      </div>
      <div class="template-info-divider" />
      <div class="template-info-item">
        <span class="template-info-label">模板代码</span>
        <span class="template-info-value">{{ templateRow?.code ?? "—" }}</span>
      </div>
    </div>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%" row-key="id">
        <!-- 字段名 -->
        <el-table-column prop="name" label="字段名" min-width="130">
          <template #default="scope">
            <el-input
              v-if="isEditingCell(scope.row.id, 'name')"
              v-model="scope.row.name"
              size="small"
              @blur="submitCell(scope.row, 'name')"
            />
            <div v-if="!isEditingCell(scope.row.id, 'name')" class="editable-cell" @click="activateCell(scope.row.id, 'name')">
              {{ scope.row.name || "-" }}
            </div>
          </template>
        </el-table-column>

        <!-- 可见性策略 -->
        <el-table-column prop="policyCrudJson" label="可见性策略" min-width="140">
          <template #default="scope">
            <el-select
              v-if="isEditingCell(scope.row.id, 'policyCrudJson')"
              v-model="scope.row.policyCrudJson"
              multiple
              collapse-tags
              collapse-tags-tooltip
              size="small"
              @visible-change="(visible: boolean) => onPolicyCrudVisibleChange(scope.row, visible)"
            >
              <el-option value="ADD" label="新增" />
              <el-option value="EDIT" label="编辑" />
              <el-option value="DETAILS" label="详情" />
              <el-option value="LIST_QUERY" label="列表查询" />
              <el-option value="LIST_VIEW" label="列表显示" />
            </el-select>
            <div
              v-if="!isEditingCell(scope.row.id, 'policyCrudJson')"
              class="editable-cell editable-cell-inline"
              @click="activateCell(scope.row.id, 'policyCrudJson')"
            >
              <span
                v-for="key in POLICY_CRUD_ORDER"
                :key="key"
                :style="{
                  color: scope.row.policyCrudJson?.includes(key) ? POLICY_CRUD_COLOR_MAP[key] : '#c0c4cc',
                  fontWeight: 500,
                }"
              >{{ POLICY_CRUD_LABEL_MAP[key] }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 查询策略 -->
        <el-table-column prop="policyQuery" label="查询策略" min-width="100">
          <template #default="scope">
            <el-select
              v-if="isEditingCell(scope.row.id, 'policyQuery')"
              :model-value="scope.row.policyQuery"
              size="small"
              @change="(val: number) => submitField(scope.row, 'policyQuery', val)"
            >
              <el-option :value="0" label="等于" />
              <el-option :value="1" label="模糊" />
            </el-select>
            <div v-if="!isEditingCell(scope.row.id, 'policyQuery')" class="editable-cell" @click="activateCell(scope.row.id, 'policyQuery')">
              {{ formatPolicyQuery(scope.row.policyQuery) }}
            </div>
          </template>
        </el-table-column>

        <!-- 显示策略 -->
        <el-table-column prop="policyView" label="显示策略" min-width="100">
          <template #default="scope">
            <el-select
              v-if="isEditingCell(scope.row.id, 'policyView')"
              :model-value="scope.row.policyView"
              size="small"
              @change="(val: number) => submitField(scope.row, 'policyView', val)"
            >
              <el-option :value="0" label="文本框" />
              <el-option :value="1" label="文本域" />
              <el-option :value="2" label="下拉" />
              <el-option :value="3" label="单选" />
              <el-option :value="4" label="多选" />
              <el-option :value="5" label="LD" />
              <el-option :value="6" label="LDT" />
            </el-select>
            <div v-if="!isEditingCell(scope.row.id, 'policyView')" class="editable-cell" @click="activateCell(scope.row.id, 'policyView')">
              {{ formatPolicyView(scope.row.policyView) }}
            </div>
          </template>
        </el-table-column>

        <!-- 排序 -->
        <el-table-column prop="seq" label="排序" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              seq-field="seq"
              :get-detail-api="(id: string) => PolyTemplateFieldApi.getPolyTemplateFieldDetails({ id })"
              :edit-api="(_id: string, dto: any) => PolyTemplateFieldApi.editPolyTemplateField(dto)"
              :display-value="scope.row.seq"
              @success="loadList"
            />
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" fixed="right" min-width="80" align="center">
          <template #default="scope">
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 新增聚合模板字段模态框 -->
    <el-dialog
      v-model="modalVisible"
      title="创建聚合模板字段"
      width="600px"
      :close-on-click-modal="false"
      @close="resetModal()"
    >
      <el-form
        v-if="modalVisible"
        ref="modalFormRef"
        :model="modalForm"
        :rules="modalRules"
        label-width="110px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="字段名" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入字段名" clearable :maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="查询策略" prop="policyQuery">
          <el-select v-model="modalForm.policyQuery" placeholder="选择查询策略">
            <el-option label="等于" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示策略" prop="policyView">
          <el-select v-model="modalForm.policyView" placeholder="选择显示策略">
            <el-option label="文本框" :value="0" />
            <el-option label="文本域" :value="1" />
            <el-option label="下拉" :value="2" />
            <el-option label="单" :value="3" />
            <el-option label="多" :value="4" />
            <el-option label="LD" :value="5" />
            <el-option label="LDT" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button type="primary" @click="submitModal" :loading="modalLoading">创建</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { FormInstance } from "element-plus";
import type { GetPolyTemplateFieldListVo } from "@/views/assembly/api/PolyTemplateFieldApi.ts";
import PolyTemplateFieldApi from "@/views/assembly/api/PolyTemplateFieldApi.ts";
import PolyTemplateFieldService from "@/views/assembly/service/PolyTemplateFieldService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import ComIconService from "@/soa/com-series/service/ComIconService";
import type { GetPolyTemplateListVo } from "@/views/assembly/api/PolyTemplateApi";

const { resolveIcon } = ComIconService.useIconService();

const DeleteIcon = resolveIcon("delete");

const { cdrcCanReturn, cdrcReturnName, cdrcReturn, getCdrcQuery } = ComDirectRouteContext.useDirectRouteContext();
const templateRow = getCdrcQuery() as GetPolyTemplateListVo | null;
const polyTemplateId = ref(templateRow?.id ?? "");

const { listForm, listData, listTotal, listLoading, loadList, removeList } =
  PolyTemplateFieldService.usePolyTemplateFieldList(polyTemplateId);

// ==================== 行内编辑 ====================

const editingCellKey = ref("");

const { submitRow, commitField } = PolyTemplateFieldService.usePolyTemplateFieldCellEdit();

const POLICY_CRUD_LABEL_MAP: Record<string, string> = {
  ADD: "增",
  EDIT: "编",
  DETAILS: "详",
  LIST_QUERY: "查",
  LIST_VIEW: "列",
};
const POLICY_CRUD_COLOR_MAP: Record<string, string> = {
  ADD: "#41b7cc",
  EDIT: "#41b7cc",
  DETAILS: "#41b7cc",
  LIST_QUERY: "#41b7cc",
  LIST_VIEW: "#41b7cc",
};
const POLICY_CRUD_ORDER = ["ADD", "EDIT", "DETAILS", "LIST_QUERY", "LIST_VIEW"] as const;
const POLICY_QUERY_LABEL_MAP: Record<number, string> = {
  0: "等于",
  1: "模糊",
};
const POLICY_VIEW_LABEL_MAP: Record<number, string> = {
  0: "文本框",
  1: "文本域",
  2: "下拉",
  3: "单选",
  4: "多选",
  5: "LD",
  6: "LDT",
};

const buildCellKey = (rowId: string, field: string): string => `${rowId}_${field}`;

const activateCell = (rowId: string, field: string): void => {
  editingCellKey.value = buildCellKey(rowId, field);
};

const clearEditingCell = (): void => {
  editingCellKey.value = "";
};

const isEditingCell = (rowId: string, field: string): boolean => editingCellKey.value === buildCellKey(rowId, field);

const submitCell = async (row: GetPolyTemplateFieldListVo, field: string): Promise<void> => {
  const success = await submitRow(row);
  if (!success) {
    return;
  }
  if (!isEditingCell(row.id, field)) {
    return;
  }
  clearEditingCell();
};

const submitField = async (row: GetPolyTemplateFieldListVo, field: string, value: any): Promise<void> => {
  const success = await commitField(row, field, value);
  if (!success) {
    return;
  }
  clearEditingCell();
};

const onPolicyCrudVisibleChange = async (row: GetPolyTemplateFieldListVo, visible: boolean): Promise<void> => {
  if (visible) {
    return;
  }
  await submitCell(row, "policyCrudJson");
};

const formatPolicyQuery = (value: number): string => POLICY_QUERY_LABEL_MAP[value] ?? "-";
const formatPolicyView = (value: number): string => POLICY_VIEW_LABEL_MAP[value] ?? "-";

// ==================== 新增模态框 ====================

const modalFormRef = ref<FormInstance>();

const { modalVisible, modalLoading, modalForm, modalRules, openModal, resetModal, submitModal } =
  PolyTemplateFieldService.usePolyTemplateFieldModal(modalFormRef, polyTemplateId, loadList);

onMounted(async () => {
  if (!polyTemplateId.value) {
    return;
  }
  await loadList();
});
</script>

<style scoped>
.template-info-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 0;
  border: 1px solid #e4e7ed;
  border-left: 4px solid #409eff;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.template-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 20px;
}

.template-info-label {
  font-size: 11px;
  color: #909399;
  letter-spacing: 0.5px;
}

.template-info-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.template-info-divider {
  width: 1px;
  height: 36px;
  background: #e4e7ed;
  flex-shrink: 0;
}

.editable-cell {
  min-height: 24px;
  line-height: 24px;
  cursor: pointer;
  padding: 0 4px;
}

.editable-cell-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>