<template>
  <el-dialog v-model="modalVisible" title="文件上传队列" width="600px">
    <div class="upload-content">
      <div class="queue-header">
        <span class="queue-info">队列中共 {{ uploadQueue.length }} 个文件</span>
        <el-button v-if="uploading" type="danger" size="small" @click="onCancelAll">取消全部</el-button>
      </div>

      <div class="queue-list">
        <div
          v-for="(item, index) in uploadQueue"
          :key="index"
          class="queue-item"
          :class="{ active: index === currentIndex, failed: item.status === 'failed' }"
        >
          <div class="item-info">
            <span class="item-name">{{ item.file.name }}</span>
            <span class="item-size">{{ formatFileSize(item.file.size) }}</span>
          </div>
          <div class="item-progress">
            <el-progress v-if="item.status === 'uploading'" :percentage="item.progress" :stroke-width="6" />
            <el-progress v-if="item.status === 'failed'" :percentage="item.progress" status="exception" :stroke-width="6" />
            <span v-if="item.status === 'pending'" class="pending-text">等待中...</span>
          </div>
          <div class="item-status">
            <span class="status-text">{{ item.statusText }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import AttachApi, { type PreCheckAttachDto, type ApplyChunkVo } from "@/views/core/api/AttachApi.ts";
import { Result } from "@/commons/model/Result.ts";
import Sha256Worker from "@/views/drive/workers/Sha256C.worker.ts?worker";
import type { AddEntryDto } from "@/views/drive/api/DriveTypes.ts";
import DriveApi from "@/views/drive/api/DriveApi.ts";
import { DriveStore } from "@/views/drive/service/DriveStore";

export interface UploadQueueItem {
  file: File;
  parentId: string | null;
  status: "pending" | "uploading" | "completed" | "failed";
  progress: number;
  statusText: string;
}

const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB

const props = defineProps<{
  kind: string;
  defaultParentId?: string | null;
}>();

const emit = defineEmits<{
  (e: "on-queue-update", queue: UploadQueueItem[]): void;
  (e: "on-upload-success"): void;
}>();

const modalVisible = ref(false);
const uploadQueue = ref<UploadQueueItem[]>([]);
const uploading = ref(false);
const currentIndex = ref(-1);
const isCancelled = ref(false);
const hashWorker = ref<Worker | null>(null);

const toUploadQueue = (files: File | File[], parentId?: string | null): void => {
  const targetParentId = parentId !== undefined ? parentId : props.defaultParentId || null;

  const fileArray = Array.isArray(files) ? files : [files];
  if (fileArray.length === 0) {
    return;
  }

  const items: UploadQueueItem[] = fileArray.map((file) => ({
    file: file,
    parentId: targetParentId,
    status: "pending" as const,
    progress: 0,
    statusText: "等待中...",
  }));

  uploadQueue.value.push(...items);
  emit("on-queue-update", uploadQueue.value);

  if (!uploading.value) {
    processQueue();
  }
};

const onCancelAll = (): void => {
  isCancelled.value = true;
  uploading.value = false;
  uploadQueue.value.forEach((item) => {
    if (item.status === "uploading" || item.status === "pending") {
      item.status = "failed";
      item.statusText = "已取消";
    }
  });
  emit("on-queue-update", uploadQueue.value);
  ElMessage.info("已取消全部上传");
};

const processQueue = async (): Promise<void> => {
  if (uploading.value) {
    return;
  }

  const pendingIndex = uploadQueue.value.findIndex((item) => item.status === "pending");
  if (pendingIndex === -1) {
    uploading.value = false;
    currentIndex.value = -1;
    emit("on-upload-success");
    return;
  }

  uploading.value = true;
  currentIndex.value = pendingIndex;
  const item = uploadQueue.value[pendingIndex];

  await uploadFile(item);

  uploading.value = false;
  if (!isCancelled.value) {
    processQueue();
  }
};

const uploadFile = async (item: UploadQueueItem): Promise<void> => {
  item.status = "uploading";
  item.progress = 0;
  item.statusText = "正在计算哈希...";
  emit("on-queue-update", uploadQueue.value);

  try {
    const sha256 = await computeSHA256(item.file, (progress) => {
      item.statusText = `正在计算哈希... ${progress}%`;
    });
    item.statusText = "正在预检...";
    emit("on-queue-update", uploadQueue.value);

    const preCheckDto: PreCheckAttachDto = {
      name: item.file.name,
      kind: props.kind,
      totalSize: item.file.size.toString(),
      sha256: sha256,
    };

    const preCheckResult = await AttachApi.preCheckAttach(preCheckDto);
    if (Result.isError(preCheckResult)) {
      throw new Error(preCheckResult.message);
    }

    const { preCheckId, status, missingChunkIds } = preCheckResult.data;

    if (status === 3) {
      item.progress = 100;
      await createEntry(item.file, preCheckId, item.parentId);
      removeFromQueue(item);
      return;
    }

    const chunksToUpload = missingChunkIds.map((id: string) => parseInt(id));
    const totalChunks = chunksToUpload.length;

    if (totalChunks === 0) {
      item.progress = 100;
      await createEntry(item.file, preCheckId, item.parentId);
      removeFromQueue(item);
      return;
    }

    let uploadedCount = 0;
    let finalAttachId = preCheckId;

    for (const chunkId of chunksToUpload) {
      if (isCancelled.value) {
        return;
      }

      const start = chunkId * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, item.file.size);
      const chunk = item.file.slice(start, end);

      item.statusText = `正在上传区块 ${chunkId + 1}/${totalChunks}`;
      emit("on-queue-update", uploadQueue.value);
      const result = await uploadChunk(preCheckId, chunkId, chunk);
      if (!result) {
        throw new Error("上传区块失败");
      }

      if (result.attachId) {
        finalAttachId = result.attachId;
      }

      uploadedCount++;
      item.progress = Math.round((uploadedCount / totalChunks) * 100);
      emit("on-queue-update", uploadQueue.value);

      if (result.attachId && result.chunkApplied === result.chunkTotal) {
        await createEntry(item.file, result.attachId, item.parentId);
        removeFromQueue(item);
        return;
      }
    }

    await createEntry(item.file, finalAttachId, item.parentId);
    removeFromQueue(item);
  } catch (error: any) {
    if (isCancelled.value) {
      item.status = "failed";
      item.statusText = "已取消";
      emit("on-queue-update", uploadQueue.value);
      return;
    }

    item.status = "failed";
    item.statusText = error.message || "上传失败";
    emit("on-queue-update", uploadQueue.value);
    ElMessage.error(`${item.file.name} 上传失败: ${error.message}`);
  }
};

