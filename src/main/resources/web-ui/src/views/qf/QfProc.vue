<template>
  <div v-loading="detailsLoading" class="qf-approve-root w-full">
    <el-tabs v-if="!detailsLoading" v-model="activeTab" class="qf-approve-tabs">
      <!-- 流程表单 -->
      <el-tab-pane label="流程表单" name="form">
        <QfProcForm :details="details" :loading="detailsLoading" :proc-context="procContext" />
      </el-tab-pane>

      <!-- 流转记录 -->
      <el-tab-pane label="流转记录" name="records">
        <QfProcHistory v-if="activeTab === 'records' && details?.engProcId" :proc-id="details.engProcId" />
      </el-tab-pane>

      <!-- 流程图跟踪 -->
      <el-tab-pane label="流程图跟踪" name="diagram">
        <QfProcDiagram v-if="activeTab === 'diagram' && details?.engProcId" :proc-id="details.engProcId" />
      </el-tab-pane>
    </el-tabs>

    <!-- 审批区域：仅在流程表单 tab 时显示，置于 tabs 外固定底部 -->
    <div v-if="details && !detailsLoading" v-show="activeTab === 'form' && row._mode === 'approve'" class="approve-section">
      <div v-if="details.allowComment === 1" class="approve-body">
        <div class="approve-title">
          <el-icon><EditPen /></el-icon>
          审批意见
        </div>
        <el-input
          v-model="approveComment"
          type="textarea"
          :autosize="{ minRows: 3 }"
          placeholder="请输入审批意见（可选）"
          maxlength="255"
          show-word-limit
          class="approve-textarea"
        />
      </div>
      <div class="approve-footer">
        <el-button size="large" type="primary" :icon="CircleCloseIcon" :loading="submitLoading" @click="onBack()">
          返回
        </el-button>
        <el-button
          v-if="!details.allowActions?.length || details.allowActions.some((a) => a.kind === 1)"
          size="large"
          type="danger"
          :icon="CircleCloseIcon"
          :loading="submitLoading"
          @click="onApprove(1)"
        >
          {{ details.allowActions?.find((a) => a.kind === 1)?.name ?? "审批驳回" }}
        </el-button>
        <el-button
          v-if="!details.allowActions?.length || details.allowActions.some((a) => a.kind === 0)"
          size="large"
          type="success"
          :icon="CircleCheckIcon"
          :loading="submitLoading"
          @click="onApprove(0)"
        >
          {{ details.allowActions?.find((a) => a.kind === 0)?.name ?? "审批通过" }}
        </el-button>
        <el-button
          v-if="details.allowActions?.some((a) => a.kind === 2)"
          size="large"
          type="warning"
          :icon="CircleCloseIcon"
          :loading="submitLoading"
          @click="onApprove(2)"
        >
          {{ details.allowActions?.find((a) => a.kind === 2)?.name ?? "转交" }}
        </el-button>
        <el-button
          v-if="details.allowActions?.some((a) => a.kind === 3)"
          size="large"
          type="primary"
          :icon="CircleCloseIcon"
          :loading="submitLoading"
          @click="onApprove(3)"
        >
          {{ details.allowActions?.find((a) => a.kind === 3)?.name ?? "驳回节点" }}
        </el-button>
      </div>
    </div>
  </div>

  <!-- 转交人员选择器 -->
  <ModalUserSelector
    v-model="transferModalVisible"
    v-model:checked-user-ids="transferUserIds"
    mode="single"
    title="选择转交人员"
    @on-submit="onTransferSubmit"
  />
</template>

