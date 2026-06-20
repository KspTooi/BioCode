<template>
  <div class="container">
    <!-- 请求详情模态框 -->
    <el-dialog
      v-model="dialogVisible"
      title="请求详情"
      width="1000px"
      :close-on-click-modal="true"
      class="request-preview-dialog"
      :modal="true"
      :append-to-body="true"
      :close-on-press-escape="true"
      align-center
    >
      <div v-if="dialogVisible" class="dialog-content">
        <el-tabs v-model="activeTab" class="preview-tabs">
          <!-- 负载标签页 -->
          <el-tab-pane label="负载" name="payload">
            <div class="payload-container">
              <div class="payload-section">
                <div class="section-header">
                  <h4>请求体</h4>
                  <el-tag v-if="formData.requestBodyType" size="small" type="info">
                    {{ formData.requestBodyType }}
                  </el-tag>
                </div>
                <div class="json-container">
                  <vue-json-pretty
                    :data="parseJsonSafe(formData.requestBody)"
                    :show-line="true"
                    :show-length="true"
                    :deep="99"
                    :show-double-quotes="true"
                    class="json-viewer"
                  />
                </div>
              </div>

              <el-divider />

              <div class="payload-section">
                <div class="section-header">
                  <h4>响应体</h4>
                  <div class="response-meta">
                    <el-tag v-if="formData.responseBodyType" size="small" type="info">
                      {{ formData.responseBodyType }}
                    </el-tag>
                    <el-tag v-if="formData.statusCode" size="small" :type="getStatusType(formData.statusCode)">
                      {{ formData.statusCode }}
                    </el-tag>
                  </div>
                </div>
                <div class="json-container">
                  <vue-json-pretty
                    :data="parseJsonSafe(formData.responseBody)"
                    :show-line="true"
                    :show-length="true"
                    :deep="99"
                    :show-double-quotes="true"
                    class="json-viewer"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 标头标签页 -->
          <el-tab-pane label="标头" name="headers">
            <div class="headers-container">
              <div class="header-section">
                <h4 class="header-title">请求头</h4>
                <el-table :data="formData.requestHeaders" border stripe size="small" class="headers-table">
                  <el-table-column prop="k" label="键" width="200" show-overflow-tooltip />
                  <el-table-column prop="v" label="值" show-overflow-tooltip />
                </el-table>
              </div>

              <el-divider />

              <div class="header-section">
                <h4 class="header-title">响应头</h4>
                <el-table :data="formData.responseHeaders" border stripe size="small" class="headers-table">
                  <el-table-column prop="k" label="键" width="200" show-overflow-tooltip />
                  <el-table-column prop="v" label="值" show-overflow-tooltip />
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <!-- 详情标签页 -->
          <el-tab-pane label="详情" name="meta">
            <div class="meta-container">
              <el-card class="meta-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>基本信息</span>
                    <el-tag :type="getMethodType(formData.method)" size="small">{{ formData.method }}</el-tag>
                  </div>
                </template>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="请求ID">
                    <el-text class="monospace-text">{{ formData.requestId || "-" }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item label="来源">
                    {{ formData.source || "-" }}
                  </el-descriptions-item>
                  <el-descriptions-item label="请求URL" :span="2">
                    <el-text class="url-text">{{ formData.url || "-" }}</el-text>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <el-card class="meta-card" shadow="hover">
                <template #header>
                  <span>请求信息</span>
                </template>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="请求体类型">
                    <el-tag v-if="formData.requestBodyType" size="small" type="info">
                      {{ formData.requestBodyType }}
                    </el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="请求体长度">
                    {{ formatBytes(formData.requestBodyLength) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="请求时间" :span="2">
                    {{ formData.requestTime || "-" }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <el-card class="meta-card" shadow="hover">
                <template #header>
                  <span>响应信息</span>
                </template>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="HTTP状态码">
                    <el-tag v-if="formData.statusCode" :type="getStatusType(formData.statusCode)" size="small">
                      {{ formData.statusCode }}
                    </el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="getStatusTagType(formData.status)" size="small">
                      {{ getStatusText(formData.status) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="响应体类型">
                    <el-tag v-if="formData.responseBodyType" size="small" type="info">
                      {{ formData.responseBodyType }}
                    </el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="响应体长度">
                    {{ formatBytes(formData.responseBodyLength) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="响应时间" :span="2">
                    {{ formData.responseTime || "-" }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="formData.redirectUrl" label="重定向URL" :span="2">
                    <el-text class="url-text">{{ formData.redirectUrl }}</el-text>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { HttpHeaderVo } from "@/views/relay/api/UserRequestLogApi";
import { ref } from "vue";
import VueJsonPretty from "vue-json-pretty";
import "vue-json-pretty/lib/styles.css";

const dialogVisible = ref(false);
const activeTab = ref<"payload" | "headers" | "meta">("payload");

export interface RequestPreviewVo {
  id: string | null;
  requestId: string | null;
  method: string | null;
  url: string | null;
  source: string | null;
  requestHeaders: HttpHeaderVo[] | null;
  requestBodyLength: number | null;
  requestBodyType: string | null;
  requestBody: string | null;
  responseHeaders: HttpHeaderVo[] | null;
  responseBodyLength: number | null;
  responseBodyType: string | null;
  responseBody: string | null;
  statusCode: number | null;
  redirectUrl: string | null;
  status: number | null;
  requestTime: string | null;
  responseTime: string | null;
}

const formData = ref<RequestPreviewVo>({
  id: null,
  requestId: null,
  method: null,
  url: null,
  source: null,
  requestHeaders: null,
  requestBodyLength: null,
  requestBodyType: null,
  requestBody: null,
  responseHeaders: null,
  responseBodyLength: null,
  responseBodyType: null,
  responseBody: null,
  statusCode: null,
  redirectUrl: null,
  status: null,
  requestTime: null,
  responseTime: null,
});

const parseJsonSafe = (json: string | null): any => {
  if (!json) {
    return null;
  }

  try {
    return JSON.parse(json);
  } catch {
    return json;
  }
};

const getStatusType = (statusCode: number | null): string => {
  if (!statusCode) {
    return "";
  }

  if (statusCode >= 200 && statusCode < 300) {
    return "success";
  }
  if (statusCode >= 300 && statusCode < 400) {
    return "warning";
  }
  if (statusCode >= 400 && statusCode < 500) {
    return "danger";
  }
  if (statusCode >= 500) {
    return "danger";
  }
  return "info";
};

const getMethodType = (method: string | null): string => {
  if (!method) {
    return "";
  }

  if (method.toUpperCase() === "GET") {
    return "success";
  }
  if (method.toUpperCase() === "POST") {
    return "primary";
  }
  if (method.toUpperCase() === "PUT") {
    return "warning";
  }
  if (method.toUpperCase() === "DELETE") {
    return "danger";
  }
  if (method.toUpperCase() === "PATCH") {
    return "info";
  }
  return "";
};

const getStatusText = (status: number | null): string => {
  if (status === 0) {
    return "正常";
  }
  if (status === 1) {
    return "HTTP失败";
  }
  if (status === 2) {
    return "业务失败";
  }
  if (status === 3) {
    return "连接超时";
  }
  return "未知";
};

const getStatusTagType = (status: number | null): string => {
  if (status === 0) {
    return "success";
  }
  if (status === 1) {
    return "danger";
  }
  if (status === 2) {
    return "warning";
  }
  return "info";
};

const formatBytes = (bytes: number | null): string => {
  if (!bytes) {
    return "-";
  }

  const sizes = ["B", "KB", "MB", "GB"];
  if (bytes === 0) {
    return "0 B";
  }

  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return Math.round((bytes / Math.pow(1024, i)) * 100) / 100 + " " + sizes[i];
};

const openPreview = (vo: RequestPreviewVo): void => {
  formData.value = vo;
  dialogVisible.value = true;
};

defineExpose({
  openPreview,
});
</script>

<style scoped>
.request-preview-dialog {
  --el-dialog-margin-top: 0;
}

.dialog-content {
  max-height: 75vh;
  overflow: hidden;
}

.preview-tabs {
  height: 100%;
}

.preview-tabs :deep(.el-tabs__content) {
  max-height: calc(75vh - 120px);
  overflow-y: auto;
  padding: 8px 0;
}

.payload-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.payload-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--el-border-color-light);
  flex-shrink: 0;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.response-meta {
  display: flex;
  gap: 4px;
}

.json-container {
  flex: 1;
  min-height: 0;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color);
  border-radius: 0;
  overflow: hidden;
}

.json-container :deep(.vjs-tree) {
  font-size: 13px;
  line-height: 1.4;
  height: 100%;
  overflow: auto;
}

.json-container :deep(.vue-json-pretty) {
  height: 100%;
  overflow: auto;
}

.json-viewer {
  height: 100%;
  overflow: auto;
}

.headers-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-section {
  flex: 1;
}

.header-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.headers-table {
  font-size: 13px;
}

.headers-table :deep(.el-table__cell) {
  padding: 4px 8px;
}

.meta-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-card {
  border: 1px solid var(--el-border-color-lighter);
}

.meta-card :deep(.el-card__header) {
  background: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 8px 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.meta-card :deep(.el-card__body) {
  padding: 12px;
}

.meta-card :deep(.el-descriptions__label) {
  font-weight: 600;
  color: var(--el-text-color-regular);
  width: 120px;
}

.meta-card :deep(.el-descriptions__content) {
  color: var(--el-text-color-primary);
}

.monospace-text {
  font-family: "Monaco", "Menlo", "Ubuntu Mono", monospace;
  font-size: 12px;
  background: var(--el-fill-color-light);
  padding: 1px 4px;
  border-radius: 0;
}

.url-text {
  word-break: break-all;
  font-size: 13px;
  color: var(--el-color-primary);
}

:deep(.el-dialog) {
  border-radius: 0;
  box-shadow:
    0 12px 32px 4px rgba(0, 0, 0, 0.04),
    0 8px 20px rgba(0, 0, 0, 0.08);
  max-height: 90vh;
  overflow: hidden;
  margin: 0 !important;
  transform: translate(-50%, -50%) !important;
}

:deep(.el-dialog__header) {
  padding: 12px 16px 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
}

:deep(.el-dialog__body) {
  padding: 0 16px 16px;
  max-height: calc(90vh - 100px);
  overflow: hidden;
}

:deep(.el-tabs__nav-wrap::after) {
  background: var(--el-border-color-lighter);
}

:deep(.el-tabs__active-bar) {
  background: var(--el-color-primary);
}

:deep(.el-tabs__item) {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

:deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
  font-weight: 600;
}

.payload-container :deep(.el-divider--horizontal) {
  margin: 4px 0;
  flex-shrink: 0;
}

:deep(.el-divider--horizontal) {
  margin: 8px 0;
}

:deep(.el-tag) {
  border-radius: 0;
}

:deep(.el-descriptions) {
  --el-descriptions-table-border: 1px solid var(--el-border-color-lighter);
}

/* 防止模态框打开时出现全局滚动条 */
:deep(.el-overlay) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.el-overlay-dialog) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
}

@media (max-width: 1200px) {
  .request-preview-dialog {
    --el-dialog-width: 90vw;
  }

  :deep(.el-dialog) {
    max-height: 95vh;
  }

  .dialog-content {
    max-height: 70vh;
  }

  .preview-tabs :deep(.el-tabs__content) {
    max-height: calc(70vh - 100px);
  }
}
</style>
