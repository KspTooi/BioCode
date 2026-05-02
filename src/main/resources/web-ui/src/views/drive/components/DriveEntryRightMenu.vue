<template>
  <div
    v-show="visible"
    ref="rightMenuRef"
    class="context-menu"
    :style="{ left: menuX + 'px', top: menuY + 'px' }"
    @click.stop
    @contextmenu.prevent
  >
    <!-- 当没有选中条目时，只显示新建文件夹和上传文件 -->
    <template v-if="!currentEntry">
      <div class="menu-item" @click="onRefresh">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </div>
      <div class="menu-item" :class="{ disabled: !hasClipboard }" @click="onPaste">
        <el-icon><DocumentCopy /></el-icon>
        <span>粘贴</span>
      </div>
      <div class="menu-item" @click="onCreateFolder">
        <el-icon><FolderAdd /></el-icon>
        <span>新建文件夹</span>
      </div>
      <div class="menu-item" @click="onUploadFile">
        <el-icon><Upload /></el-icon>
        <span>上传文件</span>
      </div>
    </template>

    <!-- 当单选条目时，显示完整菜单 -->
    <template v-if="currentEntry && !isMultiSelect">
      <div v-if="!isMultiSelect && currentEntry.kind === 0" class="menu-item" @click="onPreview">
        <el-icon><View /></el-icon>
        <span>预览</span>
      </div>
      <div v-if="currentEntry.kind === 0" class="menu-item" @click="onDownload">
        <el-icon><Download /></el-icon>
        <span>下载</span>
      </div>
      <div v-if="currentEntry.kind === 0" class="menu-item" @click="onDownloadUrl">
        <el-icon><Download /></el-icon>
        <span>创建下载链接</span>
      </div>
      <div v-if="currentEntry.kind === 0" class="menu-divider"></div>
      <div class="menu-item" @click="onCut">
        <el-icon><Scissor /></el-icon>
        <span>剪切</span>
      </div>
      <div class="menu-item" @click="onCopy">
        <el-icon><DocumentCopy /></el-icon>
        <span>复制</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="onDelete">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
      <div class="menu-item" @click="onRename">
        <el-icon><Edit /></el-icon>
        <span>重命名</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="onProperties">
        <el-icon><InfoFilled /></el-icon>
        <span>属性</span>
      </div>
    </template>

    <!-- 当多选条目时，显示完整菜单 -->
    <template v-if="isMultiSelect">
      <div v-if="currentEntries.every((item) => item.kind === 0)" class="menu-item" @click="onDownload">
        <el-icon><Download /></el-icon>
        <span>打包下载({{ currentEntries.length }})</span>
      </div>
      <div v-if="currentEntries.every((item) => item.kind === 0)" class="menu-item" @click="onDownloadUrl">
        <el-icon><Download /></el-icon>
        <span>创建下载链接({{ currentEntries.length }})</span>
      </div>
      <div class="menu-item" @click="onCopy">
        <el-icon><DocumentCopy /></el-icon>
        <span>复制</span>
      </div>
      <div class="menu-item" @click="onDelete">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import {
  FolderAdd,
  View,
  Download,
  Scissor,
  DocumentCopy,
  Delete,
  Edit,
  InfoFilled,
  Upload,
  Refresh,
} from "@element-plus/icons-vue";
import DriveEntryRightMenuService, { type RightMenuEmitter } from "@/views/drive/service/DriveEntryRightMenuService";
import type { EntryPo } from "@/views/drive/api/DriveTypes.ts";

const rightMenuRef = ref<HTMLElement>();

const emit = defineEmits<RightMenuEmitter>();

//右键菜单控制打包
const {
  openRightMenu,
  closeRightMenu,
  isMultiSelect,
  currentEntries,
  visible,
  currentEntry,
  hasClipboard,
  x: menuX,
  y: menuY,
} = DriveEntryRightMenuService.useRightMenuControl(rightMenuRef);

//右键菜单事件提交打包
const {
  onRefresh,
  onPaste,
  onCreateFolder,
  onUploadFile,
  onPreview,
  onDownload,
  onDownloadUrl,
  onCut,
  onCopy,
  onDelete,
  onRename,
  onProperties,
} = DriveEntryRightMenuService.useRightMenuEmitter(emit, closeRightMenu, currentEntry, currentEntries);

defineExpose({
  openRightMenu,
  closeRightMenu,
});
</script>

<style scoped>
.context-menu {
  position: fixed;
  background: #ffffff;
  border: 1px solid #d4d4d4;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  min-width: 160px;
  padding: 2px 0;
  font-size: 13px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 20px 6px 28px;
  cursor: pointer;
  color: #333333;
  transition: background-color 0.1s;
  user-select: none;
  white-space: nowrap;
}

.menu-item:hover {
  background: #e8f4ff;
  color: #000000;
}

.menu-item .el-icon {
  font-size: 14px;
  width: 16px;
  text-align: center;
}

.menu-divider {
  height: 1px;
  background: #e5e5e5;
  margin: 2px 0;
}

.menu-item.disabled {
  color: #a8a8a8;
  cursor: not-allowed;
  pointer-events: none;
}

.menu-item.disabled:hover {
  background: transparent;
  color: #a8a8a8;
}
</style>
