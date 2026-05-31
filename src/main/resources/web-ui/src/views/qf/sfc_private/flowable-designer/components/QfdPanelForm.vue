<template>
  <div v-loading="fieldLoading" class="qfd-panel-form">
    <el-alert v-if="!formId" type="info" :closable="false" show-icon title="流程未绑定业务表单，无法配置可编辑字段" />
    <template v-else-if="!fieldLoading && fieldList.length === 0">
      <el-alert type="warning" :closable="false" show-icon title="当前业务表单暂无字段，请在表单字段管理中维护表单字段。" />
    </template>
    <template v-else>
      <div class="qfd-panel-form__toolbar">
        <span class="qfd-panel-form__hint">勾选审批时可编辑的字段</span>
        <div class="qfd-panel-form__actions">
          <el-button type="primary" link size="small" @click="selectAllFields">全选</el-button>
          <el-button type="danger" link size="small" @click="clearAllFields">清空</el-button>
        </div>
      </div>
      <el-table :data="fieldList" size="small" border stripe empty-text="暂无表单字段">
        <el-table-column type="index" label="序号" width="56" />
        <el-table-column prop="fieldName" label="字段名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
        <el-table-column label="可编辑" width="72" align="center" fixed="right">
          <template #default="{ row }">
            <el-checkbox
              :model-value="isFieldEditable(row.fieldName)"
              @change="(val: boolean) => toggleFieldEditable(row.fieldName, val)"
            />
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup lang="ts">
import QfdPanelFormService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelFormService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
  formId?: string | null;
}>();

const { fieldList, fieldLoading, selectAllFields, clearAllFields, isFieldEditable, toggleFieldEditable } =
  QfdPanelFormService.useQfdPanelForm(
    () => props.modeler,
    () => props.element,
    () => props.formId
  );
</script>

<style scoped>
.qfd-panel-form {
  max-width: 100%;
}
.qfd-panel-form__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 8px;
}
.qfd-panel-form__hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.qfd-panel-form__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>
