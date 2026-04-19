<template>
  <div class="multi-instance">
    <el-collapse class="mi-tip-collapse">
      <el-collapse-item name="1">
        <template #title>
          <div class="tip-title">
            <el-icon><InfoFilled /></el-icon>
            <span>后端开发提示（展开查看）</span>
          </div>
        </template>
        <div class="tip-content">
          <div class="tip-section">
            <div class="tip-section-title">1. 会签 / 或签（多实例）数据准备</div>
            <p>
              引擎执行多实例需依赖真实的 List 集合。请在节点到达前（如执行监听器），将前端保存的逗号分隔字符串转换为
              List，并注入流程变量：
            </p>
            <ul>
              <li><b>指定用户</b>：读取 <code>candidateUsers</code>，转为 List 存入 <code>assigneeList</code> 变量。</li>
              <li><b>组织机构 / 用户组</b>：读取 <code>candidateGroups</code>，转为 List 存入 <code>groupList</code> 变量。</li>
            </ul>
          </div>
          <div class="tip-section">
            <div class="tip-section-title">2. 审批人类型底层映射</div>
            <ul>
              <li><b>指定用户</b>：单人存为 <code>assignee="ID"</code>；多人存为 <code>candidateUsers="ID1,ID2"</code>。</li>
              <li>
                <b>组织机构 / 用户组</b>：均存为 <code>candidateGroups="ID1,ID2"</code>。可通过解析扩展属性
                <code>flowable:assigneeKind</code>（值为 <code>dept</code> 或 <code>group</code>）来区分。
              </li>
              <li>
                <b>发起人</b>：存为 <code>assignee="${initiator}"</code>。后端需在启动流程前调用
                <code>IdentityService.setAuthenticatedUserId(userId)</code>。
              </li>
            </ul>
          </div>
          <div class="tip-section">
            <div class="tip-section-title">3. 如何判断当前是会签还是或签？</div>
            <p>后端可通过解析 XML 中 <code>loopCharacteristics</code> 的 <code>completionCondition</code> 来判断：</p>
            <ul>
              <li><b>会签</b>：条件为 <code>${nrOfCompletedInstances == nrOfInstances}</code>（或无条件）。</li>
              <li><b>或签</b>：条件为 <code>${nrOfCompletedInstances &gt; 0}</code>。</li>
            </ul>
          </div>
          <div class="tip-section">
            <div class="tip-section-title">4. 如何在后端获取 flowable:assigneeKind？</div>
            <p>在 Java 中，可通过 <code>BpmnModel</code> 获取 UserTask 上的自定义扩展属性：</p>
            <pre><code>String kind = userTask.getAttributeValue("http://flowable.org/bpmn", "assigneeKind");</code></pre>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <el-form label-position="top" size="small" class="mi-form" @submit.prevent>
      <el-form-item label="审批人设置">
        <el-radio-group v-model="assigneeKind" class="full-width-radio" @change="onAssigneeKindChange">
          <el-radio-button value="user">指定用户</el-radio-button>
          <el-radio-button value="dept" disabled>组织机构</el-radio-button>
          <el-radio-button value="group">用户组</el-radio-button>
          <el-radio-button value="initiator">发起人</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <template v-if="assigneeKind === 'user'">
        <el-form-item>
          <div class="user-select-box">
            <div v-if="selectedUsers.length > 0" class="tag-row">
              <el-tag v-for="u in selectedUsers" :key="u.id" closable class="mi-tag" @close="removeUser(u.id)">
                {{ displayUser(u) }}
              </el-tag>
            </div>
            <el-button type="primary" plain class="add-btn" @click="openUserModal">
              {{ approvalMultiMode === "none" ? "+ 选择用户（仅限1人）" : "+ 添加用户" }}
            </el-button>
          </div>
        </el-form-item>
      </template>

      <template v-if="assigneeKind === 'dept'">
        <el-form-item>
          <el-tree-select
            v-model="selectedDeptId"
            :data="orgTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择组织机构"
            check-strictly
            filterable
            clearable
            style="width: 100%"
            @change="onSelectedDeptIdChange"
          />
        </el-form-item>
      </template>

      <template v-if="assigneeKind === 'group'">
        <el-form-item>
          <el-select
            v-model="selectedGroupIds"
            :multiple="approvalMultiMode !== 'none'"
            filterable
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
            :placeholder="approvalMultiMode === 'none' ? '请选择用户组（仅限1个）' : '请选择用户组'"
            @change="onSelectedGroupIdsChange"
          >
            <el-option v-for="g in groupOptions" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="多实例审批方式" class="mt-4">
        <el-radio-group v-model="approvalMultiMode" class="full-width-radio" @change="onApprovalMultiModeChange">
          <el-radio-button value="none">无</el-radio-button>
          <el-radio-button value="countersign">会签</el-radio-button>
          <el-radio-button value="orSign">或签</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <template v-if="approvalMultiMode === 'custom'">
        <div class="custom-box">
          <el-form-item label="循环类型">
            <el-select v-model="customLoop.mode" style="width: 100%" @change="commit">
              <el-option label="无" value="none" />
              <el-option label="并行多实例" value="parallel" />
              <el-option label="顺序多实例" value="sequential" />
            </el-select>
          </el-form-item>
          <template v-if="customLoop.mode !== 'none'">
            <el-form-item label="集合">
              <el-input v-model="customLoop.collection" placeholder="如 ${assigneeList}" @change="commit" />
            </el-form-item>
            <el-form-item label="元素变量">
              <el-input v-model="customLoop.elementVariable" placeholder="如 assignee" @change="commit" />
            </el-form-item>
            <el-form-item label="完成条件">
              <el-input
                v-model="customLoop.completionCondition"
                type="textarea"
                :rows="2"
                placeholder="如 ${nrOfCompletedInstances >= nrOfInstances}"
                @change="commit"
              />
            </el-form-item>
            <el-form-item label="循环基数">
              <el-input v-model="customLoop.loopCardinality" placeholder="可选" @change="commit" />
            </el-form-item>
          </template>
        </div>
      </template>
    </el-form>

    <CoreUserSelectModal
      v-model="userModalVisible"
      title="选择用户"
      :multiple="approvalMultiMode !== 'none'"
      :default-selected="defaultUserIds"
      @confirm="onUsersConfirmed"
    />
  </div>
