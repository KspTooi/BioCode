<template>
  <el-form-item label="表单 Key">
    <el-input v-model="form.formKey" @change="onStartEventCommit" />
  </el-form-item>
  <el-form-item label="发起人变量">
    <el-input v-model="form.initiator" @change="onStartEventCommit" />
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelStartEventService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelStartEventService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onStartEventCommit } = QfdPanelStartEventService.useQfdPanelStartEvent(
  () => props.modeler,
  () => props.element
);
</script>
