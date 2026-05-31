<template>
  <el-form-item label="到期时间">
    <el-input v-model="form.dueDate" placeholder="不支持" disabled @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="跟进时间">
    <el-input v-model="form.followUpDate" placeholder="不支持" disabled @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="优先级">
    <el-input v-model="form.priority" placeholder="不支持" disabled @change="onTaskCommit" />
  </el-form-item>
  <el-form-item label="跳过表达式">
    <el-input v-model="form.skipExpression" :disabled="true" placeholder="Flowable skipExpression" @change="onOtherCommit" />
  </el-form-item>
  <el-form-item label="发起时跳过">
    <el-switch v-model="form.utGeInitSkip" active-value="1" inactive-value="0" @change="onInitSkipCommit" />
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelUserTaskService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelUserTaskService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onTaskCommit, onOtherCommit, onInitSkipCommit } = QfdPanelUserTaskService.useQfdPanelUserTask(
  () => props.modeler,
  () => props.element
);
</script>