</template>

<script setup lang="ts">
import { InfoFilled } from "@element-plus/icons-vue";
import CoreUserSelectModal from "@/views/core/components/public/CoreUserSelectModal.vue";
import { useMultiInstancePanel } from "@/views/qf/sfc_private/flowable-designer/deprecated/multiInstanceFlow.ts";

const props = defineProps<{
  modeler: unknown;
  element: unknown;
}>();

const {
  assigneeKind,
  approvalMultiMode,
  selectedUsers,
  selectedGroupIds,
  groupOptions,
  orgTreeOptions,
  selectedDeptId,
  customLoop,
  userModalVisible,
  defaultUserIds,
  onAssigneeKindChange,
  onApprovalMultiModeChange,
  openUserModal,
  onUsersConfirmed,
  onSelectedGroupIdsChange,
  onSelectedDeptIdChange,
  removeUser,
  commit,
  displayUser,
} = useMultiInstancePanel(props);
</script>

<style scoped>
.multi-instance {
  max-width: 100%;
}
.mi-tip-collapse {
  margin-bottom: 16px;
  border-top: none;
  border-bottom: none;
}
.mi-tip-collapse :deep(.el-collapse-item__header) {
  background-color: var(--el-color-info-light-9);
  border-radius: 6px;
  height: 36px;
  line-height: 36px;
  padding: 0 12px;
  border-bottom: none;
}
.mi-tip-collapse :deep(.el-collapse-item__wrap) {
  background-color: var(--el-color-info-light-9);
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
  border-bottom: none;
  padding: 0 12px;
}
.mi-tip-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 12px;
}
.tip-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--el-color-info);
  font-size: 13px;
}
.tip-content {
  font-size: 12px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}
.tip-section {
  margin-bottom: 12px;
}
.tip-section:last-child {
  margin-bottom: 0;
}
.tip-section-title {
  font-weight: bold;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}
.tip-content p {
  margin: 4px 0;
}
.tip-content ul {
  margin: 4px 0 0 0;
  padding-left: 20px;
}
.tip-content pre {
  margin: 4px 0;
  background-color: var(--el-color-info-light-8);
  padding: 6px;
  border-radius: 4px;
  overflow-x: auto;
}
.tip-content pre code {
  background-color: transparent;
  padding: 0;
  color: var(--el-text-color-regular);
}
.tip-content code {
  font-size: 12px;
  background-color: var(--el-color-info-light-8);
  padding: 2px 4px;
  border-radius: 4px;
  color: var(--el-color-danger);
}
.mi-form {
  padding: 0 4px;
}
.full-width-radio {
  display: flex;
  width: 100%;
}
.full-width-radio :deep(.el-radio-button) {
  flex: 1;
}
.full-width-radio :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 8px 0;
}
.user-select-box {
  width: 100%;
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
  padding: 8px;
  min-height: 32px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.mi-tag {
  margin: 0;
}
.add-btn {
  width: 100%;
  border-style: dashed;
}
.custom-box {
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  padding: 12px 12px 4px 12px;
  margin-top: 8px;
}
.mt-4 {
  margin-top: 16px;
}
</style>
