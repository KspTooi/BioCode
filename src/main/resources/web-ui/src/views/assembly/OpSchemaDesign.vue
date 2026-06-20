<template>
  <StdListContainer>
    <!-- 当前输出方案信息区域 -->
    <div v-loading="polyListLoading || originListLoading" class="schema-info-bar">
      <div class="schema-info-item">
        <span class="schema-info-label">输出方案</span>
        <span class="schema-info-value">{{ schemaInfo.name }}</span>
      </div>
      <div class="schema-info-divider" />
      <div class="schema-info-item">
        <span class="schema-info-label">模型名称</span>
        <span class="schema-info-value">{{ schemaInfo.modelName }}</span>
      </div>
      <div class="schema-info-divider" />
      <div class="schema-info-item">
        <span class="schema-info-label">数据源表名</span>
        <span class="schema-info-value">{{ schemaInfo.tableName ?? "未配置" }}</span>
      </div>
      <div class="schema-info-divider" />
      <div class="schema-info-item">
        <span class="schema-info-label">字段数量</span>
        <span v-if="listViewModel === 'raw'" class="schema-info-value">{{ originListData.length }}</span>
        <span v-if="listViewModel === 'poly'" class="schema-info-value">{{ polyListData.length }}</span>
      </div>
      <div class="schema-info-divider" />
      <div class="schema-info-item">
        <span class="schema-info-label">显示原始模型</span>
        <el-switch
          v-model="listViewModel"
          :disabled="polyListLoading || originListLoading"
          :active-value="'raw'"
          :inactive-value="'poly'"
          size="small"
        />
      </div>
      <div class="schema-info-divider" />
      <div v-show="listViewModel === 'poly'" class="schema-info-item">
        <span class="schema-info-label">从原始模型导入</span>
        <el-button type="primary" size="small" :icon="MagicStickIcon" @click="importFromRaw(importTemplateId || undefined)"
          >导入</el-button
        >
      </div>
      <div v-show="listViewModel === 'poly'" class="schema-info-item">
        <span class="schema-info-label">选择一个聚合模板</span>
        <el-select
          v-model="importTemplateId"
          placeholder="选择模板（可选）"
          clearable
          filterable
          size="small"
          style="width: 180px"
          :loading="importTemplateLoading"
          @focus="loadImportTemplates"
        >
          <el-option v-for="tpl in importTemplateOptions" :key="tpl.id" :label="`${tpl.name}(${tpl.code})`" :value="tpl.id" />
        </el-select>
      </div>
      <div v-show="listViewModel === 'raw'" class="schema-info-item">
        <span class="schema-info-label">从数据源导入</span>
        <el-button type="primary" size="small" :icon="MagicStickIcon" @click="syncRawModelFromDataSource">导入</el-button>
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div v-loading="polyListLoading || originListLoading" class="action-bar">
      <el-button type="primary" :icon="CloseIcon" link @click="goToList">回退</el-button>
      <el-button :disabled="listViewModel === 'raw'" type="success" :icon="AddFieldIcon" link @click="openPolyAddModal()"
        >添加字段</el-button
      >
      <el-button type="primary" :icon="PreviewIcon" link @click="goToPreview">转到模拟</el-button>
    </div>

    <!-- 原始模型列表表格区域 -->
    <StdListAreaTable v-show="listViewModel === 'raw'">
      <el-table v-loading="originListLoading" :data="originListData" stripe border height="100%">
        <el-table-column prop="seq" label="序号" min-width="45" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="字段名" min-width="95" show-overflow-tooltip />
        <el-table-column prop="dataType" label="数据类型" min-width="80" show-overflow-tooltip />
        <el-table-column prop="length" label="长度" min-width="40" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.length == null" class="text-gray-400">不支持</span>
            <span v-else>{{ scope.row.length }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="require" label="必填" min-width="25" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-checkbox v-if="scope.row.require === 1" :model-value="true" />
            <el-checkbox v-else :model-value="false" />
          </template>
        </el-table-column>
        <el-table-column prop="pk" label="主键" min-width="25" show-overflow-tooltip align="center">
          <template #default="scope">
            <div v-if="scope.row.pk === 1" style="display: flex; justify-content: center; align-items: center">
              <PkIcon style="font-size: 18px" />
            </div>
            <span v-else class="text-gray-400">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="注释" min-width="120" show-overflow-tooltip />
      </el-table>
    </StdListAreaTable>

    <!-- 聚合模型列表表格区域 -->
    <StdListAreaTable v-show="listViewModel === 'poly'">
      <el-table v-loading="polyListLoading" :data="polyListData" row-key="id" stripe border height="100%">
        <!-- 序号 -->
        <el-table-column prop="seq" label="序号" min-width="70" align="center">
          <template #default="scope">
            <el-input
              v-if="isEditingCell(scope.row.id, 'seq')"
              v-model.number="scope.row.seq"
              size="small"
              @blur="submitCell(scope.row, 'seq')"
            />
            <div v-if="!isEditingCell(scope.row.id, 'seq')" class="editable-cell" @click="activateCell(scope.row.id, 'seq')">
              {{ scope.row.seq ?? "-" }}
            </div>
          </template>
        </el-table-column>

        <!-- 聚合字段名 -->
        <el-table-column prop="name" label="字段名" min-width="150">
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

        <!-- 备注 -->
        <el-table-column prop="remark" label="字段备注" min-width="150">
          <template #default="scope">
            <el-input
              v-if="isEditingCell(scope.row.id, 'remark')"
              v-model="scope.row.remark"
              size="small"
              @blur="submitCell(scope.row, 'remark')"
            />
            <div
              v-if="!isEditingCell(scope.row.id, 'remark')"
              class="editable-cell"
              @click="activateCell(scope.row.id, 'remark')"
            >
              {{ scope.row.remark || "-" }}
            </div>
          </template>
        </el-table-column>

        <!-- 数据类型 -->
        <el-table-column prop="dataType" label="数据类型" min-width="120">
          <template #default="scope">
            <el-input
              v-if="isEditingCell(scope.row.id, 'dataType')"
              v-model="scope.row.dataType"
              size="small"
              @blur="submitCell(scope.row, 'dataType')"
            />
            <div
              v-if="!isEditingCell(scope.row.id, 'dataType')"
              class="editable-cell"
              @click="activateCell(scope.row.id, 'dataType')"
            >
              {{ scope.row.dataType || "-" }}
            </div>
          </template>
        </el-table-column>

        <!-- 长度 -->
        <el-table-column prop="length" label="长度" min-width="80">
          <template #default="scope">
            <el-input
              v-if="isEditingCell(scope.row.id, 'length')"
              v-model="scope.row.length"
              size="small"
              @blur="submitCell(scope.row, 'length')"
            />
            <div
              v-if="!isEditingCell(scope.row.id, 'length')"
              class="editable-cell"
              @click="activateCell(scope.row.id, 'length')"
            >
              {{ scope.row.length || "-" }}
            </div>
          </template>
        </el-table-column>

        <!-- 必填 -->
        <el-table-column prop="require" label="必填" min-width="60" align="center">
          <template #default="scope">
            <div class="editable-cell editable-cell-center" @click="toggleRequire(scope.row)">
              <span :style="{ color: scope.row.require === 1 ? '#F56C6C' : '#909399' }">
                {{ scope.row.require === 1 ? "是" : "否" }}
              </span>
            </div>
          </template>
        </el-table-column>

        <!-- 可见性策略 -->
        <el-table-column prop="policyCrudJson" label="可见性策略" min-width="120">
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
                >{{ POLICY_CRUD_LABEL_MAP[key] }}</span
              >
            </div>
          </template>
        </el-table-column>

        <!-- 显示策略 -->
        <el-table-column prop="policyView" label="显示策略" min-width="120">
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
            <div
              v-if="!isEditingCell(scope.row.id, 'policyView')"
              class="editable-cell"
              @click="activateCell(scope.row.id, 'policyView')"
            >
              {{ formatPolicyView(scope.row.policyView) }}
            </div>
          </template>
        </el-table-column>

        <!-- 主键 -->
        <el-table-column prop="pk" label="主键" min-width="45" show-overflow-tooltip align="center">
          <template #default="scope">
            <div v-if="scope.row.pk === 1" style="display: flex; justify-content: center; align-items: center">
              <PkIcon style="font-size: 18px" />
            </div>
            <span v-else class="text-gray-400">否</span>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" fixed="right" min-width="80" align="center">
          <template #default="scope">
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removePolyList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 新增聚合字段模态框 -->
    <el-dialog
      v-model="polyAddModalVisible"
      title="新增聚合字段"
      width="600px"
      :close-on-click-modal="false"
      @close="resetPolyAddModal"
    >
      <el-form
        v-if="polyAddModalVisible"
        ref="polyAddFormRef"
        :model="polyAddForm"
        :rules="polyAddRules"
        label-width="130px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="字段名" prop="name">
          <el-input v-model="polyAddForm.name" placeholder="请输入字段名" clearable maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-input v-model="polyAddForm.dataType" placeholder="请输入数据类型" clearable maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="长度" prop="length">
          <el-input
            v-model.number="polyAddForm.length"
            type="number"
            placeholder="请输入长度"
            clearable
            min="0"
            max="65535"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="必填" prop="require">
          <el-checkbox v-model="polyAddFormRequireChecked" />
        </el-form-item>
        <el-form-item label="可见策略" prop="policyCrudJson">
          <el-checkbox-group v-model="polyAddForm.policyCrudJson">
            <el-checkbox value="AD" label="增" />
            <el-checkbox value="ED" label="编" />
            <el-checkbox value="DV" label="详" />
            <el-checkbox value="LD" label="查" />
            <el-checkbox value="LV" label="列" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="查询策略" prop="policyQuery">
          <el-select v-model="polyAddForm.policyQuery" :disabled="true">
            <el-option :value="0" label="等于" />
            <el-option :value="1" label="模糊" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示策略" prop="policyView">
          <el-select v-model="polyAddForm.policyView">
            <el-option :value="0" label="文本框" />
            <el-option :value="1" label="文本域" />
            <el-option :value="2" label="下拉" />
            <el-option :value="3" label="单选" />
            <el-option :value="4" label="多选" />
            <el-option :value="5" label="LD" />
            <el-option :value="6" label="LDT" />
          </el-select>
        </el-form-item>
        <el-form-item label="字段备注" prop="remark">
          <el-input v-model="polyAddForm.remark" placeholder="请输入字段备注" clearable maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="主键" prop="pk">
          <el-checkbox :model-value="false" :disabled="true" />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="polyAddForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="polyAddModalVisible = false">取消</el-button>
          <el-button type="primary" :loading="polyAddLoading" @click="submitPolyAdd">创建</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import type { FormInstance } from "element-plus";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import OpSchemaDesignService from "@/views/assembly/service/OpSchemaDesignService";
import type { GetPolyModelListVo } from "@/views/assembly/api/PolyModelApi";
import type { GetOpSchemaListVo } from "@/views/assembly/api/OpSchemaApi";
import type { GetPolyTemplateListVo } from "@/views/assembly/api/PolyTemplateApi";
import PolyTemplateApi from "@/views/assembly/api/PolyTemplateApi";
import ComIconService from "@/soa/com-series/service/ComIconService";

const { resolveIcon } = ComIconService.useIconService();

const DeleteIcon = resolveIcon("delete");
const MagicStickIcon = resolveIcon("magic-stick");
const CloseIcon = resolveIcon("fontisto:close");
const AddFieldIcon = resolveIcon("zondicons:add-outline");
const PkIcon = resolveIcon("fxemoji:key");
const PreviewIcon = resolveIcon("view");

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

const { cdrcCanReturn, cdrcReturn, getCdrcQuery, cdrcRedirect } = ComDirectRouteContext.useDirectRouteContext();

const schemaInfo = getCdrcQuery() as GetOpSchemaListVo;
const outputSchemaId = ref(schemaInfo?.id ?? "");

//列表视图模式 poly: 聚合模型, origin: 原始模型
const listViewModel = ref<"raw" | "poly">("poly");

//聚合模型列表打包
const {
  listData: polyListData,
  listLoading: polyListLoading,
  loadList: loadPolyList,
  removeList: removePolyList,
  importFromRaw,
} = OpSchemaDesignService.usePolyModelList(outputSchemaId);

//聚合模板下拉列表
const importTemplateId = ref<string>("");
const importTemplateOptions = ref<GetPolyTemplateListVo[]>([]);
const importTemplateLoading = ref(false);

const loadImportTemplates = async (): Promise<void> => {
  if (importTemplateOptions.value.length > 0) {
    return;
  }
  importTemplateLoading.value = true;
  try {
    const result = await PolyTemplateApi.getPolyTemplateList({ pageNum: 1, pageSize: 10000 });
    if (result.code === 0) {
      importTemplateOptions.value = result.data;
    }
  } catch {
    // ignore
  }
  importTemplateLoading.value = false;
};

//原始模型列表打包
const {
  listData: originListData,
  listLoading: originListLoading,
  loadList: loadOriginList,
  syncRawModelFromDataSource,
} = OpSchemaDesignService.useRawModelList(outputSchemaId);

//监听列表视图模式变化
watch(listViewModel, (newVal) => {
  if (newVal === "raw") {
    loadOriginList();
  }
  if (newVal === "poly") {
    loadPolyList();
  }
});

// ==================== 单元格内联编辑 ====================

const editingCellKey = ref("");

const { submitRow, commitField } = OpSchemaDesignService.usePolyModelCellEdit();

const buildCellKey = (rowId: string, field: string): string => `${rowId}_${field}`;

const activateCell = (rowId: string, field: string): void => {
  editingCellKey.value = buildCellKey(rowId, field);
};

const clearEditingCell = (): void => {
  editingCellKey.value = "";
};

const isEditingCell = (rowId: string, field: string): boolean => editingCellKey.value === buildCellKey(rowId, field);

const submitCell = async (row: GetPolyModelListVo, field: string): Promise<void> => {
  const success = await submitRow(row);
  if (!success) {
    return;
  }
  if (!isEditingCell(row.id, field)) {
    return;
  }
  clearEditingCell();
  if (field === "seq") {
    await loadPolyList();
  }
};

const submitField = async (row: GetPolyModelListVo, field: string, value: any): Promise<void> => {
  const success = await commitField(row, field, value);
  if (!success) {
    return;
  }
  clearEditingCell();
};

const onPolicyCrudVisibleChange = async (row: GetPolyModelListVo, visible: boolean): Promise<void> => {
  if (visible) {
    return;
  }
  await submitCell(row, "policyCrudJson");
};

const toggleRequire = async (row: GetPolyModelListVo): Promise<void> => {
  const nextValue = row.require === 1 ? 0 : 1;
  await submitField(row, "require", nextValue);
};

const formatPolicyQuery = (value: number): string => POLICY_QUERY_LABEL_MAP[value] ?? "-";
const formatPolicyView = (value: number): string => POLICY_VIEW_LABEL_MAP[value] ?? "-";

// ==================== 新增模态框 ====================

const polyAddFormRef = ref<FormInstance>();

const {
  modalVisible: polyAddModalVisible,
  modalLoading: polyAddLoading,
  modalForm: polyAddForm,
  modalRequireChecked: polyAddFormRequireChecked,
  modalRules: polyAddRules,
  openModal: openPolyAddModal,
  resetModal: resetPolyAddModal,
  submitModal: submitPolyAdd,
} = OpSchemaDesignService.usePolyModelAddModal(outputSchemaId, polyAddFormRef, loadPolyList);

const goToPreview = (): void => {
  cdrcRedirect("op-schema-preview", schemaInfo);
};

const goToList = (): void => {
  cdrcRedirect("op-schema-manager", schemaInfo);
};

onMounted(() => {
  loadPolyList();
});
</script>

<style scoped>
.std-list-container {
  padding-top: 0;
}

.schema-info-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 0;
  border: 1px solid #e4e7ed;
  border-top: 2px solid var(--el-color-primary);
  flex-shrink: 0;
}

.schema-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 20px;
}

.schema-info-label {
  font-size: 11px;
  color: #909399;
  letter-spacing: 0.5px;
  height: 25px;
}

.schema-info-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.schema-info-divider {
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

.editable-cell-center {
  text-align: center;
}

.editable-cell-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.action-bar {
  display: flex;
  margin-bottom: 8px;
}

.action-bar :deep(.el-button.is-link) {
  padding: 8px 16px;
  border-radius: 0;
  transition:
    background-color 0.2s,
    color 0.2s;
}

.action-bar :deep(.el-button.is-link:hover) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.action-bar :deep(.el-button--success.is-link:hover) {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.action-bar :deep(.el-button--danger.is-link:hover) {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}
</style>
