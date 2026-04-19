<template>
  <el-form-item label="候选启动用户">
    <el-input v-model="form.candidateStarterUsers" placeholder="逗号分隔，支持 EL 表达式" @change="onProcessOtherCommit" />
  </el-form-item>
  <el-form-item label="候选启动组">
    <el-input v-model="form.candidateStarterGroups" placeholder="逗号分隔，支持 EL 表达式" @change="onProcessOtherCommit" />
  </el-form-item>
  <el-form-item label="历史保留时间">
    <el-input v-model="form.historyTimeToLive" placeholder="ISO 8601 / 整数天，如 P30D" @change="onProcessOtherCommit" />
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelProcessService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelProcessService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onProcessOtherCommit } = QfdPanelProcessService.useQfdPanelProcess(
  () => props.modeler,
  () => props.element
);
</script>
