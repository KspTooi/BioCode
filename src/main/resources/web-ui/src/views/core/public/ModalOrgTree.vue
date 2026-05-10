<!--
  * 组织机构选择器
  * 这只是OrgTree的一层包装，使其支持模态框。
  * 所有属性都会透传给OrgTree。
-->
<template>
  <el-dialog
    v-model="modalVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="core-org-dept-select-modal"
    @close="onClose"
  >
    <div v-loading="loading" class="modal-body">
      <div class="tree-container">
        <OrgTree v-if="modalVisible" :check="false" v-model:checked-nks="modalCheckedOrgIds" ref="orgTreeRef" v-bind="$attrs" />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
        <el-button type="primary" @click="onSubmit" :disabled="modalCheckedOrgIds.length < 1"
          >保存 ({{ modalCheckedOrgIds.length }})</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from "vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

const props = withDefaults(
  defineProps<{
    title?: string;
    width?: string | number;
  }>(),
  {
    title: "选择组织机构",
    width: "450px",
  }
);

const emit = defineEmits<{
  (e: "on-submit", checkedOrgIds: string[]): void;
  (e: "on-close"): void;
}>();

const modalVisible = defineModel<boolean>("visible", { default: false });
const loading = ref(false);

//已勾选的组织机构ID列表
const modalCheckedOrgIds = defineModel<string[]>("checkedOrgIds", { default: () => [] });

const onSubmit = (): void => {
  modalVisible.value = false;
  emit("on-submit", modalCheckedOrgIds.value);
};

const onClose = (): void => {
  modalVisible.value = false;
  emit("on-close");
};
</script>

<style scoped>
.core-org-dept-select-modal :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.modal-body {
  height: 500px;
  display: flex;
  flex-direction: column;
  border-radius: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.tree-container {
  flex: 1;
  overflow: hidden;
  padding: 10px;
}

.dialog-footer {
  padding-top: 10px;
}

/* 直角风格适配 */
:deep(.el-dialog) {
  border-radius: 0;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: var(--el-text-color-primary);
}
</style>
