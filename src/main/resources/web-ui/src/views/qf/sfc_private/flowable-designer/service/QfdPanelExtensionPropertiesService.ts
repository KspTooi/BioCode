/**
 * QFD「扩展属性」折叠块：flowable:Property 的增删改与 commandStack 刷新
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { ref, shallowRef, watch } from "vue";
import { ElMessage } from "element-plus";

type BpmnEl = { businessObject?: Record<string, unknown> };

export type ExtPropRow = { name: string; value: string; raw: Record<string, unknown> };

export default {
  useQfdPanelExtensionProperties(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    const rows = shallowRef<ExtPropRow[]>([]);
    const dialogVisible = ref(false);
    const editing = ref<ExtPropRow | null>(null);
    const form = ref({ name: "", value: "" });

    let stackOff: (() => void) | null = null;

    function getM(): any {
      return modelerGetter();
    }

    function findPropertiesHolder(extValues: Record<string, unknown>[]): Record<string, unknown> | null {
      return extValues.find((v) => v?.$type === "flowable:Properties") ?? null;
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
      const holder = findPropertiesHolder(ext.values);
      if (!holder) {
        rows.value = [];
        return;
      }
      const vals = (holder.values as Record<string, unknown>[]) || [];
      rows.value = vals
        .filter((v) => v?.$type === "flowable:Property")
        .map((p) => ({
          name: (p.name as string) || "",
          value: (p.value as string) || "",
          raw: p,
        }));
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

    function openDialog(row?: ExtPropRow): void {
      editing.value = row ?? null;
      if (row) {
        form.value = { name: row.name, value: row.value };
        dialogVisible.value = true;
        return;
      }
      form.value = { name: "", value: "" };
      dialogVisible.value = true;
    }

    function ensureExtensionElements(m: any, processEl: BpmnEl, bo: Record<string, unknown>): Record<string, unknown> {
      let ext = bo.extensionElements as Record<string, unknown> | undefined;
      if (ext) {
        return ext;
      }
      const moddle = m.get("moddle");
      const modeling = m.get("modeling");
      ext = moddle.create("bpmn:ExtensionElements", { values: [] });
      modeling.updateModdleProperties(processEl, bo, { extensionElements: ext });
      return ext as Record<string, unknown>;
    }

    function ensurePropertiesContainer(m: any, processEl: BpmnEl, ext: Record<string, unknown>): Record<string, unknown> {
      const values = [...((ext.values as Record<string, unknown>[]) || [])];
      const existing = findPropertiesHolder(values);
      if (existing) {
        return existing;
      }
      const moddle = m.get("moddle");
      const modeling = m.get("modeling");
      const propsEl = moddle.create("flowable:Properties", { values: [] });
      values.push(propsEl);
      modeling.updateModdleProperties(processEl, ext, { values });
      return propsEl;
    }

    function save(): void {
      const m = getM();
      const processEl = elementGetter() as BpmnEl | null;
      if (!m || !processEl?.businessObject) {
        return;
      }
      const name = form.value.name?.trim();
      if (!name) {
        ElMessage.warning("名称不能为空");
        return;
      }
      const bo = processEl.businessObject;
      const modeling = m.get("modeling");
      const moddle = m.get("moddle");
      const ext = ensureExtensionElements(m, processEl, bo);
      const propsContainer = ensurePropertiesContainer(m, processEl, ext);
      const propValues = [...((propsContainer.values as Record<string, unknown>[]) || [])];
      const editRow = editing.value;
      if (editRow) {
        modeling.updateModdleProperties(processEl, editRow.raw, {
          name,
          value: form.value.value || undefined,
        });
        dialogVisible.value = false;
        refresh();
        return;
      }
      const prop = moddle.create("flowable:Property", { name, value: form.value.value || undefined });
      propValues.push(prop);
      modeling.updateModdleProperties(processEl, propsContainer, { values: propValues });
      dialogVisible.value = false;
      refresh();
    }

    function removeRow(row: ExtPropRow): void {
      const m = getM();
      const processEl = elementGetter() as BpmnEl | null;
      if (!m || !processEl?.businessObject) {
        return;
      }
      const bo = processEl.businessObject;
      const ext = bo.extensionElements as { values?: Record<string, unknown>[] } | undefined;
      if (!ext?.values?.length) {
        return;
      }
      const holder = findPropertiesHolder(ext.values);
      if (!holder?.values) {
        return;
      }
      const modeling = m.get("modeling");
      const next = (holder.values as Record<string, unknown>[]).filter((v) => v !== row.raw);
      modeling.updateModdleProperties(processEl, holder, { values: next });
      refresh();
    }

    return {
      rows,
      dialogVisible,
      editing,
      form,
      openDialog,
      save,
      removeRow,
      dispose,
    };
  },
};
