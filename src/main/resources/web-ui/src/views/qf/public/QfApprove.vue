<template>
  <div v-loading="detailsLoading" class="qf-approve-root w-full">
    <el-tabs v-if="!detailsLoading" v-model="activeTab" class="qf-approve-tabs" @tab-change="onTabChange">
      <!-- 流程表单 -->
      <el-tab-pane label="流程表单" name="form">
        <div class="form-scroll-area">
          <component :is="formComponent" v-if="formComponent && details?.dataId" :id="details.dataId" mode="view" />
          <el-empty v-if="!formComponent && !detailsLoading" description="流程表单组件未找到" />
        </div>
      </el-tab-pane>

      <!-- 流转记录 -->
      <el-tab-pane label="流转记录" name="records">
        <div class="records-container">
          <el-empty v-if="records.length === 0" description="暂无流转记录" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(record, idx) in records"
              :key="idx"
              :timestamp="
                record.finTime ||
                (record.status === 0
                  ? '待处理'
                  : record.status === 1
                    ? '已处理'
                    : record.status === 10
                      ? '已作废'
                      : record.action === 0
                        ? '同意'
                        : '—')
              "
              placement="top"
              :type="record.status === 0 ? 'primary' : record.action === 0 ? 'success' : 'danger'"
            >
              <el-card class="record-card" shadow="never">
                <div class="record-header">
                  <span class="record-node">{{ record.nodeName }}</span>
                  <el-tag v-if="record.status === 0" type="primary" size="small" effect="plain"> 待处理 </el-tag>
                  <el-tag v-else-if="record.status === 1" type="info" size="small" effect="plain"> 已处理 </el-tag>
                  <el-tag v-else-if="record.status === 10" type="danger" size="small" effect="plain"> 已作废 </el-tag>
                  <el-tag v-else-if="record.action === 0" type="success" size="small" effect="plain"> 同意 </el-tag>
                  <el-tag v-else type="danger" size="small" effect="plain"> 驳回 </el-tag>
                </div>
                <div class="record-meta">
                  <el-icon><User /></el-icon>
                  <span>{{ record.finMemberName || "—" }}</span>
                </div>
                <div v-if="record.comment" class="record-comment">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>{{ record.comment }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>

      <!-- 流程图跟踪 -->
      <el-tab-pane label="流程图跟踪" name="diagram">
        <div class="diagram-tab-wrap">
          <div class="diagram-legend">
            <span class="legend-item">
              <span class="legend-dot legend-current" />
              当前节点
            </span>
            <span class="legend-item">
              <span class="legend-dot legend-done" />
              已完成
            </span>
            <span class="legend-item">
              <span class="legend-dot legend-pending" />
              未审批
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
      </el-tab-pane>
    </el-tabs>

    <!-- 审批区域：仅在流程表单 tab 时显示，置于 tabs 外固定底部 -->
    <div v-if="details && !detailsLoading" v-show="activeTab === 'form'" class="approve-section">
      <div class="approve-body">
        <div class="approve-title">
          <el-icon><EditPen /></el-icon>
          审批意见
        </div>
        <el-input
          v-model="approveComment"
          type="textarea"
          :rows="3"
          placeholder="请输入审批意见（可选）"
          maxlength="255"
          show-word-limit
          class="approve-textarea"
        />
      </div>
      <div class="approve-footer">
        <el-button size="large" type="danger" :icon="CircleClose" :loading="submitLoading" @click="onApprove(1)">
          审批驳回
        </el-button>
        <el-button size="large" type="success" :icon="CircleCheck" :loading="submitLoading" @click="onApprove(0)">
          审批通过
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, shallowRef, type Component } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { User, ChatLineRound, CircleCheck, CircleClose, EditPen } from "@element-plus/icons-vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext";
import ComTabService from "@/soa/com-series/service/ComTabService";
import QfTodoApi from "@/views/qf/api/QfTodoApi.ts";
import type { ApproveFlowRecordVo, GetQfTodoDetailsVo, GetQfTodoListVo } from "@/views/qf/api/QfTodoApi.ts";
import { useFlowableModeler } from "@/views/qf/sfc_private/flowable-designer/UseFlowableModeler";
import ComOneTimeRouteContext from "@/soa/com-series/service/ComOneTimeRouteContext";
import ComPublicCompService from "@/soa/com-series/service/ComPublicCompService";

const { getCdrcQuery } = ComDirectRouteContext.useDirectRouteContext();
const row = getCdrcQuery(false) as GetQfTodoListVo;

const { closeTab, activeTabId } = ComTabService.useTabService();
const { redirect } = ComOneTimeRouteContext.useOneTimeRouteContext();

const details = ref<GetQfTodoDetailsVo | null>(null);
const detailsLoading = ref(false);

const records = ref<ApproveFlowRecordVo[]>([]);

const activeTab = ref("form");

/** 审批区域状态 */
const approveComment = ref("");
const submitLoading = ref(false);

/** shallowRef 持有组件，避免 Vue 深度响应式包装组件对象导致问题 */
const formComponent = shallowRef<Component | null>(null);

/** 流程图跟踪相关状态 */
const diagramContainer = ref<HTMLElement | null>(null);
const flowLoading = ref(false);
const flowLoadError = ref<string | null>(null);
/** 记录流程图是否已初始化，避免重复加载 */
let diagramInitialized = false;

