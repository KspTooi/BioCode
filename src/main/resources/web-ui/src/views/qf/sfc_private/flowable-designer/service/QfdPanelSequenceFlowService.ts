/**
 * QFD「流转条件」折叠块：条件模板、条件表达式、默认流
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { ref, watch } from "vue";

type ModelerLike = { get: (name: string) => unknown };

export type ConditionPreset = "none" | "approve" | "reject" | "custom";

const PRESET_EXPR_MAP: Record<string, string> = {
  none: "",
  approve: "${approved == true}",
  reject: "${approved == false}",
};

export default {
  useQfdPanelSequenceFlow(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const form = ref<{ conditionPreset: ConditionPreset; conditionExpression: string; isDefault: boolean }>({
      conditionPreset: "none",
      conditionExpression: "",
      isDefault: false,
    });

    function loadFormFromBo(): void {
      const el = elementGetter() as {
        source?: { businessObject?: { default?: { id?: string } | string } };
        businessObject?: Record<string, unknown>;
      } | null;
      const b = el?.businessObject;
      if (!b) {
        form.value.conditionPreset = "none";
        form.value.conditionExpression = "";
        form.value.isDefault = false;
        return;
      }

      // 读取条件表达式 body
      const ce = b.conditionExpression as { body?: string } | undefined;
      const body = (ce?.body as string | undefined)?.trim() ?? "";
      form.value.conditionExpression = body;

      if (!body) {
        form.value.conditionPreset = "none";
      } else if (body === "${approved == true}") {
        form.value.conditionPreset = "approve";
      } else if (body === "${approved == false}") {
        form.value.conditionPreset = "reject";
      } else {
        form.value.conditionPreset = "custom";
      }

      // 从上游网关的 default 推断当前连线是否为默认流
      const flowId = (b.id as string) || "";
      const srcBo = el?.source?.businessObject;
      if (!srcBo || !flowId || srcBo.default == null) {
        form.value.isDefault = false;
        return;
      }
      const d = srcBo.default as { id?: string } | string;
      const defId = typeof d === "string" ? d : d?.id;
      form.value.isDefault = defId === flowId;
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

    function commitCondition(): void {
      const m = modelerGetter() as ModelerLike | null;
      if (!m?.get) {
        return;
      }
      const moddle = m.get("moddle") as { create: (type: string, props: { body: string }) => unknown };
      const body = form.value.conditionExpression?.trim();
      const expr = body ? moddle.create("bpmn:FormalExpression", { body }) : undefined;
      applyProps({ conditionExpression: expr });
    }

    function onConditionPresetChange(preset: ConditionPreset): void {
      if (preset !== "custom") {
        form.value.conditionExpression = PRESET_EXPR_MAP[preset] ?? "";
      }
      commitCondition();
    }

    function commitDefault(): void {
      const m = modelerGetter() as ModelerLike | null;
      const el = elementGetter() as { source?: unknown; businessObject?: unknown } | null;
      if (!m?.get || !el?.source) {
        return;
      }
      const modeling = m.get("modeling") as {
        updateProperties: (target: unknown, props: Record<string, unknown>) => void;
      };
      if (!form.value.isDefault) {
        modeling.updateProperties(el.source, { default: undefined });
        return;
      }
      modeling.updateProperties(el.source, { default: el.businessObject });
    }

    watch(
      () => elementGetter(),
      () => {
        loadFormFromBo();
      },
      { immediate: true }
    );

    return { form, onConditionPresetChange, commitCondition, commitDefault };
  },
};
