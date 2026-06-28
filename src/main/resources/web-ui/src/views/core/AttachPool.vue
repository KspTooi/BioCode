<template>
  <StdListContainer nopadding>
    <el-scrollbar v-loading="loading" class="pool-scroll">
      <div class="pool-content">
        <div class="page-toolbar">
          <span class="page-title">
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
            <div class="stat-item">
              <div class="stat-label">已索引附件</div>
              <div class="stat-value">{{ record.indexedCount ?? 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">游离附件</div>
              <div class="stat-value" :class="{ 'stat-warn': (record.driftCount ?? 0) > 0 }">
                {{ record.driftCount ?? 0 }}
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">磁盘已用</div>
              <div class="stat-value stat-sm">{{ formatBytes(record.poolUsageBytes) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">磁盘总容量</div>
              <div class="stat-value stat-sm">{{ formatBytes(record.poolCapacityBytes) }}</div>
            </div>
          </div>

          <div v-if="diskUsageOption" class="usage-block">
            <div class="usage-title">磁盘使用率</div>
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
              {{
                formatBytes(
                  String(Math.max(0, Number(record.poolUsageBytes) - Number(record.poolAttachesBytes)))
                )
              }}
            </el-descriptions-item>
            <el-descriptions-item label="存储池路径" :span="2">{{ record.poolPath }}</el-descriptions-item>
            <el-descriptions-item label="扫描开始时间">{{ record.scanStartTime ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="扫描结束时间">{{ record.scanEndTime ?? "-" }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-scrollbar>
  </StdListContainer>
</template>

<script setup lang="ts">
import { provide } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart } from "echarts/charts";
import { GridComponent, TooltipComponent, LegendComponent } from "echarts/components";
import VChart, { THEME_KEY } from "vue-echarts";
import { FolderOpened } from "@element-plus/icons-vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import AttachPoolService from "@/views/core/service/AttachPoolService.ts";

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent]);

provide(THEME_KEY, "light");

const { record, loading, scanning, loadRecord, onScan, formatBytes, diskUsageOption } =
  AttachPoolService.useAttachPoolStatus();
</script>

<style scoped>
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

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-title .el-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-item {
  padding: 12px 16px;
  background-color: #f8f9fb;
  border: 1px solid var(--el-border-color-lighter);
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.stat-value.stat-sm {
  font-size: 18px;
}

.stat-warn {
  color: var(--el-color-warning);
}

.usage-block {
  padding: 12px 16px;
  background-color: #f8f9fb;
  border: 1px solid var(--el-border-color-lighter);
}

.usage-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
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
    width: 120px;
  }
  .el-descriptions__content {
    color: var(--el-text-color-primary);
  }
  border-radius: 0 !important;
}
</style>
