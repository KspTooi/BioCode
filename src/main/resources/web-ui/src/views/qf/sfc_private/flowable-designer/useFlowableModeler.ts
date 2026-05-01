import { onBeforeUnmount, ref, shallowRef, type Ref, type ShallowRef } from "vue";
import BpmnModeler from "bpmn-js/lib/Modeler";
import NavigatedViewer from "bpmn-js/lib/NavigatedViewer";
import {
  getFlowableModdleExtensions,
  buildEmptyFlowableDiagram,
} from "@/views/qf/sfc_private/flowable-designer/FlowableEngine.ts";
import translateModule from "@/views/qf/sfc_private/flowable-designer/TranslateModule.ts";

export type BpmnElement = {
  id: string;
  type: string;
  businessObject: Record<string, unknown>;
};

export type ModelStatus = {
  valid: boolean;
  issues: string[];
};

export type UseFlowableModelerReturn = {
  modeler: ShallowRef<InstanceType<typeof BpmnModeler> | null>;
  defaultZoom: Ref<number>;
  revocable: Ref<boolean>;
  recoverable: Ref<boolean>;
  modelStatus: Ref<ModelStatus>;
  init: () => void;
  destroy: () => void;
  importXml: (xml: string) => Promise<void>;
  saveXml: (formatted?: boolean) => Promise<string | undefined>;
  saveSvg: () => Promise<string | undefined>;
  zoomIn: (step?: number) => void;
  zoomOut: (step?: number) => void;
  zoomFit: () => void;
  undo: () => void;
  redo: () => void;
  newDiagram: (processId: string, processName: string) => Promise<void>;
  syncCommandStack: () => void;
};

const TASK_TYPES = new Set([
  "bpmn:UserTask",
  "bpmn:ServiceTask",
  "bpmn:ScriptTask",
  "bpmn:Task",
  "bpmn:SubProcess",
  "bpmn:CallActivity",
]);

function checkEventCount(issues: string[], startEvents: any[], endEvents: any[]): void {
  if (startEvents.length === 0) {
    issues.push("缺少开始事件");
  }
  if (startEvents.length > 1) {
    issues.push("存在多个开始事件");
  }
  if (endEvents.length === 0) {
    issues.push("缺少结束事件");
  }
}

function checkConnections(
  issues: string[],
  elements: any[],
  label: string,
  needIncoming: boolean,
  needOutgoing: boolean
): void {
  for (const el of elements) {
    const bo = el.businessObject;
    const name = bo?.name || el.id;
    if (needIncoming && ((bo?.incoming ?? []) as any[]).length === 0) {
      issues.push(`${label} [${name}] 没有入口连线`);
    }
    if (needOutgoing && ((bo?.outgoing ?? []) as any[]).length === 0) {
      issues.push(`${label} [${name}] 没有出口连线`);
    }
  }
}

function collectModelIssues(m: InstanceType<typeof BpmnModeler>): string[] {
  const issues: string[] = [];
  const registry = m.get("elementRegistry") as {
    filter: (fn: (e: any) => boolean) => any[];
  };
  const allElements = registry.filter(() => true);

  const startEvents = allElements.filter((e: any) => e.type === "bpmn:StartEvent");
  const endEvents = allElements.filter((e: any) => e.type === "bpmn:EndEvent");
  const tasks = allElements.filter((e: any) => TASK_TYPES.has(e.type));
  const gateways = allElements.filter((e: any) => e.type?.includes("Gateway"));

  checkEventCount(issues, startEvents, endEvents);
  checkConnections(issues, startEvents, "开始事件", false, true);
  checkConnections(issues, endEvents, "结束事件", true, false);
  checkConnections(issues, tasks, "任务", true, true);
  checkConnections(issues, gateways, "网关", true, true);

  return issues;
}

