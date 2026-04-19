/**
 * QFD「流程其他配置」折叠块：candidateStarterUsers / candidateStarterGroups / historyTimeToLive
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
  useQfdPanelProcess(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref({
      candidateStarterUsers: "",
      candidateStarterGroups: "",
      historyTimeToLive: "",
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as { businessObject?: Record<string, unknown> } | null;
      const b = el?.businessObject;
      if (!b) {
        return;
      }
      form.value.candidateStarterUsers = (b.candidateStarterUsers as string) || "";
      form.value.candidateStarterGroups = (b.candidateStarterGroups as string) || "";
      form.value.historyTimeToLive = (b.historyTimeToLive as string) || "";
    }

    function onProcessOtherCommit(): void {
      const el = elementGetter() as { businessObject?: { $type?: string } } | null;
      if (el?.businessObject?.$type !== "bpmn:Process") {
        return;
      }
      const m = modelerGetter() as ModelerLike | null;
      const target = elementGetter() as { id?: string } | null;
      if (!m?.get || !target) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (t: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateProperties(target, {
        candidateStarterUsers: form.value.candidateStarterUsers || undefined,
        candidateStarterGroups: form.value.candidateStarterGroups || undefined,
        historyTimeToLive: form.value.historyTimeToLive || undefined,
      });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return { form, onProcessOtherCommit };
  },
};
