<template>
  <div class="diagram-tab-wrap">
    <div class="diagram-legend">
      <span class="legend-item">
        <span class="legend-dot legend-current" />
        正在处理
      </span>
      <span class="legend-item">
        <span class="legend-dot legend-done" />
        已处理
      </span>
      <span class="legend-item">
        <span class="legend-dot legend-pending" />
        未处理
      </span>
    </div>
    <div v-loading="flowLoading" class="diagram-container">
      <div class="canvas-wrapper">
        <div ref="diagramContainer" class="diagram-canvas" />
        <div class="custom-watermark">
          <div style="display: flex; align-items: center; gap: 4px">
            <div style="display: flex; flex-direction: column; gap: 4px">
              <div>PowerBy BioCode</div>
              <div>Since 1.6T50</div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!flowLoading && flowLoadError" :description="flowLoadError" />
    </div>
  </div>
</template>

<script setup lang="ts">
import QfProcDiagramService, { type QfProcDiagramProps } from "@/views/qf/public/service/QfProcDiagramService";

const props = defineProps<QfProcDiagramProps>();

const { diagramContainer, flowLoading, flowLoadError } = QfProcDiagramService.useQfProcDiagram(props);
</script>

<style scoped>
.diagram-tab-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.diagram-legend {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 12px;
  background: var(--el-fill-color-extra-light);
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  margin-bottom: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.legend-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-current {
  background-color: #409eff;
}

.legend-done {
  background-color: #67c23a;
}

.legend-pending {
  background-color: #e6a23c;
}

.diagram-container {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.canvas-wrapper {
  position: relative;
  flex: 1;
  min-height: 0;
}

.diagram-canvas {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  overflow: hidden;
  background-color: #fff;
  background-image: radial-gradient(#e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
  user-select: none;
  cursor: grab;
}

.diagram-canvas:active {
  cursor: grabbing;
}

:deep(.diagram-canvas *) {
  user-select: none;
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
</style>
