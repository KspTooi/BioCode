<template>
  <el-form-item label="条件模板">
    <el-select v-model="form.conditionPreset" style="width: 100%" @change="onConditionPresetChange">
      <el-option label="无(无条件直达)" value="none" />
      <el-option label="同意 (approved == true)" value="approve" />
      <el-option label="驳回 (approved == false)" value="reject" />
      <el-option label="自定义" value="custom" />
    </el-select>
  </el-form-item>
  <el-form-item label="条件表达式">
    <el-input
      v-model="form.conditionExpression"
      type="textarea"
      :rows="2"
      :disabled="form.conditionPreset !== 'custom'"
      placeholder="${approved == true}"
      @change="commitCondition"
    />
  </el-form-item>
  <el-form-item label="默认流">
    <div class="qfd-flow-default-row">
      <el-switch v-model="form.isDefault" @change="commitDefault" />
      <span class="qfd-flow-hint">上游网关找不到匹配条件时走此分支</span>
    </div>
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelSequenceFlowService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelSequenceFlowService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onConditionPresetChange, commitCondition, commitDefault } = QfdPanelSequenceFlowService.useQfdPanelSequenceFlow(
  () => props.modeler,
  () => props.element
);
</script>

<style scoped>
.qfd-flow-default-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.qfd-flow-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
