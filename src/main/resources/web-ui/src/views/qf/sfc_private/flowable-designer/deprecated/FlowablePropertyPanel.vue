<template>
  <div class="flowable-property-panel">
    <div class="flowable-property-panel__header">
      <div class="flowable-property-panel__title">{{ panelTitle }}</div>
    </div>
    <div v-if="!modeler" class="flowable-property-panel__empty">设计器未就绪</div>
    <div v-else-if="bo" class="flowable-property-panel__content">
      <el-form label-position="right" label-width="80px" size="small" class="flowable-property-panel__form">
        <el-collapse v-model="activeNames" class="custom-collapse">
          <el-collapse-item name="general">
            <template #title>
              <div class="collapse-title">
                <el-icon><InfoFilled /></el-icon>
                <span>通用</span>
              </div>
            </template>
            <el-form-item label="类型">
              <el-input :model-value="elementType" disabled />
            </el-form-item>
            <el-form-item label="ID">
              <el-input v-model="form.id" @change="onIdCommit" />
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="form.name" @change="onNameCommit" />
            </el-form-item>
            <template v-if="elementType === 'bpmn:Process'">
              <el-form-item label="版本标签">
                <el-input v-model="form.versionTag" @change="onProcessGeneralCommit" />
              </el-form-item>
              <el-form-item label="可执行">
                <el-switch v-model="form.isExecutable" @change="onProcessGeneralCommit" />
              </el-form-item>
            </template>
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="messagesSignals">
            <template #title>
              <div class="collapse-title">
                <el-icon><ChatDotRound /></el-icon>
                <span>消息与信号</span>
              </div>
            </template>
            <ProcessMessagesAndSignals :modeler="modeler" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="executionListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Bell /></el-icon>
                <span>执行监听器</span>
              </div>
            </template>
            <ExecutionListeners heading="流程执行监听器" :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="extensionProps">
            <template #title>
              <div class="collapse-title">
                <el-icon><CirclePlus /></el-icon>
                <span>扩展属性</span>
              </div>
            </template>
            <ExtensionProperties :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="task">
            <template #title>
              <div class="collapse-title">
                <el-icon><Select /></el-icon>
                <span>任务配置</span>
              </div>
            </template>
            <el-form-item label="到期时间">
              <el-input v-model="form.dueDate" @change="onUserTaskCommit" />
            </el-form-item>
            <el-form-item label="跟进时间">
              <el-input v-model="form.followUpDate" @change="onUserTaskCommit" />
            </el-form-item>
            <el-form-item label="优先级">
              <el-input v-model="form.priority" @change="onUserTaskCommit" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="multiInstance">
            <template #title>
              <div class="collapse-title">
                <el-icon><Grid /></el-icon>
                <span>审批与多实例</span>
              </div>
            </template>
            <MultiInstance :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="taskListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Flag /></el-icon>
                <span>任务监听器</span>
              </div>
            </template>
            <TaskListeners :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="userTaskExecutionListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Bell /></el-icon>
                <span>执行监听器</span>
              </div>
            </template>
            <ExecutionListeners heading="任务执行监听器" :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="userTaskExtensionProps">
            <template #title>
              <div class="collapse-title">
                <el-icon><CirclePlus /></el-icon>
                <span>扩展属性</span>
              </div>
            </template>
            <ExtensionProperties :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="userTaskOther">
            <template #title>
              <div class="collapse-title">
                <el-icon><Notebook /></el-icon>
                <span>其他</span>
              </div>
            </template>
            <el-form-item label="跳过表达式">
              <el-input v-model="form.skipExpression" placeholder="Flowable skipExpression" @change="onUserTaskOtherCommit" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:SequenceFlow'" name="sequenceFlow">
            <template #title>
              <div class="collapse-title">
                <el-icon><Connection /></el-icon>
                <span>流转条件</span>
              </div>
            </template>
            <el-form-item label="条件模板">
              <el-select v-model="form.conditionPreset" style="width: 100%" @change="onConditionPresetChange">
                <el-option label="无(无条件直达)" value="none" />
                <el-option label="同意 (approved == true)" value="approve" />
                <el-option label="驳回 (approved == false)" value="reject" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item label="条件表达式">
              <el-input
                v-model="form.conditionExpression"
                type="textarea"
                :rows="2"
                :disabled="form.conditionPreset !== 'custom'"
                placeholder="${approved == true}"
                @change="onSequenceFlowCommit"
              />
            </el-form-item>
            <el-form-item label="默认流">
              <div class="flow-default-row">
                <el-switch v-model="form.isDefault" @change="onDefaultFlowCommit" />
                <span class="flow-hint">上游网关找不到匹配条件时走此分支</span>
              </div>
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:StartEvent'" name="startEvent">
            <template #title>
              <div class="collapse-title">
                <el-icon><VideoPlay /></el-icon>
                <span>开始事件</span>
              </div>
            </template>
            <el-form-item label="表单 Key">
              <el-input v-model="form.formKey" @change="onStartEventCommit" />
            </el-form-item>
            <el-form-item label="发起人变量">
              <el-input v-model="form.initiator" @change="onStartEventCommit" />
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:UserTask' || isFlowableAsyncOnly" name="async">
            <template #title>
              <div class="collapse-title">
                <el-icon><Timer /></el-icon>
                <span>异步与独占</span>
              </div>
            </template>
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
          </el-collapse-item>

          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="other">
            <template #title>
              <div class="collapse-title">
                <el-icon><Setting /></el-icon>
                <span>其他</span>
              </div>
            </template>
            <el-form-item label="候选启动用户">
              <el-input v-model="form.candidateStarterUsers" placeholder="逗号分隔" @change="onProcessOtherCommit" />
            </el-form-item>
            <el-form-item label="候选启动组">
              <el-input v-model="form.candidateStarterGroups" placeholder="逗号分隔" @change="onProcessOtherCommit" />
            </el-form-item>
            <el-form-item label="历史保留时间">
              <el-input v-model="form.historyTimeToLive" @change="onProcessOtherCommit" />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </div>
    <div v-else class="flowable-property-panel__empty">未选中元素</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, shallowRef, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  InfoFilled,
  Setting,
  Select,
  VideoPlay,
  Timer,
  Bell,
  ChatDotRound,
  CirclePlus,
  Grid,
  Flag,
  Notebook,
  Connection,
} from "@element-plus/icons-vue";
import { findProcessElement } from "@/views/qf/sfc_private/flowable-designer/FlowableModelUtils";
import type { ConditionPreset } from "@/views/qf/sfc_private/flowable-designer/deprecated/FlowableSequenceFlowPanel.js";
import {
  resetSequenceFlowFormFields,
  loadSequenceFlowFromBo,
  onConditionPresetChange as applySequenceFlowPresetChange,
  commitSequenceFlowCondition,
  commitDefaultFlow as applySequenceFlowDefaultCommit,
} from "@/views/qf/sfc_private/flowable-designer/deprecated/FlowableSequenceFlowPanel.js";
import ProcessMessagesAndSignals from "@/views/qf/sfc_private/flowable-designer/deprecated/ProcessMessagesAndSignals.vue";
import ExecutionListeners from "@/views/qf/sfc_private/flowable-designer/deprecated/ExecutionListeners.vue";
import ExtensionProperties from "@/views/qf/sfc_private/flowable-designer/deprecated/ExtensionProperties.vue";
import MultiInstance from "@/views/qf/sfc_private/flowable-designer/deprecated/MultiInstance.vue";
import TaskListeners from "@/views/qf/sfc_private/flowable-designer/deprecated/TaskListeners.vue";

