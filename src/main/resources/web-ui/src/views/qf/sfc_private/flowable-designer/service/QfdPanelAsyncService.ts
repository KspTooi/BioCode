/**
 * QFD「异步与独占」折叠块：async/asyncBefore/asyncAfter/exclusive
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
  useQfdPanelAsync(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref({
      async: false,
      asyncBefore: false,
      asyncAfter: false,
      exclusive: true,
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as { businessObject?: Record<string, unknown> } | null;
      const b = el?.businessObject;
      if (!b) {
        return;
      }
      form.value.async = Boolean(b.async);
      form.value.asyncBefore = Boolean(b.asyncBefore);
      form.value.asyncAfter = Boolean(b.asyncAfter);
      form.value.exclusive = b.exclusive !== false;
    }

    function onAsyncCommit(): void {
      const m = modelerGetter() as ModelerLike | null;
      const el = elementGetter() as { id?: string } | null;
      if (!m?.get || !el) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateProperties(el, {
        async: form.value.async,
        asyncBefore: form.value.asyncBefore,
        asyncAfter: form.value.asyncAfter,
        exclusive: form.value.exclusive,
      });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return { form, onAsyncCommit };
  },
};
