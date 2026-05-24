<!--
  * 组织机构选择器
  * 这只是OrgTree的一层包装，使其支持模态框。
  * 所有属性都会透传给OrgTree。
-->
<template>
  <el-dialog
    v-model="bindModalVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="core-org-dept-select-modal"
  >
    <div class="modal-body">
      <div class="tree-container">
        <OrgTree
          v-model:checked-nks="draftCheckedOrgIds"
          :check="true"
          :check-multiple="mode === 'multiple'"
          :check-cascade="checkCascade"
          :show-kind-tag="false"
          :crop-org-id="cropOrgId"
          :readonly="readonly"
          :search="true"
          search-placeholder="请输入组织机构"
          :search-cascade="true"
          :search-fields="['name']"
          :exclude-node-method="excludeNodeMethod"
          :check-enable-method="checkEnableMethod"
          v-bind="$attrs"
        />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button :loading="btnLoading" :disabled="false" @click="onModalClose">关闭</el-button>
        <el-button
          v-if="!props.readonly"
          type="primary"
          :loading="btnLoading"
          :disabled="draftCheckedOrgIds.length < 1 || isOverMax"
          @click="onModalSubmit"
          >保存({{ draftCheckedOrgIds.length }})</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { ModalOrgTreeEmits, ModalOrgTreeProps } from "@/views/core/public/service/ModalOrgTreeService";
import ModalOrgTreeService from "@/views/core/public/service/ModalOrgTreeService";

/**
 * 模态组织机构选择器参数
 */
const props = withDefaults(defineProps<ModalOrgTreeProps>(), {
  title: "选择组织机构",
  width: "500px",
  mode: "single",
  readonly: false,
  max: null,
  checkCascade: false,
  excludeNodeMethod: undefined,
});

/**
 * 模态组织机构选择器事件发射器
 */
const emit = defineEmits<ModalOrgTreeEmits>();

//模态框显隐控制 外部用v-model绑定
const bindModalVisible = defineModel<boolean>({ default: false });

//已勾选组织机构ID数组 外部用v-model:checked-org-ids绑定
const bindCheckedOrgIds = defineModel<string[]>("checkedOrgIds", { default: () => [] });

//模态框组织机构选择器打包
const { draftCheckedOrgIds, isOverMax, btnLoading, onModalSubmit, onModalClose } = ModalOrgTreeService.useModalOrgTree(
  props,
  emit,
  bindModalVisible,
  bindCheckedOrgIds
);
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
