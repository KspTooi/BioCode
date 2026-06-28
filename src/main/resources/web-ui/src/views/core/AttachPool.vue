<template>
  <StdListContainer nopadding>
    <el-tabs v-model="activeTab" class="pool-tabs">
      <el-tab-pane label="概览" name="overview">
        <el-scrollbar v-loading="loading" class="pool-scroll">
          <div class="pool-content">
            <div class="page-toolbar">
              <span class="title-with-icon page-title">
                <el-icon><FolderOpened /></el-icon>
                QSP 文件池
              </span>
              <div class="toolbar-actions">
                <el-button size="small" :loading="loading" @click="loadRecord">刷新</el-button>
                <el-button size="small" type="primary" :loading="scanning" @click="onScan">扫描附件池</el-button>
              </div>
            </div>

            <el-empty v-if="!record && !loading" description="暂无扫描记录，请点击「扫描附件池」开始首次扫描" />

            <template v-if="record">
              <div class="stat-grid">
                <div class="stat-item stat-item-clickable" @click="openStatExplain('indexed')">
                  <div class="title-with-icon stat-label">
                    <el-icon><CircleCheck /></el-icon>
                    已索引附件
                  </div>
                  <div class="stat-value">{{ record.indexedCount ?? 0 }}</div>
                </div>
                <div class="stat-item stat-item-clickable" @click="openStatExplain('drift')">
                  <div class="title-with-icon stat-label">
                    <el-icon><Warning /></el-icon>
                    游离附件
                  </div>
                  <div class="stat-value" :class="{ 'stat-warn': (record.driftCount ?? 0) > 0 }">
                    {{ record.driftCount ?? 0 }}
                  </div>
                </div>
                <div class="stat-item">
                  <div class="title-with-icon stat-label">
                    <el-icon><Coin /></el-icon>
                    附件池总容量
                  </div>
                  <div class="stat-value stat-sm">{{ formatBytes(record.poolCapacityBytes) }}</div>
                </div>
                <div class="stat-item">
                  <div class="title-with-icon stat-label">
                    <el-icon><PieChart /></el-icon>
                    附件池已用
                  </div>
                  <div class="stat-value stat-sm">{{ formatBytes(record.poolUsageBytes) }}</div>
                </div>
              </div>

              <div v-if="diskUsageOption" class="usage-block">
                <div class="title-with-icon usage-title">
                  <el-icon><DataLine /></el-icon>
                  附件池使用率
                </div>
                <v-chart class="usage-chart" :option="diskUsageOption" autoresize />
              </div>

              <el-descriptions :column="2" border size="small" class="custom-descriptions">
                <el-descriptions-item label="扫描状态">
                  <el-tag size="small" :type="record.scanStatus === 1 ? 'success' : 'warning'" effect="plain">
                    {{ record.scanStatus === 1 ? "扫描成功" : "正在扫描" }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="附件占用">{{ formatBytes(record.poolAttachesBytes) }}</el-descriptions-item>
                <el-descriptions-item label="其他占用">
                  {{ formatBytes(String(Math.max(0, Number(record.poolUsageBytes) - Number(record.poolAttachesBytes)))) }}
                </el-descriptions-item>
                <el-descriptions-item label="存储池路径" :span="2">{{ record.poolPath }}</el-descriptions-item>
                <el-descriptions-item label="扫描开始时间">{{ record.scanStartTime ?? "-" }}</el-descriptions-item>
                <el-descriptions-item label="扫描结束时间">{{ record.scanEndTime ?? "-" }}</el-descriptions-item>
              </el-descriptions>
            </template>
          </div>
        </el-scrollbar>
      </el-tab-pane>

      <el-tab-pane label="附件池诊断" name="details" lazy>
        <AttachPoolDetails />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="statExplainVisible" :title="statExplainTitle" width="520px" @close="closeStatExplain">
      <p class="stat-explain-text">{{ statExplainText }}</p>
      <template #footer>
        <el-button type="primary" @click="closeStatExplain">知道了</el-button>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { provide } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart } from "echarts/charts";
import { GridComponent, TooltipComponent, LegendComponent } from "echarts/components";
import VChart, { THEME_KEY } from "vue-echarts";
import { CircleCheck, Coin, DataLine, FolderOpened, PieChart, Warning } from "@element-plus/icons-vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import AttachPoolDetails from "@/views/core/components/AttachPoolDetails.vue";
import AttachPoolService from "@/views/core/service/AttachPoolService.ts";

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent]);

provide(THEME_KEY, "light");

const {
  activeTab,
  record,
  loading,
  scanning,
  statExplainVisible,
  statExplainTitle,
  statExplainText,
  loadRecord,
  onScan,
  openStatExplain,
  closeStatExplain,
  formatBytes,
  diskUsageOption,
} = AttachPoolService.useAttachPoolStatus();
</script>

<style scoped>
.pool-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__header) {
  margin: 0;
  background-color: #fff;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

:deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
}

:deep(.el-tab-pane) {
  height: 100%;
}

.pool-scroll {
  height: 100%;
  background-color: #fff;
}

.pool-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

:deep(.el-scrollbar__view) {
  min-height: 100%;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.title-with-icon .el-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}

.page-title {
  height: 24px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  padding: 12px 16px;
  background-color: #f8f9fb;
  border: 1px solid var(--el-border-color-lighter);
}

.stat-item-clickable {
  cursor: pointer;
  transition:
    border-color 0.2s,
    background-color 0.2s;
}

.stat-item-clickable:hover {
  border-color: var(--el-color-primary-light-5);
  background-color: #f0f5ff;
}

.stat-label {
  font-size: 12px;
  font-weight: normal;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-label .el-icon {
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.stat-value.stat-sm {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.stat-warn {
  color: var(--el-color-danger);
}

.usage-block {
  padding: 12px 16px;
  background-color: #f8f9fb;
  border: 1px solid var(--el-border-color-lighter);
}

.usage-title {
  margin-bottom: 8px;
}

.usage-chart {
  height: 72px;
  width: 100%;
}

:deep(.custom-descriptions) {
  .el-descriptions__label {
    background-color: #f8f9fb !important;
    font-weight: 500;
    color: var(--el-text-color-regular);
    width: 120px;
  }
  .el-descriptions__content {
    color: var(--el-text-color-primary);
  }
  border-radius: 0 !important;
}

.stat-explain-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

:deep(.el-tabs--top .el-tabs__item.is-top:nth-child(2)) {
  padding-left: 20px;
}

:deep(.el-tabs__active-bar) {
  border-radius: 0;
}
</style>
