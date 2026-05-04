<template>
  <el-dialog
    :model-value="props.visible"
    :title="'管理菜单 - ' + (props.data?.name ?? '')"
    width="720px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="onClose"
  >
    <div class="tip-bar">
      <el-icon class="tip-icon"><InfoFilled /></el-icon>
      <span>勾选后点击保存生效，父级目录会随子项一同绑定</span>
    </div>

    <div class="tree-wrap">
      <StdAdvTree
        ref="svc.treeRef"
        :data="svc.treeData.value"
        :check="true"
        :check-cascade="true"
        :check-multiple="true"
        :check-on-node-click="true"
        :model-value-check="svc.checkedKeys.value"
        :loading="svc.loading.value"
        nk="id"
        nt="name"
        nc="children"
        ni="icon"
        search
        search-placeholder="请输入菜单名称"
        :expand-on-click="true"
        @update:model-value-check="svc.onCheckedKeysChange"
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
      <div class="dialog-footer">
        <el-button type="primary" :loading="svc.submitting.value" @click="svc.submit">保存</el-button>
        <el-button @click="onClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { InfoFilled } from "@element-plus/icons-vue";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";
import GroupMenuModalService, { type GroupMenuModalProps } from "@/views/auth/components/service/GroupMenuModalService.ts";

const props = defineProps<GroupMenuModalProps>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "success"): void;
}>();

const svc = GroupMenuModalService.useGroupMenuModal(props, emit);

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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
