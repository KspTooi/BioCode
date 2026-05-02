<template>
  <div class="flowable-designer-root">
    <div class="flowable-designer-toolbar">
      <el-button-group size="small" type="primary">
        <el-button :icon="Document" @click="onExit">退出设计</el-button>
        <el-button :disabled="readonly" :icon="Document" @click="onSave">保存模型设计</el-button>
        <el-button :icon="FolderOpened" @click="onImportClick">导入</el-button>
        <el-button :icon="Download" @click="downloadXml">导出 BPMN</el-button>
        <el-button :icon="Download" @click="downloadSvg">导出 SVG</el-button>
      </el-button-group>
      <el-tag v-if="readonly" type="warning" class="ml8" size="small">只读模式</el-tag>
      <el-button-group size="small" class="ml8">
        <el-tooltip content="缩小" placement="bottom">
          <el-button :icon="ZoomOut" :disabled="defaultZoom <= 0.2" @click="zoomOut()" />
        </el-tooltip>
        <el-button disabled>{{ Math.floor(defaultZoom * 100) }}%</el-button>
        <el-tooltip content="放大" placement="bottom">
          <el-button :icon="ZoomIn" :disabled="defaultZoom >= 4" @click="zoomIn()" />
        </el-tooltip>
        <el-tooltip content="适应画布" placement="bottom">
          <el-button :icon="ScaleToOriginal" @click="zoomFit()" />
        </el-tooltip>
      </el-button-group>
      <el-button-group size="small" class="ml8">
        <el-tooltip content="撤销" placement="bottom">
          <el-button :icon="RefreshLeft" :disabled="!revocable" @click="undo()" />
        </el-tooltip>
        <el-tooltip content="重做" placement="bottom">
          <el-button :icon="RefreshRight" :disabled="!recoverable" @click="redo()" />
        </el-tooltip>
        <el-tooltip content="新建空白流程" placement="bottom">
          <el-button :icon="Refresh" @click="onRestart" />
        </el-tooltip>
      </el-button-group>
      <el-tooltip placement="bottom" :show-after="300" popper-class="flowable-model-status-tooltip-popper">
        <template #content>
          <div v-if="modelStatus.valid">模型结构正常</div>
          <div v-else class="flowable-model-status-tooltip">
            <div v-for="(issue, idx) in modelStatus.issues" :key="idx">{{ idx + 1 }}. {{ issue }}</div>
          </div>
        </template>
        <span :class="['flowable-designer-status', 'ml8', modelStatus.valid ? 'is-valid' : 'is-invalid']">
          <el-icon><SuccessFilled v-if="modelStatus.valid" /><WarningFilled v-else /></el-icon>
          {{ modelStatus.valid ? "模型有效" : `${modelStatus.issues.length} 项问题` }}
        </span>
      </el-tooltip>
      <span class="flowable-designer-hint ml8">
        <el-icon><Cpu /></el-icon>
        Flowable（flow 命名空间）
      </span>
    </div>

    <input ref="fileInputRef" type="file" accept=".xml,.bpmn" class="flowable-designer-file" @change="onFileChange" />

    <splitpanes class="flowable-designer-split custom-theme">
      <pane min-size="40" size="70">
        <div class="canvas-wrapper">
          <div ref="canvasRef" class="flowable-designer-canvas" />
          <div class="custom-watermark">
            <div style="display: flex; align-items: center; gap: 4px">
              <!-- <img src="@/assets/qlc_logo.png" alt="BioCode" class="custom-watermark-logo" width="32" height="32" /> -->
              <div style="display: flex; flex-direction: column; gap: 4px">
                <div>PowerBy BioCode</div>
                <div>Since 1.6T50</div>
              </div>
            </div>
          </div>
        </div>
      </pane>
      <pane min-size="20" size="30">
        <!-- <FlowablePropertyPanel :modeler="modeler" /> -->
        <QfdPanel :modeler="modeler" />
      </pane>
    </splitpanes>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import {
  ZoomIn,
  ZoomOut,
  RefreshLeft,
  RefreshRight,
  Refresh,
  Download,
  FolderOpened,
  ScaleToOriginal,
  Document,
  Cpu,
  SuccessFilled,
  WarningFilled,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
import { useFlowableModeler } from "@/views/qf/sfc_private/flowable-designer/UseFlowableModeler";
import FlowablePropertyPanel from "@/views/qf/sfc_private/flowable-designer/deprecated/FlowablePropertyPanel.vue";
import QfdPanel from "@/views/qf/sfc_private/flowable-designer/components/QfdPanel.vue";

const props = withDefaults(
  defineProps<{
    /** 初始 BPMN XML，空则新建空白流程 */
    initialXml?: string;
    /** 是否只读 */
    readonly?: boolean;
  }>(),
  {
    initialXml: "",
  }
);

const emit = defineEmits<{
  (e: "update:xml", xml: string): void;
  (e: "save", xml: string): void;
  (e: "exit"): void;
}>();

const canvasRef = ref<HTMLElement | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);