//默认不展开任何项!
const activeNames = ref([
  /*   "general",
  "task",
  "multiInstance",
  "taskListeners",
  "userTaskExecutionListeners",
  "userTaskExtensionProps",
  "userTaskOther",
  "other",
  "messagesSignals",
  "executionListeners",
  "extensionProps",
  "startEvent",
  "async", */
]);

const panelTitle = computed(() => {
  //如果是bpmn:Process，则返回"流程配置"
  if (elementType.value === "bpmn:Process") {
    return "流程全局配置";
  }

  return "节点属性配置: " + elementType.value;
});

const props = defineProps<{
  modeler: unknown;
}>();

const selected = shallowRef<unknown>(null);

function getModeler(): any {
  return props.modeler;
}

const targetElement = computed(() => {
  const m = getModeler();
  if (!m) {
    return null;
  }
  const el = selected.value as { type?: string } | null;
  if (!el) {
    return findProcessElement(m);
  }
  if (el.type === "label") {
    return findProcessElement(m);
  }
  return el;
});

const bo = computed(() => {
  const el = targetElement.value as { businessObject?: Record<string, unknown> } | null;
  if (!el?.businessObject) {
    return null;
  }
  return el.businessObject;
});

const elementType = computed(() => (bo.value?.$type as string) || "");

