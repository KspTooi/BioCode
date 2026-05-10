<template>
  <el-dialog v-model="modalVisible" title="选择公共组件" width="860px" :close-on-click-modal="false" @close="onCancel">
    <div class="comp-choose-container">
      <el-input
        v-model="searchKeyword"
        placeholder="通过 业务域、组件名 搜索"
        clearable
        style="margin-bottom: 15px"
        prefix-icon="Search"
      />

      <div class="comp-list">
        <el-table
          ref="tableRef"
          :data="filteredCompList"
          border
          highlight-current-row
          :row-key="(row: PublicCompEntry) => row.key"
          :row-style="{ cursor: 'pointer' }"
          height="400"
          @selection-change="onSelectionChange"
          @select-all="onSelectAll"
          @row-click="onRowClick"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column label="业务域" prop="biz" width="120" show-overflow-tooltip />
          <el-table-column label="组件名" prop="name" min-width="200" show-overflow-tooltip />
          <el-table-column label="路径" prop="path" min-width="320" show-overflow-tooltip />
        </el-table>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">关闭</el-button>
        <el-button type="primary" :disabled="!selectedRow" @click="onConfirm">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, ref, watch, useTemplateRef } from "vue";
import type { TableInstance } from "element-plus";
import ComPublicCompChooseModalService from "@/soa/com-series/service/ComPublicCompChooseModalService";
import type { PublicCompEntry } from "@/soa/com-series/service/ComPublicCompChooseModalService";

const selectedKey = defineModel<string | null>({ default: null });
const modalVisible = defineModel<boolean>("modalVisible", { default: false });
const searchKeyword = defineModel<string>("searchKeyword", { default: "" });

const emit = defineEmits<{
  (e: "on-submit", key: string): void;
  (e: "on-close"): void;
}>();

const tableRef = useTemplateRef<TableInstance>("tableRef");
const syncingSelection = ref(false);
const selectingAll = ref(false);

const {
  selectedRow,
  filteredCompList,
  restoreSelection,
  clearSelection,
  searchKeyword: serviceKw,
} = ComPublicCompChooseModalService.useComPublicCompChooseModal();

/** 同步 v-model:search-keyword ↔ service 内部 searchKeyword */
watch(
  searchKeyword,
  (val) => {
    if (val === serviceKw.value) {
      return;
    }
    serviceKw.value = val;
  },
  { immediate: true }
);

watch(serviceKw, (val) => {
  if (val === searchKeyword.value) {
    return;
  }
  searchKeyword.value = val;
});

/** 打开时用外部 selectedKey 回填选中行；无论是否命中都同步表格视觉状态 */
watch(modalVisible, (visible) => {
  if (!visible) {
    return;
  }
  nextTick(() => {
    restoreSelection(selectedKey.value);
    applySingleSelection(selectedRow.value);
  });
});

const applySingleSelection = (row: PublicCompEntry | null): void => {
  if (!tableRef.value) {
    selectedRow.value = row;
    return;
  }

  syncingSelection.value = true;
  tableRef.value.clearSelection();

  if (!row) {
    tableRef.value.setCurrentRow(null);
    selectedRow.value = null;
    syncingSelection.value = false;
    return;
  }

  tableRef.value.toggleRowSelection(row, true);
  tableRef.value.setCurrentRow(row);
  selectedRow.value = row;
  syncingSelection.value = false;
};

const onSelectionChange = (rows: PublicCompEntry[]): void => {
  if (syncingSelection.value) {
    return;
  }
  if (selectingAll.value) {
    return;
  }

  if (!rows || rows.length === 0) {
    applySingleSelection(null);
    return;
  }

  const dataList = filteredCompList.value;
  if (dataList.length > 0 && rows.length === dataList.length) {
    applySingleSelection(rows[0]);
    return;
  }

  applySingleSelection(rows[rows.length - 1]);
};

const onSelectAll = (rows: PublicCompEntry[]): void => {
  if (syncingSelection.value) {
    return;
  }

  selectingAll.value = true;

  if (!rows || rows.length === 0) {
    applySingleSelection(null);
    nextTick(() => {
      selectingAll.value = false;
    });
    return;
  }

  applySingleSelection(rows[0]);
  nextTick(() => {
    selectingAll.value = false;
  });
};

const onRowClick = (row: PublicCompEntry): void => {
  if (syncingSelection.value) {
    return;
  }
  if (!row) {
    return;
  }
  if (selectedRow.value?.key === row.key) {
    applySingleSelection(null);
    return;
  }
  applySingleSelection(row);
};

const onConfirm = (): void => {
  if (!selectedRow.value) {
    return;
  }
  selectedKey.value = selectedRow.value.key;
  modalVisible.value = false;
  emit("on-submit", selectedRow.value.key);
};

const onCancel = (): void => {
  clearSelection();
  modalVisible.value = false;
  emit("on-close");
};
</script>
