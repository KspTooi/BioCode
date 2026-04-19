<template>
  <el-form-item label="异步">
    <el-switch v-model="form.async" @change="onAsyncCommit" />
  </el-form-item>
  <el-form-item label="异步前">
    <el-switch v-model="form.asyncBefore" @change="onAsyncCommit" />
  </el-form-item>
  <el-form-item label="异步后">
    <el-switch v-model="form.asyncAfter" @change="onAsyncCommit" />
  </el-form-item>
  <el-form-item label="独占">
    <el-switch v-model="form.exclusive" @change="onAsyncCommit" />
  </el-form-item>
</template>

<script setup lang="ts">
import QfdPanelAsyncService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelAsyncService";

const props = defineProps<{
  modeler: unknown;
  element: unknown | null;
}>();

const { form, onAsyncCommit } = QfdPanelAsyncService.useQfdPanelAsync(
  () => props.modeler,
  () => props.element
);
</script>