/** UserTask 已在专属区块展示异步；其余带 AsyncCapable 的类型单独展示 */
const isFlowableAsyncOnly = computed(() => {
  const t = elementType.value;
  if (!t) {
    return false;
  }
  if (t === "bpmn:UserTask") {
    return false;
  }
  const asyncTypes = new Set([
    "bpmn:ServiceTask",
    "bpmn:ScriptTask",
    "bpmn:ReceiveTask",
    "bpmn:ManualTask",
    "bpmn:BusinessRuleTask",
    "bpmn:SendTask",
    "bpmn:CallActivity",
    "bpmn:SubProcess",
    "bpmn:ExclusiveGateway",
    "bpmn:ParallelGateway",
    "bpmn:InclusiveGateway",
    "bpmn:EventBasedGateway",
    "bpmn:StartEvent",
    "bpmn:EndEvent",
    "bpmn:IntermediateCatchEvent",
    "bpmn:IntermediateThrowEvent",
    "bpmn:BoundaryEvent",
  ]);
  return asyncTypes.has(t);
});

const form = ref({
  id: "",
  name: "",
  candidateStarterUsers: "",
  candidateStarterGroups: "",
  versionTag: "",
  historyTimeToLive: "",
  dueDate: "",
  followUpDate: "",
  priority: "",
  formKey: "",
  initiator: "",
  async: false,
  asyncBefore: false,
  asyncAfter: false,
  exclusive: true,
  isExecutable: true,
  skipExpression: "",
  conditionPreset: "none" as ConditionPreset,
  conditionExpression: "",
  isDefault: false,
});

function loadFormFromBo(): void {
  const b = bo.value;
  if (!b) {
    return;
  }
  resetSequenceFlowFormFields(form);
  form.value.id = (b.id as string) || "";
  form.value.name = (b.name as string) || "";
  form.value.candidateStarterUsers = (b.candidateStarterUsers as string) || "";
  form.value.candidateStarterGroups = (b.candidateStarterGroups as string) || "";
  form.value.versionTag = (b.versionTag as string) || "";
  form.value.historyTimeToLive = (b.historyTimeToLive as string) || "";
  form.value.dueDate = (b.dueDate as string) || "";
  form.value.followUpDate = (b.followUpDate as string) || "";
  form.value.priority = (b.priority as string) || "";
  form.value.formKey = (b.formKey as string) || "";
  form.value.initiator = (b.initiator as string) || "";
  form.value.async = Boolean(b.async);
  form.value.asyncBefore = Boolean(b.asyncBefore);
  form.value.asyncAfter = Boolean(b.asyncAfter);
  form.value.exclusive = b.exclusive !== false;
  if (b.$type === "bpmn:Process") {
    form.value.isExecutable = b.isExecutable === undefined ? true : Boolean(b.isExecutable);
    form.value.skipExpression = "";
    return;
  }
  form.value.isExecutable = true;
  if (b.$type === "bpmn:UserTask") {
    form.value.skipExpression = (b.skipExpression as string) || "";
    return;
  }
  if (b.$type === "bpmn:SequenceFlow") {
    form.value.skipExpression = "";
    loadSequenceFlowFromBo(b, targetElement.value, form);
    return;
  }
  form.value.skipExpression = "";
}

watch([bo, elementType, targetElement], () => {
  loadFormFromBo();
});

let selectionOff: (() => void) | null = null;

