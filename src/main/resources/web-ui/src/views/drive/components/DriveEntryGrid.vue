<template>
  <div ref="gridRef" v-loading="listLoading" class="list-grid" @contextmenu.prevent="onGridRightClick" @mousedown="onMouseDown">
    <!-- 上级目录 -->
    <DriveEntryItem
      v-if="hasParentDir"
      :type="1"
      :current-dir="currentDir"
      @on-click="onEntryClick"
      @on-dblclick="enterDirectory"
      @on-contextmenu="onContextmenu"
      @on-drag-drop="onDrop"
    />

    <!-- 条目列表 -->
    <DriveEntryItem
      v-for="item in listData"
      :key="item.id"
      :ref="(el) => setEntryRef(item.id, el)"
      :entry="item"
      :current-dir="currentDir"
      :class="{ selected: selectedIds.has(item.id) }"
      @on-click="onEntryClick"
      @on-dblclick="onEntryDoubleClick"
      @on-contextmenu="onContextmenu"
      @on-drag-start="onDragStart"
      @on-drag-over="onDragOver"
      @on-drag-drop="onDrop"
    />

    <el-empty v-if="listTotal === 0" description="此处没有任何物品" style="width: 100%; height: 100%; user-select: none" />

    <!-- 框选矩形 -->
    <div
      v-if="hasSelecting"
      class="selection-box"
      :style="{
        left: selectBox.left + 'px',
        top: selectBox.top + 'px',
        width: selectBox.width + 'px',
        height: selectBox.height + 'px',
      }"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import DriveEntryItem from "@/views/drive/components/DriveEntryItem.vue";
import DriveEntryGridService, { type EntryGridEmitter } from "@/views/drive/service/DriveEntryGridService.ts";
import type { CurrentDirPo, EntryPo } from "@/views/drive/api/DriveTypes.ts";

const props = defineProps<{
  //搜索关键词
  keyword: string | null;
}>();

const emit = defineEmits<EntryGridEmitter>();

const gridRef = ref<HTMLElement | null>(null);
const entryRefs = ref<Map<string, HTMLElement>>(new Map());

//条目列表打包
const { hasParentDir, currentDir, listQuery, listData, listTotal, listLoading, listLoad } =
  DriveEntryGridService.useEntryList(emit);

//鼠标框选和点选打包
const { selectEntry, hasSelecting, selectBox, selectedIds, onMouseDown } = DriveEntryGridService.useEntrySelection(
  gridRef,
  entryRefs
);

//鼠标拖拽打包
const { onDragStart, onDragOver, onDrop } = DriveEntryGridService.useEntryDrag(listData, selectedIds, emit);

//文件夹导航打包
const {
  redirectDirectory,
  enterDirectory,
  backspace: backParentDirectory,
} = DriveEntryGridService.useDirectoryNavigation(listQuery, selectedIds, listLoad);

const setEntryRef = (id: string, el: any): void => {
  if (!el) {
    entryRefs.value.delete(id);
    return;
  }
  if (el.$el) {
    entryRefs.value.set(id, el.$el);
  }
};

/**
 * 条目被单击
 * @param entry 条目对象
 */
const onEntryClick = (entry: EntryPo): void => {
  selectEntry(entry);
  emit("on-entry-click", entry);
};

/**
 * 条目被双击
 * @param entry 条目对象
 */
const onEntryDoubleClick = (entry: EntryPo): void => {
  if (entry.kind === 1) {
    enterDirectory(entry, currentDir.value);
    return;
  }
  emit("on-entry-dblclick", entry);
};

/**
 * 右键菜单事件
 * @param entry 条目对象
 * @param event 鼠标事件
 */
const onContextmenu = (entry: EntryPo, event: MouseEvent): void => {
  //如果选了多个且当前右键的也在其中，保持多选状态
  if (selectedIds.value.size > 1 && entry.id && selectedIds.value.has(entry.id)) {
    const selectedEntries = listData.value.filter((item) => selectedIds.value.has(item.id));
    emit("on-entry-contextmenu", selectedEntries, event);
    return;
  }

  //单选
  emit("on-entry-contextmenu", [entry], event);
};

/**
 * 右键点击容器中没有元素的空白区
 * @param event 鼠标事件
 */
const onGridRightClick = (event: MouseEvent): void => {
  emit("on-entry-contextmenu", [], event);
};

onMounted(() => {
  listLoad();
});

//监听keyword变更
watch(
  () => props.keyword,
  (newVal: string | null) => {
    listQuery.keyword = newVal;
    listLoad();
  }
);

defineExpose({
  /**
   * 加载条目列表
   */
  listLoad,

  /**
   * 获取当前目录ID
   * @returns 当前目录ID
   */
  getCurrentDirId: () => {
    return currentDir.value.id;
  },

  /**
   * 获取当前选中的条目
   * @returns 当前选中的条目列表
   */
  getSelectedEntries: () => {
    return listData.value.filter((item) => selectedIds.value.has(item.id));
  },

  /**
   * 全选
   */
  selectAll: () => {
    selectedIds.value.clear();
    listData.value.forEach((item) => {
      selectedIds.value.add(item.id);
    });
  },

  /**
   * 返回上级目录
   */
  backspace: () => {
    backParentDirectory();
  },

  /**
   * 导航进入文件夹
   * @param entry 条目对象
   * @param currentDir 当前目录对象
   */
  enterDirectory,

  /**
   * 重定向到指定目录
   * @param dirId 目录ID
   */
  redirectDirectory,
});
</script>
<style scoped>
.list-grid {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  padding: 10px 0;
  width: calc(100% - 2px);
  height: calc(100vh - 190px);
  /* border: 1px solid var(--el-border-color); */
  overflow-y: auto;
  outline: none;
}

.selection-box {
  position: absolute;
  border: 1px solid #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  pointer-events: none;
  z-index: 1000;
}

.drive-entry-item.selected {
  background-color: rgba(64, 158, 255, 0.2) !important;
}
</style>