<script setup lang="ts">
import { markRaw, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { CircleCheck, CircleClose, EditPen } from "@element-plus/icons-vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext";
import ComTabService from "@/soa/com-series/service/ComTabService";
import ComOneTimeRouteContext from "@/soa/com-series/service/ComOneTimeRouteContext";
import QfTodoApi from "@/views/qf/api/QfTodoApi.ts";
import type { GetQfTodoDetailsVo, GetQfTodoListVo } from "@/views/qf/api/QfTodoApi.ts";
import QfProcDiagram from "@/views/qf/public/QfProcDiagram.vue";
import QfProcHistory from "@/views/qf/public/QfProcHistory.vue";
import QfProcForm from "@/views/qf/public/QfProcForm.vue";
import type { QfProcContext } from "@/views/qf/public/service/QfProcFormService";
import ModalUserSelector from "@/views/core/public/ModalUserSelector.vue";

const CircleCheckIcon = markRaw(CircleCheck);
const CircleCloseIcon = markRaw(CircleClose);

const DEFAULT_LABEL: Record<number, string> = { 0: "审批通过", 1: "审批驳回", 2: "转交", 3: "驳回节点" };

// 转交用户选择器状态
const transferModalVisible = ref(false);
const transferUserIds = ref<string[]>([]);
// 转交待提交的 action 暂存（固定为 2，预留扩展）
const pendingTransferAction = ref<number>(2);

const { getCdrcQuery } = ComDirectRouteContext.useDirectRouteContext();
const row = getCdrcQuery(false) as GetQfTodoListVo;

const { closeTab, activeTabId } = ComTabService.useTabService();
const { redirect } = ComOneTimeRouteContext.useOneTimeRouteContext();

const details = ref<GetQfTodoDetailsVo | null>(null);
const detailsLoading = ref(false);
const activeTab = ref("form");
const approveComment = ref("");
const submitLoading = ref(false);

// 审批前置拦截函数，由动态表单组件通过 procContext.registerBeforeSubmit 注入
const beforeSubmit = ref<((action: number) => Promise<string>) | null>(null);

// 流程上下文，完整内聚在此，通过 prop 传递给 QfProcForm → 动态表单组件
const procContext = reactive<QfProcContext>({
  registerBeforeSubmit: (fn) => {
    beforeSubmit.value = fn;
  },
  dataId: "",
  details: null,
  allowEditFields: [],
});

/** 关闭当前标签页并跳转回待办列表 */
const onBack = (): void => {
  closeTab(activeTabId.value);
  redirect("qfTodo");
};

/**
 * 执行审批操作；提交前先同步 procContext、弹确认框，再触发动态表单前置拦截，最后调接口
 * @param action 操作类型 0:同意 1:驳回 2:转交 3:驳回节点
 * @param memberId 转交时指定的目标用户ID
 */
const onApprove = async (action: number, memberId = ""): Promise<void> => {
  if (!details.value) {
    return;
  }

  // 转交操作先打开人员选择器，选完后由 onTransferSubmit 回调继续
  if (action === 2 && !memberId) {
    pendingTransferAction.value = action;
    transferUserIds.value = [];
    transferModalVisible.value = true;
    return;
  }

  procContext.dataId = details.value.dataId;
  procContext.details = details.value;
  procContext.allowEditFields = details.value.allowEditFields;

  const label = details.value.allowActions?.find((a) => a.kind === action)?.name ?? DEFAULT_LABEL[action];

  try {
    await ElMessageBox.confirm(`确定要「${label}」此待办吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
  } catch {
    return;
  }

  submitLoading.value = true;
  try {
    //通知动态注入的子组件
    const intercept = await beforeSubmit.value?.(action);

    if (intercept) {
      ElMessage.warning(intercept);
      return;
    }
    const msg = await QfTodoApi.approveQfTodo({
      id: details.value.id,
      action,
      comment: approveComment.value,
      nodeId: "",
      memberId,
    });
    ElMessage.success(msg || "操作成功");
    approveComment.value = "";
    closeTab(activeTabId.value);
    redirect("qfTodo");
  } catch (error) {
    ElMessage.error((error as Error).message ?? "操作失败");
  } finally {
    submitLoading.value = false;
  }
};

/** 人员选择器确认回调：取第一个选中用户ID，发起转交审批 */
const onTransferSubmit = (ids: string[]): void => {
  const memberId = ids[0] ?? "";
  if (!memberId) {
    ElMessage.warning("请选择转交人员");
    return;
  }
  transferModalVisible.value = false;
  onApprove(pendingTransferAction.value, memberId);
};

/** 加载待办详情；row 缺失或 routePc 未配置时关闭标签页并终止 */
onMounted(async () => {
  if (!row) {
    closeTab(activeTabId.value);
    return;
  }

  detailsLoading.value = true;
  try {
    details.value = await QfTodoApi.getQfTodoDetails({ id: row.id });

    //流程上下文赋值
    procContext.dataId = details.value.dataId;
    procContext.details = details.value;
    procContext.allowEditFields = details.value.allowEditFields;
  } catch (error) {
    ElMessage.error((error as Error).message ?? "加载失败");
    closeTab(activeTabId.value);
    return;
  } finally {
    detailsLoading.value = false;
  }
});
</script>

<style scoped>
.qf-approve-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.qf-approve-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 24px;
}

.qf-approve-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.qf-approve-tabs :deep(.el-tab-pane) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
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
</style>
