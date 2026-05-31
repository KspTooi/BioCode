import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import QfProcApi from "@/views/qf/api/QfProcApi.ts";
import type { ApproveFlowRecordVo } from "@/views/qf/api/QfTodoApi.ts";

/**
 * 流转记录参数
 * 由父级在流转记录 tab 挂载时传入工程流程 ID
 *
 * 双向绑定 v-model 参数
 * （无，列表加载状态由组件内部维护）
 */
export interface QfProcHistoryProps {
  //引擎流程 ID（engProcId）
  procId: string;
}

export default {
  /**
   * 流转记录：拉取并按时间展示审批节点列表
   * @param props 流转记录参数，procId 为空时不请求
   */
  useQfProcHistory(props: QfProcHistoryProps) {
    const records = ref<ApproveFlowRecordVo[]>([]);
    const recordsLoading = ref(false);

    const loadRecords = async (): Promise<void> => {
      if (!props.procId) {
        return;
      }
      recordsLoading.value = true;
      try {
        records.value = await QfProcApi.getProcessApproveFlowRecord({ engProcId: props.procId });
      } catch (error) {
        records.value = [];
        ElMessage.error((error as Error).message ?? "流转记录加载失败");
      } finally {
        recordsLoading.value = false;
      }
    };

    onMounted(() => {
      void loadRecords();
    });

    return {
      records,
      recordsLoading,
    };
  },
};
