<template>
  <div class="extension-properties">
    <div class="section-head">
      <span>自定义属性（flowable:property）</span>
      <el-button type="primary" link size="small" @click="openDialog()">+ 添加属性</el-button>
    </div>
    <el-table :data="rows" size="small" border stripe empty-text="暂无数据">
      <el-table-column type="index" label="序号" width="56" />
      <el-table-column prop="name" label="名称" min-width="100" show-overflow-tooltip />
      <el-table-column prop="value" label="值" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑扩展属性' : '新建扩展属性'" width="420px" destroy-on-close>
      <el-form label-width="72px" size="small">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="form.value" type="textarea" :rows="2" />
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
import { onBeforeUnmount, ref, shallowRef, watch } from "vue";
import { ElMessage } from "element-plus";

type BpmnEl = { businessObject?: Record<string, unknown> };

const props = defineProps<{
  modeler: unknown;
  element: unknown;
}>();

type Row = { name: string; value: string; raw: Record<string, unknown> };

const rows = shallowRef<Row[]>([]);
const dialogVisible = ref(false);
const editing = ref<Row | null>(null);
const form = ref({ name: "", value: "" });

let stackOff: (() => void) | null = null;

function getM(): any {
  return props.modeler;
}

function isFlowableProperties(v: Record<string, unknown>): boolean {
  return v?.$type === "flowable:Properties";
}

function findPropertiesHolder(extValues: Record<string, unknown>[]): Record<string, unknown> | null {
  const found = extValues.find((v) => isFlowableProperties(v));
  if (found) {
    return found;
  }
  return null;
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
  return ext;
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
  const processEl = props.element as BpmnEl | null;
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

function removeRow(row: Row): void {
  const m = getM();
  const processEl = props.element as BpmnEl | null;
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
</script>

<style scoped>
.extension-properties {
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
