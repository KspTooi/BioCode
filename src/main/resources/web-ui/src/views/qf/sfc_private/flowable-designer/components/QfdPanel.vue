<template>
  <div class="flowable-property-panel">
    <div class="flowable-property-panel__header">
      <div class="flowable-property-panel__title">{{ panelTitle }}</div>
      <el-tag v-if="readonly" type="warning" size="small">只读模式</el-tag>
    </div>
    <div v-if="!modeler" class="flowable-property-panel__empty">设计器未就绪</div>
    <div v-else-if="bo" class="flowable-property-panel__content">
      <el-form label-position="right" label-width="80px" size="small" class="flowable-property-panel__form">
        <el-collapse v-model="activeNames" class="custom-collapse">
          <!-- 通用：所有元素 -->
          <el-collapse-item name="general">
            <template #title>
              <div class="collapse-title">
                <el-icon><InfoFilled /></el-icon>
                <span>通用</span>
              </div>
            </template>
            <QfdPanelGeneral :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 流程：消息与信号 -->
          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="messagesSignals">
            <template #title>
              <div class="collapse-title">
                <el-icon><ChatDotRound /></el-icon>
                <span>消息与信号</span>
              </div>
            </template>
            <QfdPanelMsgAndSignals :modeler="modeler" />
          </el-collapse-item>

          <!-- 流程：执行监听器 -->
          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="executionListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Bell /></el-icon>
                <span>执行监听器</span>
              </div>
            </template>
            <QfdPanelExecutionListeners heading="流程执行监听器" :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 流程：扩展属性 -->
          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="extensionProps">
            <template #title>
              <div class="collapse-title">
                <el-icon><CirclePlus /></el-icon>
                <span>扩展属性</span>
              </div>
            </template>
            <QfdPanelExtensionProperties :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 流程：其他配置 -->
          <el-collapse-item v-if="elementType === 'bpmn:Process'" name="processOther">
            <template #title>
              <div class="collapse-title">
                <el-icon><Setting /></el-icon>
                <span>其他</span>
              </div>
            </template>
            <QfdPanelProcess :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：任务配置 -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="task">
            <template #title>
              <div class="collapse-title">
                <el-icon><Select /></el-icon>
                <span>任务配置</span>
              </div>
            </template>
            <QfdPanelUserTask :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：审批与多实例 -->
          <el-collapse-item v-show="false" v-if="elementType === 'bpmn:UserTask'" name="multiInstance">
            <template #title>
              <div class="collapse-title">
                <el-icon><Grid /></el-icon>
                <span>审批与多实例</span>
              </div>
            </template>
            <QfdPanelMultiInstance :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：审批与多实例 (QFE) -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="multiInstanceQfe">
            <template #title>
              <div class="collapse-title">
                <el-icon><Grid /></el-icon>
                <span>审批与多实例 (QFE)</span>
              </div>
            </template>
            <QfdPanelMultiInstanceQfe :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：表单配置 -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="formConfig">
            <template #title>
              <div class="collapse-title">
                <el-icon><Document /></el-icon>
                <span>表单配置</span>
              </div>
            </template>
            <QfdPanelForm :modeler="modeler" :element="targetElement" :form-id="formId" />
          </el-collapse-item>

          <!-- 用户任务：任务监听器 -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="taskListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Flag /></el-icon>
                <span>任务监听器</span>
              </div>
            </template>
            <QfdPanelTaskListeners :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：执行监听器 -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="userTaskExecutionListeners">
            <template #title>
              <div class="collapse-title">
                <el-icon><Bell /></el-icon>
                <span>执行监听器</span>
              </div>
            </template>
            <QfdPanelExecutionListeners heading="任务执行监听器" :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 用户任务：扩展属性 -->
          <el-collapse-item v-if="elementType === 'bpmn:UserTask'" name="userTaskExtensionProps">
            <template #title>
              <div class="collapse-title">
                <el-icon><CirclePlus /></el-icon>
                <span>扩展属性</span>
              </div>
            </template>
            <QfdPanelExtensionProperties :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 顺序流：流转条件 -->
          <el-collapse-item v-if="elementType === 'bpmn:SequenceFlow'" name="sequenceFlow">
            <template #title>
              <div class="collapse-title">
                <el-icon><Connection /></el-icon>
                <span>流转条件</span>
              </div>
            </template>
            <QfdPanelSequenceFlow :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 开始事件 -->
          <el-collapse-item v-if="elementType === 'bpmn:StartEvent'" name="startEvent">
            <template #title>
              <div class="collapse-title">
                <el-icon><VideoPlay /></el-icon>
                <span>开始事件</span>
              </div>
            </template>
            <QfdPanelStartEvent :modeler="modeler" :element="targetElement" />
          </el-collapse-item>

          <!-- 异步与独占：用户任务 + 其他支持异步的节点 -->
          <el-collapse-item v-if="showAsync" name="async">
            <template #title>
              <div class="collapse-title">
                <el-icon><Timer /></el-icon>
                <span>异步与独占</span>
              </div>
            </template>
            <QfdPanelAsync :modeler="modeler" :element="targetElement" />
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </div>
    <div v-else class="flowable-property-panel__empty">未选中元素</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, toRef } from "vue";
import {
  Bell,
  ChatDotRound,
  CirclePlus,
  Connection,
  Document,
  Flag,
  Grid,
  InfoFilled,
  Select,
  Setting,
  Timer,
  VideoPlay,
} from "@element-plus/icons-vue";
import QfdPanelService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelService";
import QfdPanelGeneral from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelGeneral.vue";
import QfdPanelExecutionListeners from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelExecutionListeners.vue";
import QfdPanelExtensionProperties from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelExtensionProperties.vue";
import QfdPanelAsync from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelAsync.vue";
import QfdPanelUserTask from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelUserTask.vue";
import QfdPanelTaskListeners from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelTaskListeners.vue";
import QfdPanelStartEvent from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelStartEvent.vue";
import QfdPanelMultiInstance from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelMultiInstance.vue";
import QfdPanelMultiInstanceQfe from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelMultiInstanceQfe.vue";
import QfdPanelForm from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelForm.vue";
import QfdPanelSequenceFlow from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelSequenceFlow.vue";
import QfdPanelProcess from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelProcess.vue";
import QfdPanelMsgAndSignals from "@/views/qf/sfc_private/flowable-designer/components/QfdPanelMsgAndSignals.vue";

const props = withDefaults(
  defineProps<{
    modeler: unknown;
    formId?: string; //绑定的表单ID
    readonly?: boolean; //是否只读
  }>(),
  {
    formId: undefined,
    readonly: false,
  }
);

const { targetElement, bo, elementType, panelTitle, activeNames, dispose } = QfdPanelService.useQfdPanel(
  toRef(props, "modeler")
);

const ASYNC_TYPES = new Set([
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

const showAsync = computed(() => {
  const t = elementType.value;
  if (!t) {
    return false;
  }
  if (t === "bpmn:UserTask") {
    return true;
  }
  return ASYNC_TYPES.has(t);
});

onBeforeUnmount(() => {
  dispose();
});
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
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
</style>
