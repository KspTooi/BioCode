<template>
  <el-dialog
    :model-value="props.visible"
    :title="'高级权限操作(GP) - ' + (props.data?.name ?? '')"
    width="640px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="onClose"
  >
    <div class="warn-bar">
      <el-icon class="warn-icon"><WarningFilled /></el-icon>
      <span>修改此用户组的权限绑定将立即对所有已登录的组内用户生效</span>
    </div>

    <div class="info-bar">
      <el-icon class="info-icon"><InfoFilled /></el-icon>
      <span>用户的实际权限 = GM 派生权限（菜单绑定） + GP 直接权限（本页操作）。GP 用于对特定权限码做例外增删，常规授权请走 GM。</span>
    </div>

    <div class="permission-wrapper" v-loading="svc.modalLoading.value">
      <div class="permission-header">
        <el-input v-model="svc.modalSearch.value" placeholder="搜索权限码/名称" clearable size="small">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="permission-header-meta">
          <span class="meta-count">已选 {{ svc.modalSelectedCount.value }} 项</span>
          <div class="meta-actions">
            <el-button type="primary" size="small" link @click="svc.selectAll">全选</el-button>
            <el-button type="primary" size="small" link @click="svc.deselectAll">清空</el-button>
          </div>
        </div>
      </div>

      <div class="permission-body">
        <el-checkbox-group v-model="svc.modalSelectedIds.value">
          <div v-for="perm in svc.modalFilteredPermissions.value" :key="perm.id" class="perm-row">
            <el-checkbox :value="perm.id" :disabled="svc.isSystemGroup.value && perm.code === '*:*:*'">
              <div class="perm-info">
                <div class="perm-name">{{ perm.name }}</div>
                <div class="perm-code">{{ perm.code }}</div>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="svc.modalFilteredPermissions.value.length === 0" :image-size="60" description="无匹配权限" />
      </div>
    </div>

    <template #footer>
      <el-button @click="onClose">关闭</el-button>
      <el-button type="primary" :loading="svc.modalLoading.value" @click="svc.submitModal">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { InfoFilled, Search, WarningFilled } from "@element-plus/icons-vue";
import GroupGpModalService, { type GroupGpModalProps } from "@/views/auth/components/service/GroupGpModalService.ts";

const props = defineProps<GroupGpModalProps>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "success"): void;
}>();

const svc = GroupGpModalService.useGroupGpModal(props, emit);

const onClose = (): void => {
  emit("close");
};
</script>

<style scoped>
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

.info-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-color-info-light-9);
  border-radius: 4px;
  padding: 7px 10px;
  margin-bottom: 12px;
}

.info-icon {
  font-size: 14px;
  flex-shrink: 0;
  color: var(--el-color-info);
}

.permission-wrapper {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.permission-header {
  padding: 10px;
  background-color: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.permission-header-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.meta-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-actions {
  display: flex;
  gap: 4px;
}

.permission-body {
  height: 420px;
  overflow-y: auto;
  padding: 10px;
}

.perm-row {
  padding: 6px 0;
  border-bottom: 1px dashed var(--el-border-color-extra-light);
}

.perm-row:last-child {
  border-bottom: none;
}

.perm-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
  margin-left: 4px;
}

.perm-name {
  font-size: 13px;
  color: var(--el-text-color-primary);
}

.perm-code {
  font-size: 11px;
  font-family: monospace;
  color: var(--el-text-color-secondary);
}

:deep(.el-checkbox) {
  height: auto;
  display: flex;
  align-items: center;
}

:deep(.el-checkbox__label) {
  padding-left: 0;
}
</style>
