/**
 * QFD「开始事件」折叠块：formKey / initiator
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
  useQfdPanelStartEvent(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref({
      formKey: "",
      initiator: "",
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as { businessObject?: Record<string, unknown> } | null;
      const b = el?.businessObject;
      if (!b) {
        return;
      }
      form.value.formKey = (b.formKey as string) || "";
      form.value.initiator = (b.initiator as string) || "";
    }

    function onStartEventCommit(): void {
      const m = modelerGetter() as ModelerLike | null;
      const el = elementGetter() as { id?: string } | null;
      if (!m?.get || !el) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateProperties(el, {
        formKey: form.value.formKey || undefined,
        initiator: form.value.initiator || undefined,
      });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return { form, onStartEventCommit };
  },
};
