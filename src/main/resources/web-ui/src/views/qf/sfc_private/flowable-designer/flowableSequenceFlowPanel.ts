import type { Ref } from "vue";

/** 连线条件模板，与后端 complete 时 vars.approved 约定一致 */
export type ConditionPreset = "none" | "approve" | "reject" | "custom";

export interface SequenceFlowFormSlice {
  conditionPreset: ConditionPreset;
  conditionExpression: string;
  isDefault: boolean;
}

export function resetSequenceFlowFormFields(form: Ref<{ conditionPreset: ConditionPreset; conditionExpression: string; isDefault: boolean }>): void {
  form.value.conditionPreset = "none";
  form.value.conditionExpression = "";
  form.value.isDefault = false;
}

export function loadSequenceFlowFromBo(
  b: Record<string, unknown>,
  targetElement: unknown,
  form: Ref<SequenceFlowFormSlice>
): void {
  const ce = b.conditionExpression as { body?: string } | undefined;
  const body = (ce?.body as string | undefined)?.trim() ?? "";
  form.value.conditionExpression = body;
  if (!body) {
    form.value.conditionPreset = "none";
  }
  if (body === "${approved == true}") {
    form.value.conditionPreset = "approve";
  }
  if (body === "${approved == false}") {
    form.value.conditionPreset = "reject";
  }
  if (body.length > 0 && form.value.conditionPreset === "none") {
    form.value.conditionPreset = "custom";
  }
  const el = targetElement as { source?: { businessObject?: { default?: { id?: string } | string } } } | null;
  const srcBo = el?.source?.businessObject;
  const flowId = (b.id as string) || "";
  if (!srcBo || !flowId || srcBo.default == null) {
    return;
  }
  const d = srcBo.default as { id?: string } | string;
  const defId = typeof d === "string" ? d : d?.id;
  form.value.isDefault = defId === flowId;
}

export function onConditionPresetChange(
  elementType: string,
  preset: ConditionPreset,
  form: Ref<SequenceFlowFormSlice>,
  commitCondition: () => void
): void {
  if (elementType !== "bpmn:SequenceFlow") {
    return;
  }
  if (preset !== "custom") {
    const presetMap: Record<string, string> = {
      none: "",
      approve: "${approved == true}",
      reject: "${approved == false}",
    };
    form.value.conditionExpression = presetMap[preset] ?? "";
  }
  commitCondition();
}

export function commitSequenceFlowCondition(
  elementType: string,
  getModeler: () => unknown,
  applyProps: (patch: Record<string, unknown>) => void,
  form: Ref<{ conditionExpression: string }>
): void {
  if (elementType !== "bpmn:SequenceFlow") {
    return;
  }
  const m = getModeler() as { get: (name: string) => unknown } | null;
  if (!m) {
    return;
  }
  const moddle = m.get("moddle") as { create: (type: string, props: { body: string }) => unknown };
  const body = form.value.conditionExpression?.trim();
  const expr = body ? moddle.create("bpmn:FormalExpression", { body }) : undefined;
  applyProps({ conditionExpression: expr });
}

export function commitDefaultFlow(
  elementType: string,
  targetElement: unknown,
  isDefault: boolean,
  getModeler: () => unknown
): void {
  if (elementType !== "bpmn:SequenceFlow") {
    return;
  }
  const m = getModeler() as { get: (name: string) => unknown } | null;
  const el = targetElement as { source?: unknown; businessObject?: unknown } | null;
  if (!m || !el?.source) {
    return;
  }
  const modeling = m.get("modeling") as { updateProperties: (shape: unknown, props: Record<string, unknown>) => void };
  const sourceShape = el.source;
  if (!isDefault) {
    modeling.updateProperties(sourceShape, { default: undefined });
    return;
  }
  modeling.updateProperties(sourceShape, { default: el.businessObject });
}