/**
 * 只读模式的 bpmn-js viewer，复用项目已有的 useFlowableModeler
 */
const { init: initViewer, importXml, zoomFit } = useFlowableModeler(diagramContainer, true);

const { resolvePublicComp } = ComPublicCompService.usePublicComp();

const loadDetails = async (): Promise<GetQfTodoDetailsVo | null> => {
  try {
    detailsLoading.value = true;
    return await QfTodoApi.getQfTodoDetails({ id: row.id });
  } catch (error) {
    ElMessage.error((error as Error).message ?? "加载失败");
    return null;
  } finally {
    detailsLoading.value = false;
  }
};

/** 加载并渲染流程图，切换到 diagram tab 时触发，只执行一次 */
const loadDiagram = async (): Promise<void> => {
  if (diagramInitialized) {
    return;
  }
  if (!details.value) {
    return;
  }
  diagramInitialized = true;
  flowLoading.value = true;
  flowLoadError.value = null;
  try {
    const xml = await QfTodoApi.getQfTodoApproveFlow({ id: details.value.id });
    // 等待 DOM 渲染完成后再初始化 viewer，否则 container 可能不可见导致渲染异常
    await nextTick();
    initViewer();
    await importXml(xml);
    zoomFit();
    //const registry = (modeler.value as any).get("elementRegistry");
    //const el = registry.get("Activity_0b97o4y"); // 换成你 XML 里有 bioc:fill 的那个 id
    //console.log("DI:", el.di);
    //console.log("bioc:fill on DI:", el.di.get("bioc:fill"));
    //console.log("bioc:stroke on DI:", el.di.get("bioc:stroke"));
    //console.log("$attrs:", el.di.$attrs);
  } catch (error) {
    const msg = (error as Error).message ?? "流程图加载失败";
    flowLoadError.value = msg;
    ElMessage.error(msg);
  } finally {
    flowLoading.value = false;
  }
};

const onApprove = async (action: 0 | 1): Promise<void> => {
  if (!details.value) {
    return;
  }
  const label = action === 0 ? "审批通过" : "审批驳回";
  await ElMessageBox.confirm(`确定要「${label}」此待办吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  });
  submitLoading.value = true;
  try {
    const msg = await QfTodoApi.approveQfTodo({
      id: details.value.id,
      action,
      comment: approveComment.value,
    });
    ElMessage.success(msg || "审批成功");
    approveComment.value = "";

    //关闭当前标签页
    closeTab(activeTabId.value);

    //跳转到待办列表页
    redirect("qfTodo");
  } catch (error) {
    ElMessage.error((error as Error).message ?? "审批失败");
  } finally {
    submitLoading.value = false;
  }
};

const onTabChange = async (tabName: string): Promise<void> => {
  if (tabName === "form") {
    return;
  }

  if (tabName === "records") {
    const ret = await QfTodoApi.getQfTodoApproveFlowRecord({ id: details.value.id });
    records.value = ret;
  }

  if (tabName === "diagram") {
    void loadDiagram();
  }
};

onMounted(async () => {
  if (!row) {
    closeTab(activeTabId.value);
    return;
  }

  details.value = await loadDetails();

  if (!details.value || !details.value.routePc) {
    ElMessage.error("待办详情不存在，或流程表单配置有误！");
    closeTab(activeTabId.value);
    return;
  }

  //解析公共组件
  const comp = resolvePublicComp(details.value.routePc);

  if (!comp) {
    ElMessage.error("未找到流程表单组件：" + details.value.routePc);
    closeTab(activeTabId.value);
    return;
  }

  formComponent.value = comp;
});
</script>

<style scoped>
/* 撑满框架分配的全部高度，min-h-0 让 flex 子项可以收缩不溢出 */
.qf-approve-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px 24px;
  box-sizing: border-box;
}

/* el-tabs 撑满剩余高度，内部内容区可滚动 */
.qf-approve-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* el-tabs 的 tab-header 固定，tab-content 占满剩余高度，溢出由子级自己管理 */
.qf-approve-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* el-tab-pane 默认 display:block，需要撑满 */
.qf-approve-tabs :deep(.el-tab-pane) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

/* 业务表单区：撑满 tab 内容区，作为唯一滚动容器 */
.form-scroll-area {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 8px;
}

/* 压平业务表单自带的滚动（避免双滚动条），由外层统一滚动 */
.form-scroll-area :deep(.form_container) {
  max-height: none !important;
  overflow: visible !important;
  padding-right: 0 !important;
}

/* 流程图 tab 外层，flex 列撑满高度 */
.diagram-tab-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

/* 图例栏 */
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

/* 流程图容器，flex:1 撑满剩余高度，bpmn-js 需要明确的尺寸才能正常渲染 */
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

.approve-section {
  flex-shrink: 0;
  border-top: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.approve-body {
  padding: 16px 24px 12px;
  max-width: 900px;
  margin: 0 auto;
}

.approve-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.approve-textarea {
  width: 100%;
}

.approve-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 12px 24px 16px;
}

.records-container {
  padding: 16px 8px;
  max-width: 720px;
}

.record-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
}

.record-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.record-node {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  flex: 1;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.record-comment {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-top: 6px;
  line-height: 1.5;
}
</style>