const removeFromQueue = (item: UploadQueueItem): void => {
  const index = uploadQueue.value.indexOf(item);
  if (index === -1) {
    return;
  }
  uploadQueue.value.splice(index, 1);
  emit("on-queue-update", uploadQueue.value);
};

const getHashWorker = (): Worker => {
  if (!hashWorker.value) {
    hashWorker.value = new Sha256Worker();
  }
  return hashWorker.value;
};

const computeSHA256 = async (file: File, onProgress?: (progress: number) => void): Promise<string> => {
  const worker = getHashWorker();

  return new Promise((resolve, reject) => {
    const messageHandler = (e: MessageEvent): void => {
      const { type, progress, hash, error } = e.data;

      if (type === "progress" && progress !== undefined) {
        if (onProgress) {
          onProgress(progress);
        }
        return;
      }

      if (type === "completed" && hash) {
        worker.removeEventListener("message", messageHandler);
        resolve(hash);
        return;
      }

      if (type === "error") {
        worker.removeEventListener("message", messageHandler);
        reject(new Error(error || "计算哈希失败"));
        return;
      }
    };

    worker.addEventListener("message", messageHandler);
    worker.postMessage({
      type: "start",
      file: file,
    });
  });
};

const uploadChunk = async (preCheckId: string, chunkId: number, chunk: Blob): Promise<ApplyChunkVo | null> => {
  if (isCancelled.value) {
    throw new Error("上传已取消");
  }

  const file = new File([chunk], `chunk_${chunkId}`, { type: "application/octet-stream" });
  const ret = await AttachApi.applyChunk(preCheckId, chunkId.toString(), file);
  if (Result.isSuccess(ret)) {
    return ret.data;
  }

  return null;
};

const createEntry = async (file: File, attachId: string, parentId: string | null): Promise<void> => {
  const addEntryDto: AddEntryDto = {
    driveSpaceId: DriveStore().getCurrentDriveSpace.id,
    parentId: parentId,
    name: file.name,
    kind: 0,
    attachId: attachId,
  };
  const result = await DriveApi.addEntry(addEntryDto);
  if (result.code !== 0) {
    throw new Error(result.message || "创建条目失败");
  }
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) {
    return "0 B";
  }

  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
};

onUnmounted(() => {
  if (hashWorker.value) {
    hashWorker.value.terminate();
    hashWorker.value = null;
  }
});

defineExpose({
  toUploadQueue,
  openModal: (): void => {
    modalVisible.value = true;
  },
});
</script>

<style scoped>
.upload-content {
  padding: 10px 0;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.queue-info {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.queue-list {
  max-height: 400px;
  overflow-y: auto;
}

.queue-item {
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  background: #f5f7fa;
  transition: all 0.3s;
}

.queue-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.queue-item.failed {
  background: #fef0f0;
  border-color: #fbc4c4;
}

.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 12px;
}

.item-size {
  font-size: 12px;
  color: #909399;
}

.item-progress {
  margin-bottom: 4px;
}

.pending-text {
  font-size: 12px;
  color: #909399;
}

.item-status {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.status-text {
  font-size: 12px;
  color: #606266;
}
</style>
