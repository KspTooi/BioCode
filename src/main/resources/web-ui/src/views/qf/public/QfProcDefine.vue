<template>
  <div class="qf-proc-define">
    <div v-if="loading" class="qf-proc-define__loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-if="!loading && nodes.length === 0" class="qf-proc-define__empty">
      <el-empty description="暂无审批节点" :image-size="60" />
    </div>

    <div v-if="!loading && nodes.length > 0" class="qf-proc-define__list">
      <template v-for="(node, index) in nodes" :key="node.nodeId">
        <!-- 连接线 (仅在非第一个节点前显示) -->
        <div v-if="index > 0" class="qf-proc-define__connector">
          <div class="qf-proc-define__connector-line" />
          <el-icon class="qf-proc-define__connector-arrow"><ArrowDown /></el-icon>
        </div>

        <!-- 审批节点 -->
        <div class="qf-proc-define__node">
          <div class="qf-proc-define__node-icon">
            <span class="qf-proc-define__node-index">{{ index + 1 }}</span>
          </div>
          <div class="qf-proc-define__node-content">
            <div class="qf-proc-define__node-title">{{ node.nodeName }}</div>
            <div class="qf-proc-define__members">
              <span v-if="node.memberKind === 3" class="qf-proc-define__members-count">1人处理</span>
              <span v-else-if="node.aprKind === 0 && node.memberNames.length > 0" class="qf-proc-define__members-count"
                >{{ node.memberNames.length }}人处理</span
              >
              <span v-else-if="node.aprKind === 1" class="qf-proc-define__members-count"> 需发起时指定 </span>
            </div>
          </div>
          <div class="qf-proc-define__node-right">
            <div v-if="node.memberKind === 3" class="qf-proc-define__right-tags">
              <el-tag size="small" type="primary" effect="plain" class="qf-proc-define__member-tag">
                {{ currentUserDisplayName }}
              </el-tag>
            </div>
            <div v-else-if="node.aprKind === 0 && node.memberNames.length > 0" class="qf-proc-define__right-tags">
              <el-tag
                v-for="name in node.memberNames"
                :key="name"
                size="small"
                type="primary"
                effect="plain"
                class="qf-proc-define__member-tag"
              >
                {{ name }}
              </el-tag>
            </div>
            <div v-else-if="node.aprKind === 1" class="qf-proc-define__right-tags">
              <div v-if="getSelectedNames(node).length > 0" class="qf-proc-define__right-tags">
                <el-tag
                  v-for="name in getSelectedNames(node)"
                  :key="name"
                  size="small"
                  type="success"
                  effect="plain"
                  class="qf-proc-define__member-tag"
                >
                  {{ name }}
                </el-tag>
                <el-button size="small" type="primary" link @click="onSelectUser(node)">修改</el-button>
              </div>
              <el-button v-else size="small" type="primary" @click="onSelectUser(node)">选择人员</el-button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 用户选择器模态框 -->
    <ModalUserSelector
      v-model="userSelectorVisible"
      v-model:checked-user-ids="selectedUserIds"
      mode="single"
      title="选择处理人"
      @on-submit-entity="onUserSelected"
    />
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, Loading } from "@element-plus/icons-vue";
import QfProcDefineService, { type QfProcDefineProps, type QfProcDefineEmits } from "@/views/qf/public/service/QfProcDefine.ts";
import type { LaunchMemberParamDto } from "@/views/qf/api/QfProcApi.ts";
import ModalUserSelector from "@/views/core/public/ModalUserSelector.vue";

const props = defineProps<QfProcDefineProps>();

const emits = defineEmits<QfProcDefineEmits>();

//启动成员参数 外部通过v-model绑定，用于发起流程时传递启动成员参数
const bindLmp = defineModel<LaunchMemberParamDto[]>({ default: () => [] });

const {
  nodes,
  loading,
  currentUserDisplayName,
  userSelectorVisible,
  selectedUserIds,
  onSelectUser,
  onUserSelected,
  getSelectedNames,
} = QfProcDefineService.useQfProcDefine(props, emits, bindLmp);
</script>

<style scoped>
.qf-proc-define {
  padding: 16px;
  min-width: 240px;
}

.qf-proc-define__loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-color-info);
  padding: 24px 0;
  justify-content: center;
}

.qf-proc-define__empty {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.qf-proc-define__list {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qf-proc-define__connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}

.qf-proc-define__connector-line {
  width: 2px;
  height: 16px;
  background-color: var(--el-border-color);
}

.qf-proc-define__connector-arrow {
  color: var(--el-border-color);
  font-size: 14px;
}

.qf-proc-define__node {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 12px 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.qf-proc-define__node-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.qf-proc-define__node-index {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.qf-proc-define__node-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.qf-proc-define__node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.qf-proc-define__members {
  display: flex;
  align-items: center;
}

.qf-proc-define__members-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.qf-proc-define__node-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
  margin-left: 12px;
}

.qf-proc-define__right-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: flex-end;
  max-width: 200px;
}

.qf-proc-define__member-tag {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
