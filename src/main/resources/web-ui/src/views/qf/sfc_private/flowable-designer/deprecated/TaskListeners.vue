<template>
  <div class="task-listeners">
    <div class="section-head">
      <span>{{ heading }}</span>
      <el-button type="primary" link size="small" @click="openDialog()">+ 添加监听器</el-button>
    </div>
    <el-table :data="rows" size="small" border stripe empty-text="暂无数据">
      <el-table-column type="index" label="序号" width="56" />
      <el-table-column prop="event" label="事件" width="100" />
      <el-table-column prop="implType" label="类型" width="100" />
      <el-table-column prop="implValue" label="实现" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑任务监听器' : '新建任务监听器'" width="480px" destroy-on-close>
      <el-form label-width="100px" size="small">
        <el-form-item label="事件类型">
          <el-select v-model="form.event" style="width: 100%">
            <el-option label="create" value="create" />
            <el-option label="assignment" value="assignment" />
            <el-option label="complete" value="complete" />
            <el-option label="delete" value="delete" />
            <el-option label="timeout" value="timeout" />
          </el-select>
        </el-form-item>
        <el-form-item label="监听器类型">
          <el-select v-model="form.implKind" style="width: 100%">
            <el-option label="Java 类" value="class" />
            <el-option label="表达式" value="expression" />
            <el-option label="委托表达式" value="delegateExpression" />
          </el-select>
        </el-form-item>
        <el-form-item :label="implLabel">
          <el-input v-model="form.implText" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, shallowRef, watch } from "vue";

type BpmnEl = { businessObject?: Record<string, unknown> };

const props = withDefaults(
  defineProps<{
    modeler: unknown;
    element: unknown;
    heading?: string;
  }>(),
  {
    heading: "任务监听器",
  }
);

type Row = {
  event: string;
  implType: string;
  implValue: string;
  raw: Record<string, unknown>;
};

const rows = shallowRef<Row[]>([]);

const dialogVisible = ref(false);
const editing = ref<Row | null>(null);
const form = ref({
  event: "create",
  implKind: "class" as "class" | "expression" | "delegateExpression",
  implText: "",
});

let stackOff: (() => void) | null = null;

const implLabel = computed(() => {
  if (form.value.implKind === "class") {
    return "Java 类";
  }
  if (form.value.implKind === "expression") {
    return "表达式";
  }
  return "委托表达式";
});

function getM(): any {
  return props.modeler;
}

function isTaskListener(v: Record<string, unknown>): boolean {
  return v?.$type === "flowable:TaskListener";
}

function readImpl(listener: Record<string, unknown>): { kind: Row["implType"]; text: string } {
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

function toRow(listener: Record<string, unknown>): Row {
  const impl = readImpl(listener);
  return {
    event: (listener.event as string) || "",
    implType: impl.kind,
    implValue: impl.text,
    raw: listener,
  };
}

function refresh(): void {
  const el = props.element as BpmnEl | null;
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

watch(
  () => [props.modeler, props.element],
  () => {
    bindStack();
    refresh();
  },
  { immediate: true, deep: true }
);

onBeforeUnmount(() => {
  if (!stackOff) {
    return;
  }
  stackOff();
  stackOff = null;
});

function openDialog(row?: Row): void {
  editing.value = row ?? null;
  if (row) {
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
    return;
  }
  form.value = { event: "create", implKind: "class", implText: "" };
  dialogVisible.value = true;
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

function ensureExtensionElements(m: any, diagramEl: BpmnEl, bo: Record<string, unknown>): { ext: Record<string, unknown> } {
  let ext = bo.extensionElements as Record<string, unknown> | undefined;
  if (ext) {
    return { ext };
  }
  const moddle = m.get("moddle");
  const modeling = m.get("modeling");
  ext = moddle.create("bpmn:ExtensionElements", { values: [] });
  modeling.updateModdleProperties(diagramEl, bo, { extensionElements: ext });
  return { ext };
}

function save(): void {
  const m = getM();
  const diagramEl = props.element as BpmnEl | null;
  if (!m || !diagramEl?.businessObject) {
    return;
  }
  const bo = diagramEl.businessObject;
  const modeling = m.get("modeling");
  const moddle = m.get("moddle");
  const { ext } = ensureExtensionElements(m, diagramEl, bo);
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

function removeRow(row: Row): void {
  const m = getM();
  const diagramEl = props.element as BpmnEl | null;
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
</script>

<style scoped>
.task-listeners {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-regular);
}
</style>