const {
  modeler,
  defaultZoom,
  revocable,
  recoverable,
  modelStatus,
  init,
  importXml,
  saveXml,
  saveSvg,
  zoomIn,
  zoomOut,
  zoomFit,
  undo,
  redo,
  newDiagram,
} = useFlowableModeler(canvasRef, props.readonly);

function setEncoded(type: string, filename: string, data: string): { filename: string; href: string } {
  const encodedData = encodeURIComponent(data);
  return {
    filename: `${filename}.${type}`,
    href: `data:application/${type === "svg" ? "svg+xml" : "bpmn20-xml"};charset=UTF-8,${encodedData}`,
  };
}

function downloadFunc(href: string, filename: string): void {
  if (!href || !filename) {
    return;
  }
  const a = document.createElement("a");
  a.download = filename;
  a.href = href;
  a.click();
  URL.revokeObjectURL(a.href);
}

async function emitXmlChanged(): Promise<void> {
  const xml = await saveXml(true);
  if (!xml) {
    return;
  }
  emit("update:xml", xml);
}

let offCommandStack: (() => void) | null = null;

async function bindStackListener(): Promise<void> {
  const m = modeler.value as any;
  if (!m) {
    return;
  }
  const eventBus = m.get("eventBus");
  eventBus.on("commandStack.changed", emitXmlChanged);
  offCommandStack = () => {
    eventBus.off("commandStack.changed", emitXmlChanged);
  };
}

async function onSave(): Promise<void> {
  const xml = await saveXml(true);
  console.log("11", xml);
  if (!xml) {
    return;
  }
  emit("save", xml);
  //ElMessage.success("已生成流程 XML");
}

async function downloadXml(): Promise<void> {
  const xml = await saveXml(true);
  if (!xml) {
    return;
  }
  const { href, filename } = setEncoded("bpmn", "diagram", xml);
  downloadFunc(href, filename);
}

async function downloadSvg(): Promise<void> {
  const svg = await saveSvg();
  if (!svg) {
    return;
  }
  const { href, filename } = setEncoded("svg", "diagram", svg);
  downloadFunc(href, filename);
}

function onImportClick(): void {
  fileInputRef.value?.click();
}

function onFileChange(): void {
  const input = fileInputRef.value;
  if (!input?.files?.length) {
    return;
  }
  const file = input.files[0];
  const reader = new FileReader();
  reader.onload = () => {
    const text = reader.result as string;
    importXml(text)
      .then(() => {
        zoomFit();
        emitXmlChanged();
      })
      .catch(() => ElMessage.error("导入失败"));
    input.value = "";
  };
  reader.readAsText(file);
}

function onRestart(): void {
  const pid = `Process_${Date.now()}`;
  const pname = `业务流程_${Date.now()}`;
  newDiagram(pid, pname)
    .then(() => {
      zoomFit();
      emitXmlChanged();
    })
    .catch(() => ElMessage.error("重置失败"));
}

/**
 * 退出设计
 */
function onExit(): void {
  //只读模式下不提示
  if (props.readonly) {
    emit("exit");
    return;
  }

  ElMessageBox.confirm("退出设计会丢失当前设计内容，确定还要继续退出吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    emit("exit");
  });
}

