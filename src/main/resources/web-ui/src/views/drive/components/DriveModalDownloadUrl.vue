<template>
  <el-dialog
    v-model="modalVisible"
    title="获取下载链接"
    width="520px"
    align-center
    destroy-on-close
    class="download-url-dialog"
  >
    <div class="modal-body">
      <el-alert title="下载链接已生成" type="success" :closable="false" show-icon class="mb-4">
        <template #default>
          <span class="text-gray-500 text-sm"> 您可以复制下方链接，使用下载工具（如 IDM、迅雷）进行下载。 </span>
        </template>
      </el-alert>

      <div class="url-container">
        <el-input
          id="copy-target"
          v-model="downloadUrl"
          type="textarea"
          :rows="3"
          readonly
          resize="none"
          class="url-input"
          @focus="onInputFocus"
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeModal">关闭</el-button>
        <el-button ref="copyBtnRef" type="primary" @click="copyUrl">
          <el-icon class="el-icon--left"><CopyDocument /></el-icon>
          一键复制
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { CopyDocument } from "@element-plus/icons-vue";

const modalVisible = ref(false);
const downloadUrl = ref("");

/**
 * 打开模态框
 * @param url 下载链接
 */
const openModal = (url: string): void => {
  downloadUrl.value = url;
  modalVisible.value = true;

  // 打开后自动聚焦并全选
  nextTick(() => {
    const input = document.querySelector("#copy-target textarea") as HTMLTextAreaElement;
    if (input) {
      input.focus();
      input.select();
    }
  });
};

/**
 * 关闭模态框
 */
const closeModal = (): void => {
  modalVisible.value = false;
};

/**
 * 复制链接到剪贴板
 */
const copyUrl = async (): Promise<void> => {
  if (!downloadUrl.value) {
    return;
  }

  try {
    await navigator.clipboard.writeText(downloadUrl.value);
    ElMessage.success("下载链接已复制到剪贴板");
    closeModal();
  } catch {
    // 降级处理：手动选择
    const input = document.querySelector("#copy-target textarea") as HTMLTextAreaElement;
    if (input) {
      input.select();
      document.execCommand("copy");
      ElMessage.success("下载链接已复制到剪贴板");
      closeModal();
    }
    if (!input) {
      ElMessage.error("复制失败，请手动复制");
    }
  }
};

/**
 * 输入框获得焦点时自动全选
 */
const onInputFocus = (e: FocusEvent): void => {
  const target = e.target as HTMLTextAreaElement;
  target.select();
};

defineExpose({
  openModal,
  closeModal,
});
</script>

<style scoped>
.modal-body {
  padding: 10px 0;
}

.url-container {
  margin-top: 15px;
}

.mb-4 {
  margin-bottom: 16px;
}

/* 优化输入框样式，使其看起来更像代码块 */
:deep(.url-input .el-textarea__inner) {
  font-family: Consolas, Monaco, "Andale Mono", "Ubuntu Mono", monospace;
  font-size: 13px;
  color: #409eff;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  padding: 12px;
  line-height: 1.6;
}

:deep(.url-input .el-textarea__inner:focus) {
  border-color: #409eff;
  background-color: #fff;
  box-shadow: 0 0 0 1px #409eff inset;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}
</style>
