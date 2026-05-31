import { nextTick, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import QfProcApi from "@/views/qf/api/QfProcApi.ts";
import { useFlowableModeler } from "@/views/qf/sfc_private/flowable-designer/UseFlowableModeler";

/**
 * 流程图跟踪参数
 * 只读 BPMN 流程图展示，由父级在流程图 tab 挂载时传入工程流程 ID
 *
 * 双向绑定 v-model 参数
 * （无，流程图加载与错误状态由组件内部维护）
 */
export interface QfProcDiagramProps {
  //工程流程 ID（engProcId）
  procId: string;
}

export default {
  /**
   * 流程图跟踪：拉取 BPMN、初始化只读 viewer 并渲染
   * @param props 流程图参数，procId 为空时不请求
   */
  useQfProcDiagram(props: QfProcDiagramProps) {
    const diagramContainer = ref<HTMLElement | null>(null);
    const flowLoading = ref(false);
    const flowLoadError = ref<string | null>(null);
    let diagramInitialized = false;

    const { init: initViewer, importXml, zoomFit } = useFlowableModeler(diagramContainer, true);

    const loadDiagram = async (): Promise<void> => {
      if (diagramInitialized) {
        return;
      }
      if (!props.procId) {
        return;
      }
      diagramInitialized = true;
      flowLoading.value = true;
      flowLoadError.value = null;
      try {
        const xml = await QfProcApi.getProcessApproveFlow({ engProcId: props.procId });
        await nextTick();
        initViewer();
        await importXml(xml);
        zoomFit();
      } catch (error) {
        const msg = (error as Error).message ?? "流程图加载失败";
        flowLoadError.value = msg;
        ElMessage.error(msg);
      } finally {
        flowLoading.value = false;
      }
    };

    onMounted(() => {
      void loadDiagram();
    });

    return {
      diagramContainer,
      flowLoading,
      flowLoadError,
    };
  },
};
