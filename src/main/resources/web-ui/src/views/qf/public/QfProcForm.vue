<template>
  <div class="qf-proc-form-root">
    <div v-loading="props.loading" class="form-scroll-area">
      <component
        :is="formComponent"
        v-if="formComponent && props.details?.dataId"
        :id="props.details.dataId"
        :proc-context="props.procContext"
        mode="view"
      />
      <el-empty v-if="!formComponent && !props.loading" description="流程表单组件未发布或已下线，请联系管理员处理。" />
    </div>
  </div>
</template>

<script setup lang="ts">
import QfProcFormService, { type QfProcFormProps } from "@/views/qf/public/service/QfProcFormService";

const props = defineProps<QfProcFormProps>();

const { formComponent } = QfProcFormService.useQfProcForm(props);
</script>

<style scoped>
.qf-proc-form-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.form-scroll-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 8px;
}

.form-scroll-area :deep(.form_container) {
  max-height: none !important;
  overflow: visible !important;
  padding-right: 0 !important;
}
</style>