onMounted(async () => {
  await nextTick();
  init();
  await bindStackListener();
  const initial = props.initialXml?.trim();
  if (initial) {
    await importXml(initial);
    zoomFit();
    await emitXmlChanged();
    return;
  }
  await newDiagram(`Process_${Date.now()}`, `业务流程_${Date.now()}`);
  zoomFit();
  await emitXmlChanged();
});

onBeforeUnmount((): void => {
  if (!offCommandStack) {
    return;
  }
  offCommandStack();
  offCommandStack = null;
});
</script>

<style scoped>
.flowable-designer-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.flowable-designer-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  background-color: #fcfcfc;
  border-bottom: 1px solid var(--el-border-color-light);
}
.ml8 {
  margin-left: 8px;
}
.flowable-designer-status {
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: default;
  transition: all 0.3s;
  font-weight: 500;
}
.flowable-designer-status.is-valid {
  color: #67c23a;
  background-color: #f0f9eb;
}
.flowable-designer-status.is-invalid {
  color: #e6a23c;
  background-color: #fdf6ec;
}
.flowable-designer-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
}
.flowable-designer-file {
  display: none;
}
.flowable-designer-split {
  flex: 1;
  min-height: 0;
}
.flowable-designer-canvas {
  height: 100%;
  min-height: 420px;
  background-color: #fff;
  background-image: radial-gradient(#e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
}
.canvas-wrapper {
  position: relative;
  height: 100%;
}
.custom-watermark {
  position: absolute;
  bottom: 16px;
  right: 20px;
  font-size: 12px;
  font-weight: 800;
  color: rgba(0, 167, 153, 0.404);
  pointer-events: none;
  z-index: 999999999999;
  letter-spacing: 1px;
}
:deep(.bjs-powered-by) {
  display: none !important;
}
:deep(.splitpanes.custom-theme .splitpanes__pane) {
  background: var(--el-bg-color-page);
}
:deep(.splitpanes.custom-theme .splitpanes__splitter) {
  background: var(--el-border-color-lighter);
  width: 4px;
  transition: background-color 0.3s;
}
:deep(.splitpanes.custom-theme .splitpanes__splitter:hover) {
  background: var(--el-color-primary-light-5);
}

/* 优化 BPMN 内部元素样式 */
:deep(.djs-visual > rect:not([rx="0"])) {
  rx: 6px !important;
  ry: 6px !important;
}
:deep(.djs-visual > rect),
:deep(.djs-visual > circle),
:deep(.djs-visual > polygon),
:deep(.djs-visual > path) {
  transition: all 0.3s ease;
}

/* 悬停时的状态 - 仅针对主要形状，排除文字背景等 */
:deep(.djs-element:hover .djs-visual > rect:not([fill="none"])),
:deep(.djs-element:hover .djs-visual > circle),
:deep(.djs-element:hover .djs-visual > polygon) {
  fill: #f2f8fe !important;
}

/* 优化连线选中和悬停状态 */
:deep(.djs-connection:hover > path) {
  stroke: var(--el-color-primary) !important;
}
:deep(.djs-connection.selected > path) {
  stroke: var(--el-color-primary) !important;
  stroke-width: 3px !important;
}

/* 优化标签文字 */
:deep(.djs-label) {
  font-family: var(--el-font-family) !important;
  font-size: 13px !important;
}

/* 优化上下文菜单（点击元素弹出的工具栏） */
:deep(.djs-context-pad) {
  background: #fff !important;
  border-radius: 8px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid var(--el-border-color-light) !important;
}
:deep(.djs-context-pad .entry) {
  border-radius: 4px !important;
  transition: background-color 0.2s;
}
:deep(.djs-context-pad .entry:hover) {
  background-color: #f5f7fa !important;
}

/* 优化左侧工具面板（Palette） */
:deep(.djs-palette) {
  background: #fff !important;
  border-radius: 8px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid var(--el-border-color-light) !important;
}
:deep(.djs-palette .entry) {
  border-radius: 4px !important;
  transition: background-color 0.2s;
}
:deep(.djs-palette .entry:hover) {
  background-color: #f5f7fa !important;
}
</style>

<style>
/* tooltip 挂载在 body，需全局类名 */
.flowable-model-status-tooltip-popper .flowable-model-status-tooltip {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 360px;
  line-height: 1.5;
}
</style>
