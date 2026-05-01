import { is } from "bpmn-js/lib/util/ModelUtil";

/** 从画布根节点解析 Definitions（简单流程图为 Process.$parent） */
export function getDefinitions(modeler: { get: (name: string) => unknown }): Record<string, unknown> | null {
  const canvas = modeler.get("canvas") as { getRootElement: () => { businessObject?: Record<string, unknown> } | null };
  const root = canvas.getRootElement();
  if (!root?.businessObject) {
    return null;
  }
  const bo = root.businessObject;
  if (bo.$type === "bpmn:Definitions") {
    return bo as Record<string, unknown>;
  }
  const parent = bo.$parent as Record<string, unknown> | undefined;
  if (parent?.$type === "bpmn:Definitions") {
    return parent;
  }
  return null;
}

export function findProcessElement(modeler: { get: (name: string) => unknown }): unknown {
  const registry = modeler.get("elementRegistry") as {
    getAll: () => Array<{ type: string; businessObject: unknown }>;
  };
  const list = registry.getAll();
  const direct = list.find((e) => e.type === "bpmn:Process");
  if (direct) {
    return direct;
  }
  return list.find((e) => is(e.businessObject, "bpmn:Process")) ?? null;
}

export function generateBpmnLocalPart(prefix: string): string {
  return `${prefix}${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`;
}
