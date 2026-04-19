/**
 * QFD「任务监听器」折叠块：flowable:TaskListener 的增删改与 commandStack 刷新
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { computed, ref, shallowRef, watch } from "vue";

type BpmnEl = { businessObject?: Record<string, unknown> };
type ImplKind = "class" | "expression" | "delegateExpression";

export type TaskListenerRow = {
  event: string;
  implType: string;
  implValue: string;
  raw: Record<string, unknown>;
};

export default {
  useQfdPanelTaskListeners(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const rows = shallowRef<TaskListenerRow[]>([]);
    const dialogVisible = ref(false);
    const editing = ref<TaskListenerRow | null>(null);
    const form = ref<{ event: string; implKind: ImplKind; implText: string }>({
      event: "create",
      implKind: "class",
      implText: "",
    });

    const implLabel = computed(() => {
      if (form.value.implKind === "class") {
        return "Java 类";
      }
      if (form.value.implKind === "expression") {
        return "表达式";
      }
      return "委托表达式";
    });

    let stackOff: (() => void) | null = null;

    function getM(): any {
      return modelerGetter();
    }

    function isTaskListener(v: Record<string, unknown>): boolean {
      return v?.$type === "flowable:TaskListener";
    }

    function readImpl(listener: Record<string, unknown>): { kind: string; text: string } {
      const cls = listener.class as string | undefined;
      if (cls) {
        return { kind: "Java 类", text: cls };
      }
      const ex = listener.expression as string | undefined;
      if (ex) {
        return { kind: "表达式", text: ex };
      }
      const de = listener.delegateExpression as string | undefined;
      if (de) {
        return { kind: "委托表达式", text: de };
      }
      return { kind: "-", text: "" };
    }

    function toRow(listener: Record<string, unknown>): TaskListenerRow {
      const impl = readImpl(listener);
      return {
        event: (listener.event as string) || "",
        implType: impl.kind,
        implValue: impl.text,
        raw: listener,
      };
    }

    function refresh(): void {
      const el = elementGetter() as BpmnEl | null;
      if (!el?.businessObject) {
        rows.value = [];
        return;
      }
      const bo = el.businessObject;
      const ext = bo.extensionElements as { values?: Record<string, unknown>[] } | undefined;
      if (!ext?.values?.length) {
        rows.value = [];
        return;
      }
      rows.value = ext.values.filter((v) => isTaskListener(v)).map(toRow);
    }

    function bindStack(): void {
      if (stackOff) {
        stackOff();
        stackOff = null;
      }
      const m = getM();
      if (!m) {
        return;
      }
      const eventBus = m.get("eventBus");
      const handler = (): void => {
        refresh();
      };
      eventBus.on("commandStack.changed", handler);
      stackOff = () => {
        eventBus.off("commandStack.changed", handler);
      };
    }

    function dispose(): void {
      if (!stackOff) {
        return;
      }
      stackOff();
      stackOff = null;
    }

    watch(
      () => [modelerGetter(), elementGetter()],
      () => {
        bindStack();
        refresh();
      },
      { immediate: true, deep: true }
    );

    function openDialog(row?: TaskListenerRow): void {
      editing.value = row ?? null;
      if (!row) {
        form.value = { event: "create", implKind: "class", implText: "" };
        dialogVisible.value = true;
        return;
      }
      const raw = row.raw;
      form.value.event = (raw.event as string) || "create";
      const cls = raw.class as string | undefined;
      if (cls) {
        form.value.implKind = "class";
        form.value.implText = cls;
        dialogVisible.value = true;
        return;
      }
      const ex = raw.expression as string | undefined;
      if (ex) {
        form.value.implKind = "expression";
        form.value.implText = ex;
        dialogVisible.value = true;
        return;
      }
      const de = raw.delegateExpression as string | undefined;
      if (de) {
        form.value.implKind = "delegateExpression";
        form.value.implText = de;
        dialogVisible.value = true;
        return;
      }
      form.value.implKind = "class";
      form.value.implText = "";
      dialogVisible.value = true;
    }

    function ensureExtensionElements(m: any, diagramEl: BpmnEl, bo: Record<string, unknown>): Record<string, unknown> {
      let ext = bo.extensionElements as Record<string, unknown> | undefined;
      if (ext) {
        return ext;
      }
      const moddle = m.get("moddle");
      const modeling = m.get("modeling");
      ext = moddle.create("bpmn:ExtensionElements", { values: [] });
      modeling.updateModdleProperties(diagramEl, bo, { extensionElements: ext });
      return ext as Record<string, unknown>;
    }

    function buildListenerProps(): Record<string, unknown> {
      const base: Record<string, unknown> = {
        event: form.value.event,
        class: undefined,
        expression: undefined,
        delegateExpression: undefined,
      };
      const text = form.value.implText?.trim();
      if (form.value.implKind === "class") {
        base.class = text || undefined;
        return base;
      }
      if (form.value.implKind === "expression") {
        base.expression = text || undefined;
        return base;
      }
      base.delegateExpression = text || undefined;
      return base;
    }

    function save(): void {
      const m = getM();
      const diagramEl = elementGetter() as BpmnEl | null;
      if (!m || !diagramEl?.businessObject) {
        return;
      }
      const bo = diagramEl.businessObject;
      const modeling = m.get("modeling");
      const moddle = m.get("moddle");
      const ext = ensureExtensionElements(m, diagramEl, bo);
      const values = [...((ext.values as Record<string, unknown>[]) || [])];
      const propsPatch = buildListenerProps();
      const editRow = editing.value;
      if (editRow) {
        modeling.updateModdleProperties(diagramEl, editRow.raw, propsPatch);
        dialogVisible.value = false;
        refresh();
        return;
      }
      const listener = moddle.create("flowable:TaskListener", propsPatch);
      values.push(listener);
      modeling.updateModdleProperties(diagramEl, ext, { values });
      dialogVisible.value = false;
      refresh();
    }

    function removeRow(row: TaskListenerRow): void {
      const m = getM();
      const diagramEl = elementGetter() as BpmnEl | null;
      if (!m || !diagramEl?.businessObject) {
        return;
      }
      const bo = diagramEl.businessObject;
      const ext = bo.extensionElements as { values?: Record<string, unknown>[] } | undefined;
      if (!ext?.values) {
        return;
      }
      const modeling = m.get("modeling");
      const values = ext.values.filter((v) => v !== row.raw);
      modeling.updateModdleProperties(diagramEl, ext, { values });
      refresh();
    }

    return {
      rows,
      dialogVisible,
      editing,
      form,
      implLabel,
      openDialog,
      save,
      removeRow,
      dispose,
    };
  },
};
