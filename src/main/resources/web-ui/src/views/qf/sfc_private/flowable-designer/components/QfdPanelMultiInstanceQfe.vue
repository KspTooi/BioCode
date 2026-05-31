<template>
  <div class="qfd-multi-instance-qfe">
    <el-form label-position="top" size="small" class="mi-form" @submit.prevent>
      <el-form-item>
        <template #label>
          <span class="mi-form-item-label">
            <el-icon><UserIcon /></el-icon>
            <span>处理人类型</span>
          </span>
        </template>
        <el-radio-group v-model="panelForm.utAprKind" class="full-width-radio" @change="onAprKindChanged">
          <el-radio-button v-for="opt in fd.utAprKind" :key="opt.v" :value="opt.v">{{ opt.l }}</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <template #label>
          <span class="mi-form-item-label">
            <el-icon><SettingIcon /></el-icon>
            <span>处理人配置</span>
          </span>
        </template>
        <el-radio-group
          v-model="panelForm.utAprMemberKind"
          class="full-width-radio member-kind-radio"
          @change="onMemberKindChanged"
        >
          <el-radio-button
            v-for="opt in panelForm.utAprKind === '0' ? fd.utAprMemberKindStandard : fd.utAprMemberKindInit"
            :key="`${opt.v}-${panelForm.utAprKind}`"
            :value="opt.v"
            :disabled="opt.d"
          >
            {{ opt.l }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 人员选择器 -->
      <template v-if="panelForm.utAprMemberKind !== '3'">
        <template v-if="panelForm.utAprMemberKind === '0'">
          <el-form-item>
            <InputUserSelector
              v-model="draftMemberIds"
              v-model:checked-user-names="panelForm.utAprMemberNames"
              :mode="multiMode ? 'multiple' : 'single'"
              :placeholder="multiMode ? '请选择用户(最多16人)' : '请选择用户(仅限1人)'"
              :max="16"
              :title="multiMode ? '选择审批人(最多16人)' : '选择审批人(仅限1人)'"
              @on-submit-entity="onUserSelected"
            />
          </el-form-item>
        </template>

        <!-- 组织机构 -->
        <template v-if="panelForm.utAprMemberKind === '2'">
          <el-form-item> </el-form-item>
        </template>

        <!-- 用户组 -->
        <template v-if="panelForm.utAprMemberKind === '1'">
          <el-form-item>
            <el-select
              v-model="draftMemberIds"
              :multiple="multiMode"
              filterable
              collapse-tags
              collapse-tags-tooltip
              style="width: 100%"
              :placeholder="multiMode ? '请选择用户组(最多10个)' : '请选择用户组(仅限1个)'"
              :max="10"
              @change="onGroupSelected"
            >
              <el-option v-for="g in groupList" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
        </template>
      </template>

      <el-form-item v-show="panelForm.utAprKind === '0' && panelForm.utAprMemberKind !== '3'" class="mt-4">
        <template #label>
          <span class="mi-form-item-label">
            <el-icon><GridIcon /></el-icon>
            <span>多实例方式</span>
          </span>
        </template>
        <el-radio-group v-model="panelForm.utAprMi" class="full-width-radio mi-radio" @change="onMiChanged">
          <el-radio-button v-for="opt in fd.utAprMiStandard" :key="opt.v" :value="opt.v" :disabled="opt.d">
            {{ opt.l }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-show="panelForm.utAprMi !== '0'">
        <template #label>
          <span class="mi-form-item-label">
            <el-icon><EditPenIcon /></el-icon>
            <span>多实例表达式</span>
          </span>
        </template>
        <el-input
          v-model="panelForm.utAprMiExpress"
          :disabled="panelForm.utAprMi !== '3'"
          type="textarea"
          rows="1"
          :autosize="{ minRows: 1, maxRows: 6 }"
          placeholder="暂无表达式"
        />
      </el-form-item>

      <el-form-item>
        <template #label>
          <div class="mi-action-head">
            <span class="mi-form-item-label">
              <el-icon><SettingIcon /></el-icon>
              <span>处理操作</span>
            </span>
            <el-button class="ml-2" type="primary" link size="small" @click="openModal('add')">+ 添加</el-button>
          </div>
        </template>

        <el-table :data="actionList" size="small" border stripe empty-text="暂无处理操作">
          <el-table-column type="index" label="序号" width="56" />
          <el-table-column label="操作类型" min-width="88">
            <template #default="{ row }">
              {{ actionTypeLabel(row.utAprAction) }}
            </template>
          </el-table-column>
          <el-table-column prop="utAprActionName" label="显示名称" min-width="88" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openModal('edit', row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="removeAction(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-checkbox v-model="panelForm.utAprComment" class="mi-action-comment mt-2" true-value="1" false-value="0">
          允许填写审批意见
        </el-checkbox>
      </el-form-item>

      <el-dialog
        v-model="modalVisible"
        :title="modalMode === 'add' ? '添加处理操作' : '编辑处理操作'"
        width="400px"
        destroy-on-close
        @closed="resetModal"
      >
        <el-form ref="modalFormRef" :model="modalForm" :rules="modalRules" label-width="80px" size="small">
          <el-form-item label="操作类型" prop="utAprAction">
            <el-select v-if="modalMode === 'add'" v-model="modalForm.utAprAction" placeholder="请选择" style="width: 100%">
              <el-option v-for="opt in fd.utAprActions" :key="opt.v" :label="opt.l" :value="opt.v" />
            </el-select>
            <el-input v-else :model-value="actionTypeLabel(modalForm.utAprAction)" disabled />
          </el-form-item>
          <el-form-item label="显示名称" prop="utAprActionName">
            <el-input v-model="modalForm.utAprActionName" placeholder="请输入显示名称" maxlength="16" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="closeModal">取消</el-button>
          <el-button type="primary" @click="commitModal">确定</el-button>
        </template>
      </el-dialog>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { markRaw, ref } from "vue";
import type { FormInstance } from "element-plus";
import { EditPen, Grid, Setting, User } from "@element-plus/icons-vue";
import InputUserSelector from "@/views/core/public/InputUserSelector.vue";
import QfdPanelMultiInstanceQfeService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelMultiInstanceQfeService";
import { QfdPanelMiFormDefine as fd } from "@/views/qf/sfc_private/flowable-designer/api/QfdPanelMultiInstanceApi";

const UserIcon = markRaw(User);
const SettingIcon = markRaw(Setting);
const GridIcon = markRaw(Grid);
const EditPenIcon = markRaw(EditPen);

const props = defineProps<{
  modeler: unknown;
  element: unknown;
}>();

const {
  panelForm,
  groupList,
  draftMemberIds,
  multiMode,
  onUserSelected,
  onGroupSelected,
  onAprKindChanged,
  onMemberKindChanged,
  onMiChanged,
} = QfdPanelMultiInstanceQfeService.useQfdPanelMultiInstanceQfe(
  () => props.modeler,
  () => props.element
);

const modalFormRef = ref<FormInstance>();

const {
  actionList,
  actionTypeLabel,
  modalVisible,
  modalMode,
  modalForm,
  modalRules,
  openModal,
  closeModal,
  resetModal,
  commitModal,
  removeAction,
} = QfdPanelMultiInstanceQfeService.useQfePanelMiAction(modalFormRef);
</script>

<style scoped>
.qfd-multi-instance-qfe {
  max-width: 100%;
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
.member-kind-radio :deep(.el-radio-button__inner) {
  padding: 8px 4px;
  font-size: 12px;
}
.mt-4 {
  margin-top: 16px;
}
.mi-form-item-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.mi-form-item-label .el-icon {
  font-size: 14px;
  color: var(--el-color-info);
}
.mi-action-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.mi-action-comment {
  margin-bottom: 8px;
}
</style>
