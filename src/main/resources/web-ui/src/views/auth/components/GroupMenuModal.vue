<template>
  <el-dialog
    :model-value="props.visible"
    :title="'管理菜单 - ' + (props.data?.name ?? '')"
    width="720px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="onClose"
  >
    <div class="warn-bar">
      <el-icon class="warn-icon"><WarningFilled /></el-icon>
      <span>修改此用户组的菜单绑定将立即对所有已登录的组内用户生效</span>
    </div>

    <div class="batch-bar">
      <el-button size="small" @click="svc.selectAll">全选</el-button>
      <el-button size="small" @click="svc.deselectAll">取消全选</el-button>
      <span class="cascade-switch">
        <el-switch v-model="svc.modalCascadeCheck.value" size="small" />
        <span class="cascade-label">级联变更</span>
      </span>
    </div>

    <div class="tree-wrap">
      <StdAdvTree
        :ref="
          (el: any) => {
            svc.modalTreeRef.value = el;
          }
        "
        v-model:checked-nks="modalCheckedKeys"
        v-model:checked-half-nks="modalHalfCheckedKeys"
        :data="svc.modalTreeData.value"
        :check="true"
        :check-cascade="svc.modalCascadeCheck.value"
        :check-multiple="true"
        :check-on-node-click="true"
        :loading="svc.modalLoading.value"
        nk="id"
        nt="name"
        nc="children"
        ni="icon"
        search
        search-cascade
        search-placeholder="请输入菜单名称"
        :expand-on-click="true"
      >
        <template #label="{ data: nodeData }">
          <span v-if="nodeData.hide == 0">{{ nodeData.name }}</span>
          <span v-if="nodeData.hide == 1" class="label-hidden">{{ nodeData.name }}</span>
        </template>
        <template #append="{ data }">
          <el-tag v-if="data.kind === 0" size="small" type="info" class="kind-tag">目录</el-tag>
          <el-tag v-if="data.kind === 1" size="small" type="success" class="kind-tag">菜单</el-tag>
          <el-tag v-if="data.kind === 2" size="small" class="kind-tag">按钮</el-tag>
        </template>
      </StdAdvTree>
    </div>

    <template #footer>
      <el-button @click="onClose">关闭</el-button>
      <el-button type="primary" :loading="svc.modalLoading.value" @click="svc.submitModal">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { WarningFilled } from "@element-plus/icons-vue";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";
import GroupMenuModalService, { type GroupMenuModalProps } from "@/views/auth/components/service/GroupMenuModalService.ts";

const props = defineProps<GroupMenuModalProps>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "success"): void;
}>();

const svc = GroupMenuModalService.useGroupMenuModal(props, emit);

const { modalCheckedKeys, modalHalfCheckedKeys } = svc;

const onClose = (): void => {
  emit("close");
};
</script>

<style scoped>
.tip-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 7px 10px;
  margin-bottom: 12px;
}

.tip-icon {
  font-size: 14px;
  flex-shrink: 0;
  color: var(--el-color-primary);
}

.warn-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-color-warning-light-9);
  border-radius: 4px;
  padding: 7px 10px;
  margin-bottom: 12px;
}

.warn-icon {
  font-size: 14px;
  flex-shrink: 0;
  color: var(--el-color-warning);
}

.tree-wrap {
  height: 520px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.label-hidden {
  text-decoration: line-through;
  color: var(--el-text-color-placeholder);
}

.kind-tag {
  margin-left: 6px;
  flex-shrink: 0;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.cascade-switch {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 16px;
}

.cascade-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}
</style>
