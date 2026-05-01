<template>
  <div class="process-messages-signals">
    <div class="section-block">
      <div class="section-head">
        <span>消息列表</span>
        <el-button type="primary" link size="small" @click="openMessageDialog()">+ 创建新消息</el-button>
      </div>
      <el-table :data="messageRows" size="small" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="56" />
        <el-table-column prop="id" label="消息 ID" min-width="100" show-overflow-tooltip />
        <el-table-column prop="name" label="消息名称" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openMessageDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeMessage(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="section-block">
      <div class="section-head">
        <span>信号列表</span>
        <el-button type="primary" link size="small" @click="openSignalDialog()">+ 创建新信号</el-button>
      </div>
      <el-table :data="signalRows" size="small" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="56" />
        <el-table-column prop="id" label="信号 ID" min-width="100" show-overflow-tooltip />
        <el-table-column prop="name" label="信号名称" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openSignalDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeSignal(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="messageDialogVisible" :title="messageEditing ? '编辑消息' : '新建消息'" width="420px" destroy-on-close>
      <el-form label-width="88px" size="small">
        <el-form-item label="消息 ID">
          <el-input v-model="messageForm.id" />
        </el-form-item>
        <el-form-item label="消息名称">
          <el-input v-model="messageForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMessage">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="signalDialogVisible" :title="signalEditing ? '编辑信号' : '新建信号'" width="420px" destroy-on-close>
      <el-form label-width="88px" size="small">
        <el-form-item label="信号 ID">
          <el-input v-model="signalForm.id" />
        </el-form-item>
        <el-form-item label="信号名称">
          <el-input v-model="signalForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSignal">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, shallowRef, watch } from "vue";
import { is } from "bpmn-js/lib/util/ModelUtil";
import { ElMessage } from "element-plus";
import {
  findProcessElement,
  generateBpmnLocalPart,
  getDefinitions,
} from "@/views/qf/sfc_private/flowable-designer/FlowableModelUtils";

type Row = { id: string; name: string; raw: Record<string, unknown> };

const props = defineProps<{
  modeler: unknown;
}>();

const messageRows = shallowRef<Row[]>([]);
const signalRows = shallowRef<Row[]>([]);

const messageDialogVisible = ref(false);
const signalDialogVisible = ref(false);
const messageEditing = ref<Row | null>(null);
const signalEditing = ref<Row | null>(null);
const messageForm = ref({ id: "", name: "" });
const signalForm = ref({ id: "", name: "" });

let stackOff: (() => void) | null = null;

function getM(): any {
  return props.modeler;
}

function toRow(el: Record<string, unknown>): Row {
  return {
    id: (el.id as string) || "",
    name: (el.name as string) || "",
    raw: el,
  };
}

function refresh(): void {
  const m = getM();
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
  () => props.modeler,
  () => {
    bindStack();
    refresh();
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (!stackOff) {
    return;
  }
  stackOff();
  stackOff = null;
});

function openMessageDialog(row?: Row): void {
  messageEditing.value = row ?? null;
  if (row) {
    messageForm.value = { id: row.id, name: row.name };
    messageDialogVisible.value = true;
    return;
  }
  messageForm.value = { id: generateBpmnLocalPart("Message_"), name: "" };
  messageDialogVisible.value = true;
}

function openSignalDialog(row?: Row): void {
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
  const m = getM();
  const definitions = getDefinitions(m) as { rootElements?: Record<string, unknown>[] } | null;
  const processEl = findProcessElement(m) as { id?: string } | null;
  if (!m || !definitions || !processEl) {
    return;
  }
  const id = messageForm.value.id?.trim();
  if (!id) {
    ElMessage.warning("消息 ID 不能为空");
    return;
  }
  const modeling = m.get("modeling");
  const moddle = m.get("moddle");
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
  const m = getM();
  const definitions = getDefinitions(m) as { rootElements?: Record<string, unknown>[] } | null;
  const processEl = findProcessElement(m) as { id?: string } | null;
  if (!m || !definitions || !processEl) {
    return;
  }
  const id = signalForm.value.id?.trim();
  if (!id) {
    ElMessage.warning("信号 ID 不能为空");
    return;
  }
  const modeling = m.get("modeling");
  const moddle = m.get("moddle");
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

function removeMessage(row: Row): void {
  const m = getM();
  const definitions = getDefinitions(m) as { rootElements?: Record<string, unknown>[] } | null;
  const processEl = findProcessElement(m) as { id?: string } | null;
  if (!m || !definitions || !processEl) {
    return;
  }
  const roots = (definitions.rootElements || []).filter((e) => e !== row.raw);
  const modeling = m.get("modeling");
  modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
  refresh();
}

function removeSignal(row: Row): void {
  const m = getM();
  const definitions = getDefinitions(m) as { rootElements?: Record<string, unknown>[] } | null;
  const processEl = findProcessElement(m) as { id?: string } | null;
  if (!m || !definitions || !processEl) {
    return;
  }
  const roots = (definitions.rootElements || []).filter((e) => e !== row.raw);
  const modeling = m.get("modeling");
  modeling.updateModdleProperties(processEl, definitions, { rootElements: roots });
  refresh();
}
</script>

<style scoped>
.process-messages-signals {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.section-block {
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
