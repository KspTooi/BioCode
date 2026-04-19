/**
 * QFD「消息与信号」折叠块：消息/信号双表格 + 双弹窗
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { ref, shallowRef, watch } from "vue";
import { is } from "bpmn-js/lib/util/ModelUtil";
import { ElMessage } from "element-plus";
import {
  findProcessElement,
  generateBpmnLocalPart,
  getDefinitions,
} from "@/views/qf/sfc_private/flowable-designer/flowableModelUtils";

type ModelerLike = { get: (name: string) => unknown };

export type MsgSignalRow = { id: string; name: string; raw: Record<string, unknown> };

export default {
  useQfdPanelMsgAndSignals(modelerGetter: () => unknown) {
    const messageRows = shallowRef<MsgSignalRow[]>([]);
    const signalRows = shallowRef<MsgSignalRow[]>([]);

    const messageDialogVisible = ref(false);
    const signalDialogVisible = ref(false);
    const messageEditing = ref<MsgSignalRow | null>(null);
    const signalEditing = ref<MsgSignalRow | null>(null);
    const messageForm = ref({ id: "", name: "" });
    const signalForm = ref({ id: "", name: "" });

    let stackOff: (() => void) | null = null;

    function toRow(el: Record<string, unknown>): MsgSignalRow {
      return {
        id: (el.id as string) || "",
        name: (el.name as string) || "",
        raw: el,
      };
    }

    function refresh(): void {
      const m = modelerGetter() as ModelerLike | null;
      if (!m) {
        messageRows.value = [];
        signalRows.value = [];
        return;
      }
      const definitions = getDefinitions(m) as { rootElements?: unknown[] } | null;
      if (!definitions?.rootElements?.length) {
        messageRows.value = [];
        signalRows.value = [];
        return;
      }
      const roots = definitions.rootElements as Record<string, unknown>[];
      messageRows.value = roots.filter((e) => is(e, "bpmn:Message")).map(toRow);
      signalRows.value = roots.filter((e) => is(e, "bpmn:Signal")).map(toRow);
    }

    function bindStack(): void {
      if (stackOff) {
        stackOff();
        stackOff = null;
      }
      const m = modelerGetter() as ModelerLike | null;
      if (!m) {
        return;
      }
      const eventBus = m.get("eventBus") as {
        on: (e: string, h: () => void) => void;
        off: (e: string, h: () => void) => void;
      };
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
      () => modelerGetter(),
      () => {
        bindStack();
        refresh();
      },
      { immediate: true }
    );

    function openMessageDialog(row?: MsgSignalRow): void {
      messageEditing.value = row ?? null;
      if (row) {
        messageForm.value = { id: row.id, name: row.name };
        messageDialogVisible.value = true;
        return;
      }
      messageForm.value = { id: generateBpmnLocalPart("Message_"), name: "" };
      messageDialogVisible.value = true;
    }

    function openSignalDialog(row?: MsgSignalRow): void {
      signalEditing.value = row ?? null;
      if (row) {
        signalForm.value = { id: row.id, name: row.name };
        signalDialogVisible.value = true;
        return;
      }
      signalForm.value = { id: generateBpmnLocalPart("Signal_"), name: "" };
      signalDialogVisible.value = true;
    }

    function saveMessage(): void {
      const m = modelerGetter() as ModelerLike | null;
      const definitions = getDefinitions(m as ModelerLike) as { rootElements?: Record<string, unknown>[] } | null;
      const processEl = findProcessElement(m as ModelerLike) as { id?: string } | null;
      if (!m || !definitions || !processEl) {
        return;
      }
      const id = messageForm.value.id?.trim();
      if (!id) {
        ElMessage.warning("消息 ID 不能为空");
        return;
      }
      const modeling = (m as ModelerLike).get("modeling") as {
        updateModdleProperties: (p: unknown, target: unknown, props: Record<string, unknown>) => void;
      };
      const moddle = (m as ModelerLike).get("moddle") as {
        create: (type: string, props: Record<string, unknown>) => Record<string, unknown>;
      };
      const editing = messageEditing.value;
      if (editing) {
        modeling.updateModdleProperties(processEl, editing.raw, {
          id,
          name: messageForm.value.name || undefined,
        });
        messageDialogVisible.value = false;
        refresh();
        return;
      }
      const msg = moddle.create("bpmn:Message", { id, name: messageForm.value.name || undefined });
      const roots = [...(definitions.rootElements || []), msg];
      modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
      messageDialogVisible.value = false;
      refresh();
    }

    function saveSignal(): void {
      const m = modelerGetter() as ModelerLike | null;
      const definitions = getDefinitions(m as ModelerLike) as { rootElements?: Record<string, unknown>[] } | null;
      const processEl = findProcessElement(m as ModelerLike) as { id?: string } | null;
      if (!m || !definitions || !processEl) {
        return;
      }
      const id = signalForm.value.id?.trim();
      if (!id) {
        ElMessage.warning("信号 ID 不能为空");
        return;
      }
      const modeling = (m as ModelerLike).get("modeling") as {
        updateModdleProperties: (p: unknown, target: unknown, props: Record<string, unknown>) => void;
      };
      const moddle = (m as ModelerLike).get("moddle") as {
        create: (type: string, props: Record<string, unknown>) => Record<string, unknown>;
      };
      const editing = signalEditing.value;
      if (editing) {
        modeling.updateModdleProperties(processEl, editing.raw, {
          id,
          name: signalForm.value.name || undefined,
        });
        signalDialogVisible.value = false;
        refresh();
        return;
      }
      const sig = moddle.create("bpmn:Signal", { id, name: signalForm.value.name || undefined });
      const roots = [...(definitions.rootElements || []), sig];
      modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
      signalDialogVisible.value = false;
      refresh();
    }

    function removeMessage(row: MsgSignalRow): void {
      const m = modelerGetter() as ModelerLike | null;
      const definitions = getDefinitions(m as ModelerLike) as { rootElements?: Record<string, unknown>[] } | null;
      const processEl = findProcessElement(m as ModelerLike) as { id?: string } | null;
      if (!m || !definitions || !processEl) {
        return;
      }
      const roots = (definitions.rootElements || []).filter((e) => e !== row.raw);
      const modeling = (m as ModelerLike).get("modeling") as {
        updateModdleProperties: (p: unknown, target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
      refresh();
    }

    function removeSignal(row: MsgSignalRow): void {
      const m = modelerGetter() as ModelerLike | null;
      const definitions = getDefinitions(m as ModelerLike) as { rootElements?: Record<string, unknown>[] } | null;
      const processEl = findProcessElement(m as ModelerLike) as { id?: string } | null;
      if (!m || !definitions || !processEl) {
        return;
      }
      const roots = (definitions.rootElements || []).filter((e) => e !== row.raw);
      const modeling = (m as ModelerLike).get("modeling") as {
        updateModdleProperties: (p: unknown, target: unknown, props: Record<string, unknown>) => void;
      };
      modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
      refresh();
    }

    return {
      messageRows,
      signalRows,
      messageDialogVisible,
      signalDialogVisible,
      messageEditing,
      signalEditing,
      messageForm,
      signalForm,
      openMessageDialog,
      openSignalDialog,
      saveMessage,
      saveSignal,
      removeMessage,
      removeSignal,
      dispose,
    };
  },
};
