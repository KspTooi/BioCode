/**
 * QFD「任务配置」折叠块：dueDate / followUpDate / priority / skipExpression
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { ref, watch } from "vue";

type ModelerLike = { get: (name: string) => unknown };

export default {
  useQfdPanelUserTask(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref({
      dueDate: "",
      followUpDate: "",
      priority: "",
      skipExpression: "",
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as { businessObject?: Record<string, unknown> } | null;
      const b = el?.businessObject;
      if (!b) {
        return;
      }
      form.value.dueDate = (b.dueDate as string) || "";
      form.value.followUpDate = (b.followUpDate as string) || "";
      form.value.priority = (b.priority as string) || "";
      form.value.skipExpression = (b.skipExpression as string) || "";
    }

    function applyProps(patch: Record<string, unknown>): void {
      const m = modelerGetter() as ModelerLike | null;
      const el = elementGetter() as { id?: string } | null;
      if (!m?.get || !el) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateProperties(el, patch);
    }

    function onTaskCommit(): void {
      applyProps({
        dueDate: form.value.dueDate || undefined,
        followUpDate: form.value.followUpDate || undefined,
        priority: form.value.priority || undefined,
      });
    }

    function onOtherCommit(): void {
      applyProps({
        skipExpression: form.value.skipExpression || undefined,
      });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return { form, onTaskCommit, onOtherCommit };
  },
};
