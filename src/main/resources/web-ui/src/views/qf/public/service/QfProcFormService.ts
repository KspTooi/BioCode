import { onMounted, shallowRef, type Component } from "vue";
import { ElMessage } from "element-plus";
import ComPublicCompService from "@/soa/com-series/service/ComPublicCompService";
import type { GetQfTodoDetailsVo } from "@/views/qf/api/QfTodoApi.ts";

/**
 * 流程审批表单参数
 * 仅用于控制动态表单组件的渲染
 */
export interface QfProcFormProps {
  // 待办详情，含 dataId、routePc、allowEditFields 等
  details?: GetQfTodoDetailsVo | null;

  // 外部传入的流程上下文，透传给动态表单组件
  procContext?: QfProcContext | null;

  // 详情加载状态，用于控制遮罩与 Empty 显隐
  loading?: boolean;
}

/**
 * 流程上下文
 * 提供审批操作的上下文，这些参数会提供给动态表单组件
 */
export interface QfProcContext {
  /**
   * 注册审批前置事件
   * @param fn 前置拦截函数；返回非空字符串则中断审批并展示该文本
   */
  registerBeforeSubmit: (fn: (action: number) => Promise<string>) => void;

  dataId: string; // 数据ID

  details: GetQfTodoDetailsVo; // 待办详情

  allowEditFields: string[]; // 允许编辑的字段
}

export default {
  /**
   * 解析并挂载动态表单组件；仅负责渲染，不含审批逻辑
   * @param props 包含 details 的表单 props
   */
  useQfProcForm(props: QfProcFormProps) {
    const { resolvePublicComp } = ComPublicCompService.usePublicComp();
    const formComponent = shallowRef<Component | null>(null);

    onMounted(() => {
      formComponent.value = null;
      const routePc = props.details?.routePc;
      if (!routePc) {
        return;
      }
      const comp = resolvePublicComp(routePc);
      if (!comp) {
        ElMessage.warning("流程表单组件未发布或已下线，请联系管理员处理。");
        return;
      }
      formComponent.value = comp;
    });

    return { formComponent };
  },
};