export function useFlowableModeler(containerRef: Ref<HTMLElement | null>, readonly = false): UseFlowableModelerReturn {
  const modeler = shallowRef<InstanceType<typeof BpmnModeler> | null>(null);
  const defaultZoom = ref(1);
  const revocable = ref(false);
  const recoverable = ref(false);
  const modelStatus = ref<ModelStatus>({ valid: false, issues: ["模型未加载"] });

  const validateModel = (): void => {
    const m = modeler.value;
    if (!m) {
      modelStatus.value = { valid: false, issues: ["模型未加载"] };
      return;
    }
    try {
      const issues = collectModelIssues(m);
      modelStatus.value = { valid: issues.length === 0, issues };
    } catch {
      modelStatus.value = { valid: false, issues: ["模型解析异常"] };
    }
  };

  const syncCommandStack = (): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    if (readonly) {
      validateModel();
      return;
    }
    const commandStack = m.get("commandStack") as { canUndo: () => boolean; canRedo: () => boolean };
    revocable.value = commandStack.canUndo();
    recoverable.value = commandStack.canRedo();
    validateModel();
  };

  const init = (): void => {
    const el = containerRef.value;
    if (!el) {
      return;
    }
    if (modeler.value) {
      return;
    }
    const commonOptions = {
      container: el,
      additionalModules: [translateModule],
      moddleExtensions: getFlowableModdleExtensions(),
      bpmnRenderer: {
        defaultFillColor: "#ffffff",
        defaultStrokeColor: "#333333",
        defaultLabelColor: "#222222",
      },
    };
    const m: any = readonly ? new NavigatedViewer(commonOptions) : new BpmnModeler({ ...commonOptions, keyboard: {} });
    modeler.value = m;
    const eventBus = m.get("eventBus");
    if (!readonly) {
      eventBus.on("commandStack.changed", syncCommandStack);
    }
    m.on("canvas.viewbox.changed", ({ viewbox }: { viewbox: { scale: number } }) => {
      defaultZoom.value = Math.round(viewbox.scale * 100) / 100;
    });
    syncCommandStack();
  };

  const destroy = (): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    m.destroy();
    modeler.value = null;
  };

  onBeforeUnmount(() => {
    destroy();
  });

  const importXml = async (xml: string): Promise<void> => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    try {
      const result = await m.importXML(xml);
      if (result.warnings?.length) {
        result.warnings.forEach((w: unknown) => console.warn(w));
      }
    } catch (e) {
      console.error(e);
      throw e;
    }
    syncCommandStack();
    validateModel();
  };

  const saveXml = async (formatted = true): Promise<string | undefined> => {
    const m = modeler.value;
    if (!m) {
      return undefined;
    }
    const { xml } = await m.saveXML({ format: formatted });
    return xml as string;
  };

  const saveSvg = async (): Promise<string | undefined> => {
    const m = modeler.value;
    if (!m) {
      return undefined;
    }
    const { svg } = await m.saveSVG();
    return svg;
  };

  const zoomIn = (step = 0.1): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    const canvas = m.get("canvas") as { zoom: (v: number) => void };
    const z = Math.min(4, defaultZoom.value + step);
    defaultZoom.value = Math.round(z * 100) / 100;
    canvas.zoom(defaultZoom.value);
  };

  const zoomOut = (step = 0.1): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    const canvas = m.get("canvas") as { zoom: (v: number) => void };
    const z = Math.max(0.2, defaultZoom.value - step);
    defaultZoom.value = Math.round(z * 100) / 100;
    canvas.zoom(defaultZoom.value);
  };

  const zoomFit = (): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    const canvas = m.get("canvas") as { zoom: (v: string, v2: string) => void };
    defaultZoom.value = 1;
    canvas.zoom("fit-viewport", "auto");
  };

  const undo = (): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    (m.get("commandStack") as { undo: () => void }).undo();
  };

  const redo = (): void => {
    const m = modeler.value;
    if (!m) {
      return;
    }
    (m.get("commandStack") as { redo: () => void }).redo();
  };

  const newDiagram = async (processId: string, processName: string): Promise<void> => {
    await importXml(buildEmptyFlowableDiagram(processId, processName));
  };

  return {
    modeler,
    defaultZoom,
    revocable,
    recoverable,
    modelStatus,
    init,
    destroy,
    importXml,
    saveXml,
    saveSvg,
    zoomIn,
    zoomOut,
    zoomFit,
    undo,
    redo,
    newDiagram,
    syncCommandStack,
  };
}
