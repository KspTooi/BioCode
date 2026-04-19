/**
 * QFD「通用」折叠块：ID、名称、流程版本标签与可执行
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";

type ModelerLike = { get: (name: string) => unknown };

export default {
  useQfdPanelGeneral(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref({
      id: "",
      name: "",
      versionTag: "",
      isExecutable: true,
    });

    const elementType = computed(() => {
      const el = elementGetter() as { businessObject?: { $type?: string } } | null;
      if (!el?.businessObject?.$type) {
        return "";
      }
      return el.businessObject.$type;
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as { businessObject?: Record<string, unknown> } | null;
      const b = el?.businessObject;
      if (!b) {
        return;
      }
      form.value.id = (b.id as string) || "";
      form.value.name = (b.name as string) || "";
      form.value.versionTag = (b.versionTag as string) || "";
      if (b.$type !== "bpmn:Process") {
        form.value.isExecutable = true;
        return;
      }
      form.value.isExecutable = b.isExecutable === undefined ? true : Boolean(b.isExecutable);
    }

    function applyProps(patch: Record<string, unknown>): void {
      const m = modelerGetter() as ModelerLike | null;
      const el = elementGetter() as { id?: string } | null;
      if (!m?.get) {
        return;
      }
      if (!el) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateProperties(el, patch);
    }

    function onIdCommit(): void {
      const m = modelerGetter() as ModelerLike | null;
      if (!m?.get) {
        return;
      }
      const el = elementGetter() as { id?: string } | null;
      if (!el?.id) {
        return;
      }
      const newId = form.value.id?.trim();
      if (!newId) {
        ElMessage.warning("ID 不能为空");
        loadFormFromBo();
        return;
      }
      if (newId === el.id) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: { id: string }) => void;
      };
      modeling.updateProperties(el, { id: newId });
    }

    function onNameCommit(): void {
      applyProps({ name: form.value.name });
    }

    function onProcessGeneralCommit(): void {
      if (elementType.value !== "bpmn:Process") {
        return;
      }
      applyProps({
        versionTag: form.value.versionTag || undefined,
        isExecutable: form.value.isExecutable,
      });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return {
      form,
      elementType,
      onIdCommit,
      onNameCommit,
      onProcessGeneralCommit,
    };
  },
};
