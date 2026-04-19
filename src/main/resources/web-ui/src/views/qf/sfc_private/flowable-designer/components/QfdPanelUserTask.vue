<template>
  <el-form-item label="到期时间">
    <el-input v-model="form.dueDate" @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="跟进时间">
    <el-input v-model="form.followUpDate" @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="优先级">
    <el-input v-model="form.priority" @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="跳过表达式">
    <el-input v-model="form.skipExpression" placeholder="Flowable skipExpression" @change="onOtherCommit" />
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelUserTaskService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelUserTaskService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onTaskCommit, onOtherCommit } = QfdPanelUserTaskService.useQfdPanelUserTask(
  () => props.modeler,
  () => props.element
);
</script>
