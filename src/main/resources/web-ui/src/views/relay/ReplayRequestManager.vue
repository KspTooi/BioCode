<template>
  <StdListLayout show-persist-tip>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="原始请求ID">
              <el-input
                ref="originRequestIdInput"
                v-model="listForm.originRequestId"
                placeholder="请输入原始请求ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1"></el-col>
          <el-col :span="5" :offset="1"></el-col>
          <el-col :span="3" :offset="3">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadOriginRequestList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" :disabled="executeLoading" @click="executeReplay">
        {{ executeLoading ? "正在处理" : "执行重放" }}
      </el-button>
    </template>

    <template #table>
      <div v-if="originRequestList.length === 0" class="empty-state">
        <el-empty description="请输入原始请求ID进行查询">
          <el-button type="primary" @click="originRequestIdInput?.focus()">开始查询</el-button>
        </el-empty>
      </div>

      <div v-else class="request-area">
        <div class="section-title origin-title">原始请求ID: {{ listForm.originRequestId }}</div>

        <el-table v-loading="listLoading" :data="originRequestList" border class="mb-4">
          <el-table-column prop="requestId" label="请求ID" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip content="点击复制" placement="top">
                <el-link type="primary" underline="never" @click="copyText(scope.row.requestId)">
                  {{ scope.row.requestId }}
                </el-link>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="method" label="方法" min-width="50" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: methodColor(scope.row.method) }">
                {{ scope.row.method.toUpperCase() }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="url" label="请求URL" min-width="180" show-overflow-tooltip />
          <el-table-column prop="source" label="来源" min-width="60" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" min-width="50" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: statusColor(scope.row.status) }">
                {{ statusText(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="statusCode" label="HTTP" min-width="35" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: httpCodeColor(scope.row.statusCode) }">
                {{ scope.row.statusCode }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="requestTime" label="请求时间" show-overflow-tooltip />
          <el-table-column label="操作" fixed="right" min-width="100">
            <template #default="scope">
              <el-button link type="primary" size="small" :icon="ViewIcon" @click="openOriginViewModal(scope.row)">
                预览请求
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="section-title replay-title">重放请求列表</div>

        <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
          <el-table-column prop="requestId" label="请求ID" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip content="点击复制" placement="top">
                <el-link type="primary" underline="never" @click="copyText(scope.row.requestId)">
                  {{ scope.row.requestId }}
                </el-link>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="method" label="方法" min-width="50" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: methodColor(scope.row.method) }">
                {{ scope.row.method.toUpperCase() }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="url" label="请求URL" min-width="180" show-overflow-tooltip />
          <el-table-column prop="source" label="来源" min-width="60" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" min-width="50" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: statusColor(scope.row.status) }">
                {{ statusText(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="statusCode" label="HTTP" min-width="35" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: httpCodeColor(scope.row.statusCode) }">
                {{ scope.row.statusCode }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="requestTime" label="请求时间" show-overflow-tooltip />
          <el-table-column label="操作" fixed="right" min-width="100">
            <template #default="scope">
              <el-button link type="primary" size="small" :icon="ViewIcon" @click="openViewModal(scope.row)">
                预览请求
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <template #pagination>
      <el-pagination
        v-if="originRequestList.length > 0"
        v-model:current-page="listForm.pageNum"
        v-model:page-size="listForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="listTotal"
        background
        @size-change="
          (val: number) => {
            listForm.pageSize = val;
            loadList();
          }
        "
        @current-change="
          (val: number) => {
            listForm.pageNum = val;
            loadList();
          }
        "
      />
    </template>

    <template #modal>
      <RequestPreviewModal ref="requestPreviewModalRef" />
    </template>
  </StdListLayout>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, markRaw } from "vue";
import type {
  GetReplayRequestListDto,
  GetReplayRequestListVo,
  GetOriginRequestVo,
} from "@/views/relay/api/ReplayRequestApi.ts";
import ReplayRequestApi from "@/views/relay/api/ReplayRequestApi.ts";
import { ElMessage } from "element-plus";
import { View } from "@element-plus/icons-vue";
import RequestApi from "@/views/relay/api/RequestApi.ts";
import RequestPreviewModal from "@/views/relay/components/RequestPreviewModal.vue";
import type { RequestPreviewVo } from "@/views/relay/components/RequestPreviewModal.vue";
import type { HttpHeaderVo } from "@/views/relay/api/UserRequestLogApi";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";

const ViewIcon = markRaw(View);

const listForm = reactive<GetReplayRequestListDto>({
  originRequestId: null,
  relayServerId: null,
  requestId: null,
  method: null,
  url: null,
  source: null,
  status: null,
  pageNum: 1,
  pageSize: 20,
});

const listData = ref<GetReplayRequestListVo[]>([]);
const listTotal = ref(0);
const listLoading = ref(false);

const originRequestList = ref<GetOriginRequestVo[]>([]);
const originRequestIdInput = ref();

const requestPreviewModalRef = ref<InstanceType<typeof RequestPreviewModal>>();
const executeLoading = ref(false);

const methodColor = (method: string): string => {
  if (method === "DELETE") {
    return "#E74C3C";
  }
  if (method === "GET") {
    return "#3498DB";
  }
  if (method === "POST") {
    return "#2ECC71";
  }
  if (method === "PUT") {
    return "#F1C40F";
  }
  return "#95A5A6";
};

const statusColor = (status: number): string => {
  if (status === 0) {
    return "#2ECC71";
  }
  if (status === 1) {
    return "#E74C3C";
  }
  if (status === 2) {
    return "#F1C40F";
  }
  return "#95A5A6";
};

const statusText = (status: number): string => {
  if (status === 0) {
    return "正常";
  }
  if (status === 1) {
    return "HTTP失败";
  }
  if (status === 2) {
    return "业务失败";
  }
  return "连接超时";
};

const httpCodeColor = (code: number): string => {
  if (code >= 200 && code < 300) {
    return "#2ECC71";
  }
  if (code >= 300 && code < 400) {
    return "#F1C40F";
  }
  if (code >= 400 && code < 500) {
    return "#E74C3C";
  }
  return "#95A5A6";
};

const loadList = async (): Promise<void> => {
  listLoading.value = true;
  try {
    const res = await ReplayRequestApi.getReplayRequestList(listForm);
    listData.value = res.data;
    listTotal.value = res.total;
  } catch (e) {
    ElMessage.error("无法获取回放请求");
    console.error("无法获取回放请求", e);
  } finally {
    listLoading.value = false;
  }
};

const loadOriginRequestList = async (): Promise<void> => {
  listLoading.value = true;
  try {
    localStorage.setItem("originRequestId", listForm.originRequestId || "");
    const res = await ReplayRequestApi.getOriginRequest({ requestId: listForm.originRequestId });
    originRequestList.value = [res];
    loadList();
  } catch (e) {
    ElMessage.error("无法获取原始请求");
    console.error("无法获取原始请求", e);
  } finally {
    listLoading.value = false;
  }
};

const resetList = (): void => {
  listForm.originRequestId = null;
  listForm.requestId = null;
  listForm.method = null;
  listForm.url = null;
  listForm.source = null;
  listForm.status = null;
  listForm.relayServerId = null;
  localStorage.removeItem("originRequestId");
  originRequestList.value = [];
  listData.value = [];
  listTotal.value = 0;
  listForm.pageNum = 1;
  listForm.pageSize = 20;
};

const executeReplay = async (): Promise<void> => {
  try {
    executeLoading.value = true;
    await ReplayRequestApi.replayRequest(listForm.originRequestId || "");
    ElMessage.success("执行重放成功");
    loadList();
    loadOriginRequestList();
  } catch (e: any) {
    ElMessage.error(e.message);
    console.error("执行重放失败", e);
  } finally {
    executeLoading.value = false;
  }
};

onMounted(() => {
  const originRequestId = localStorage.getItem("originRequestId");
  if (!originRequestId) {
    return;
  }
  listForm.originRequestId = originRequestId;
  loadOriginRequestList();
  loadList();
});

const parseHeadersFromString = (headers: string): HttpHeaderVo[] => {
  if (!headers) {
    return [];
  }
  try {
    const parsed = JSON.parse(headers);
    if (typeof parsed === "object" && parsed !== null) {
      if (Array.isArray(parsed)) {
        return parsed;
      }
      return Object.entries(parsed).map(([k, v]) => ({ k, v: String(v) }));
    }
  } catch (e: any) {
    console.warn("无法将标头解析为JSON，将返回空数组", headers, e);
  }
  return [];
};

const openOriginViewModal = async (row: GetOriginRequestVo): Promise<void> => {
  try {
    const res = await RequestApi.getRequestDetails(row.id.toString());
    const previewData: RequestPreviewVo = {
      id: res.id.toString(),
      requestId: res.requestId,
      method: res.method,
      url: res.url,
      source: res.source,
      requestHeaders: parseHeadersFromString(res.requestHeaders),
      requestBodyLength: res.requestBodyLength,
      requestBodyType: res.requestBodyType,
      requestBody: res.requestBody,
      responseHeaders: parseHeadersFromString(res.responseHeaders),
      responseBodyLength: res.responseBodyLength,
      responseBodyType: res.responseBodyType,
      responseBody: res.responseBody,
      statusCode: res.statusCode,
      redirectUrl: res.redirectUrl,
      status: res.status,
      requestTime: res.requestTime,
      responseTime: res.responseTime,
    };
    requestPreviewModalRef.value?.openPreview(previewData);
  } catch {
    ElMessage.error("获取原始请求详情失败");
  }
};

const openViewModal = async (row: GetReplayRequestListVo): Promise<void> => {
  try {
    const res = await ReplayRequestApi.getReplayRequestDetails(row.id.toString());
    const previewData: RequestPreviewVo = {
      id: res.id.toString(),
      requestId: res.requestId,
      method: res.method,
      url: res.url,
      source: res.source,
      requestHeaders: parseHeadersFromString(res.requestHeaders),
      requestBodyLength: res.requestBodyLength,
      requestBodyType: res.requestBodyType,
      requestBody: res.requestBody,
      responseHeaders: parseHeadersFromString(res.responseHeaders),
      responseBodyLength: res.responseBodyLength,
      responseBodyType: res.responseBodyType,
      responseBody: res.responseBody,
      statusCode: res.statusCode,
      redirectUrl: res.redirectUrl,
      status: res.status,
      requestTime: res.requestTime,
      responseTime: res.responseTime,
    };
    requestPreviewModalRef.value?.openPreview(previewData);
  } catch (error) {
    ElMessage.error("获取请求详情失败");
    console.error("获取请求详情失败", error);
  }
};

const copyText = async (text: string): Promise<void> => {
  if (!text) {
    ElMessage.warning("内容为空，无法复制");
    return;
  }
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      ElMessage.success("已复制到剪贴板");
      return;
    }
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    const success = document.execCommand("copy");
    document.body.removeChild(textarea);
    if (success) {
      ElMessage.success("已复制到剪贴板");
      return;
    }
    ElMessage.error("复制失败");
  } catch {
    ElMessage.error("复制失败");
  }
};
</script>

<style scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.request-area {
  width: 100%;
}

.section-title {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.origin-title {
  color: #006aac;
}

.replay-title {
  color: #009175;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
