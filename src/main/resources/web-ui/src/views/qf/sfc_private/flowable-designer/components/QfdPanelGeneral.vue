<template>
  <el-form-item label="类型">
    <el-input :model-value="elementType" disabled />
  </el-form-item>
  <el-form-item label="ID">
    <el-input v-model="form.id" @change="onIdCommit" />
  </el-form-item>
  <el-form-item label="名称">
    <el-input v-model="form.name" @change="onNameCommit" />
  </el-form-item>
  <template v-if="elementType === 'bpmn:Process'">
    <el-form-item label="版本标签">
      <el-input v-model="form.versionTag" @change="onProcessGeneralCommit" />
    </el-form-item>
    <el-form-item label="可执行">
      <el-switch v-model="form.isExecutable" @change="onProcessGeneralCommit" />
    </el-form-item>
  </template>
</template>

<script setup lang="ts">
import QfdPanelGeneralService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelGeneralService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, elementType, onIdCommit, onNameCommit, onProcessGeneralCommit } = QfdPanelGeneralService.useQfdPanelGeneral(
  () => props.modeler,
  () => props.element
);
</script>
