/**
 * QFD 属性面板主容器：选择态、目标元素、标题与折叠状态
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { computed, ref, shallowRef, watch, type Ref } from "vue";
import { findProcessElement } from "@/views/qf/sfc_private/flowable-designer/FlowableModelUtils";

type BpmnModelerLike = { get: (name: string) => unknown };

export default {
  useQfdPanel(modelerRef: Ref<unknown>) {
    const selected = shallowRef<unknown>(null);

    function getModeler(): BpmnModelerLike | null {
      const m = modelerRef.value;
      if (!m || typeof (m as BpmnModelerLike).get !== "function") {
        return null;
      }
      return m as BpmnModelerLike;
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

    const panelTitle = computed(() => {
      if (elementType.value === "bpmn:Process") {
        return "流程全局配置";
      }
      return "节点属性配置: " + elementType.value;
    });

    const activeNames = ref<string[]>([]);

    let selectionOff: (() => void) | null = null;

    function dispose(): void {
      if (!selectionOff) {
        return;
      }
      selectionOff();
      selectionOff = null;
    }

    watch(
      modelerRef,
      (m) => {
        dispose();
        if (!m) {
          selected.value = null;
          return;
        }
        const eventBus = (m as BpmnModelerLike).get("eventBus") as {
          on: (ev: string, fn: (e: { newSelection?: unknown[] }) => void) => void;
          off: (ev: string, fn: (e: { newSelection?: unknown[] }) => void) => void;
        };
        const handler = (e: { newSelection?: unknown[] }): void => {
          selected.value = e.newSelection?.[0] ?? null;
        };
        eventBus.on("selection.changed", handler);
        selectionOff = () => {
          eventBus.off("selection.changed", handler);
        };
      },
      { immediate: true }
    );

    return {
      selected,
      targetElement,
      bo,
      elementType,
      panelTitle,
      activeNames,
      dispose,
    };
  },
};