watch(
  () => props.modeler,
  (m) => {
    if (selectionOff) {
      selectionOff();
      selectionOff = null;
    }
    if (!m) {
      selected.value = null;
      return;
    }
    const eventBus = (m as any).get("eventBus");
    const handler = (e: { newSelection: unknown[] }): void => {
      selected.value = e.newSelection?.[0] ?? null;
    };
    eventBus.on("selection.changed", handler);
    selectionOff = () => {
      eventBus.off("selection.changed", handler);
    };
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (selectionOff) {
    selectionOff();
  }
});

function applyProps(patch: Record<string, unknown>): void {
  const m = getModeler();
  const el = targetElement.value as { id?: string } | null;
  if (!m || !el) {
    return;
  }
  const modeling = m.get("modeling");
  modeling.updateProperties(el, patch);
}

function onIdCommit(): void {
  const m = getModeler();
  if (!m) {
    return;
  }
  const el = targetElement.value as { id?: string } | null;
  if (!el?.id) {
    return;
  }
  const newId = form.value.id?.trim();
  if (!newId) {
    ElMessage.warning("ID 不能为空");
    loadFormFromBo();
    return;
  }
  if (newId === el.id) {
    return;
  }
  const modeling = m.get("modeling");
  modeling.updateProperties(el, { id: newId });
}

function onNameCommit(): void {
  applyProps({ name: form.value.name });
}

function onProcessGeneralCommit(): void {
  if (elementType.value !== "bpmn:Process") {
    return;
  }
  applyProps({
    versionTag: form.value.versionTag || undefined,
    isExecutable: form.value.isExecutable,
  });
}

function onProcessOtherCommit(): void {
  if (elementType.value !== "bpmn:Process") {
    return;
  }
  applyProps({
    candidateStarterUsers: form.value.candidateStarterUsers || undefined,
    candidateStarterGroups: form.value.candidateStarterGroups || undefined,
    historyTimeToLive: form.value.historyTimeToLive || undefined,
  });
}

function onUserTaskCommit(): void {
  applyProps({
    dueDate: form.value.dueDate || undefined,
    followUpDate: form.value.followUpDate || undefined,
    priority: form.value.priority || undefined,
  });
}

function onUserTaskOtherCommit(): void {
  if (elementType.value !== "bpmn:UserTask") {
    return;
  }
  applyProps({
    skipExpression: form.value.skipExpression || undefined,
  });
}

function onAsyncCommit(): void {
  applyProps({
    async: form.value.async,
    asyncBefore: form.value.asyncBefore,
    asyncAfter: form.value.asyncAfter,
    exclusive: form.value.exclusive,
  });
}

function onStartEventCommit(): void {
  applyProps({
    formKey: form.value.formKey || undefined,
    initiator: form.value.initiator || undefined,
  });
}

function onConditionPresetChange(preset: ConditionPreset): void {
  applySequenceFlowPresetChange(elementType.value, preset, form, onSequenceFlowCommit);
}

function onSequenceFlowCommit(): void {
  commitSequenceFlowCondition(elementType.value, getModeler, applyProps, form);
}

function onDefaultFlowCommit(): void {
  applySequenceFlowDefaultCommit(elementType.value, targetElement.value, form.value.isDefault, getModeler);
}
</script>

<style scoped>
.flowable-property-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  background-color: #fff;
  border-left: 1px solid var(--el-border-color-light);
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.02);
}
.flowable-property-panel__header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: #fafafa;
}
.flowable-property-panel__title {
  font-weight: 600;
  font-size: 16px;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}
.flowable-property-panel__content {
  flex: 1;
  overflow: auto;
  padding: 0;
}
.flowable-property-panel__form {
  padding: 0;
}
.flowable-property-panel__empty {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  padding: 32px 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.custom-collapse {
  border-top: none;
  border-bottom: none;
}
:deep(.el-collapse-item__header) {
  padding: 0 20px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  background-color: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background-color 0.3s;
}
:deep(.el-collapse-item__header:hover) {
  background-color: #f5f7fa;
}
:deep(.el-collapse-item__wrap) {
  border-bottom: none;
  background-color: #fafafa;
}
:deep(.el-collapse-item__content) {
  padding: 20px;
  padding-bottom: 12px;
}
.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.collapse-title .el-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}
.flow-default-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.flow-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
