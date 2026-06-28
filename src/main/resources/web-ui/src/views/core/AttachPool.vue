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
                <el-button size="small" type="primary" :loading="scanning" :disabled="rebuildRunning || clearingInvalid" @click="onQuickScan">更新统计数据</el-button>
                <el-button size="small" type="danger" plain :loading="scanning" :disabled="rebuildRunning || clearingInvalid" @click="onDeepScan">检查索引完整性</el-button>
              </div>
            </div>

            <el-empty v-if="!record && !loading" description="暂无统计数据，请点击「更新统计数据」开始首次统计" />

            <template v-if="record">
              <div class="stat-grid">
                <div class="stat-item stat-item-clickable" @click="openStatExplain('indexed')">
                  <div class="title-with-icon stat-label">
                    <el-icon><CircleCheck /></el-icon>
                    已索引附件
                  </div>
                  <div class="stat-value">{{ record.indexedCount ?? 0 }}</div>
                </div>
                <div class="stat-item stat-item-clickable" @click="openStatExplain('indexedLost')">
                  <div class="title-with-icon stat-label">
                    <el-icon><CircleClose /></el-icon>
                    失效索引
                  </div>
                  <div class="stat-value" :class="{ 'stat-warn': (record.indexedLostCount ?? 0) > 0 }">
                    {{ record.indexedLostCount ?? 0 }}
                  </div>
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

            <div class="rebuild-block">
              <div class="rebuild-header">
                <span class="title-with-icon rebuild-title">
                  <el-icon><RefreshRight /></el-icon>
                  重建索引
                </span>
                <el-button
                  size="small"
                  type="primary"
                  plain
                  :loading="rebuildStarting || rebuildRunning"
                  :disabled="scanning || rebuildRunning || clearingInvalid"
                  @click="onStartRebuild"
                >
                  开始重建
                </el-button>
              </div>
              <p class="rebuild-purpose">
                适用于磁盘上已有附件但缺少有效索引、或索引与物理文件不一致的情况，用于恢复文件与索引的对应关系，使业务能重新检索和引用这些附件。
              </p>
              <p class="rebuild-desc">
                执行时将扫描游离文件，按内容补建缺失索引、修复损坏记录，并清理与有效索引重复的多余副本。任务在后台运行，完成后自动更新统计数据。
              </p>
              <template v-if="rebuildStatus && (rebuildRunning || rebuildStatus.endTime)">
                <el-progress
                  :percentage="rebuildProgressPercent"
                  :status="rebuildRunning ? undefined : rebuildStatus.failed > 0 ? 'warning' : 'success'"
                  :striped="rebuildRunning"
                  :striped-flow="rebuildRunning"
                />
                <div class="rebuild-stats">
                  <span>总数 {{ rebuildStatus.total ?? 0 }}</span>
                  <span>已处理 {{ rebuildStatus.processed ?? 0 }}</span>
                  <span>新建 {{ rebuildStatus.imported ?? 0 }}</span>
                  <span>修复 {{ rebuildStatus.repaired ?? 0 }}</span>
                  <span>删除 {{ rebuildStatus.deleted ?? 0 }}</span>
                  <span :class="{ 'rebuild-failed': (rebuildStatus.failed ?? 0) > 0 }">失败 {{ rebuildStatus.failed ?? 0 }}</span>
                </div>
                <div class="rebuild-message">{{ rebuildStatus.message }}</div>
              </template>
            </div>

            <div class="clear-block">
              <div class="clear-header">
                <span class="title-with-icon clear-title">
                  <el-icon><Delete /></el-icon>
                  清除无效索引
                </span>
                <el-button
                  size="small"
                  type="danger"
                  :loading="clearingInvalid"
                  :disabled="scanning || rebuildRunning || clearingInvalid"
                  @click="onClearInvalidIndexes"
                >
                  确认清除
                </el-button>
              </div>
              <p class="clear-desc">
                从数据库移除全部无效索引（未索引、区块不完整、校验中等），磁盘上的附件文件不会被删除。
              </p>
              <p class="clear-warn">
                若业务数据仍引用这些索引，删除后关联将永久断开；即便文件还在附件池中，复制回来或重建索引也无法恢复原有引用。操作前请确认无业务依赖。
              </p>
            </div>
          </div>
        </el-scrollbar>
      </el-tab-pane>

      <el-tab-pane label="附件池诊断" name="details" lazy>
        <AttachPoolDetails />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="statExplainVisible" :title="statExplainTitle" width="520px" @close="closeStatExplain">
      <div class="stat-explain-body">
        <p class="stat-explain-text">{{ statExplainIntro }}</p>
        <div v-if="statExplainTip" class="stat-explain-tip">
          <span class="stat-explain-tip-label">修复方式</span>
          <p class="stat-explain-tip-text">{{ statExplainTip }}</p>
        </div>
      </div>
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
import { CircleCheck, CircleClose, Coin, DataLine, Delete, FolderOpened, PieChart, RefreshRight, Warning } from "@element-plus/icons-vue";
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
  statExplainIntro,
  statExplainTip,
  loadRecord,
  onQuickScan,
  onDeepScan,
  onStartRebuild,
  onClearInvalidIndexes,
  rebuildStatus,
  rebuildStarting,
  rebuildRunning,
  rebuildProgressPercent,
  clearingInvalid,
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
  grid-template-columns: repeat(5, 1fr);
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

.stat-explain-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-explain-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

.stat-explain-tip {
  padding: 10px 12px;
  background-color: #f0f7ff;
  border: 1px solid var(--el-color-primary-light-7);
}

.stat-explain-tip-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.stat-explain-tip-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

.rebuild-block {
  padding: 12px 16px;
  background-color: #f0f7ff;
  border: 1px solid var(--el-color-primary-light-7);
}

.rebuild-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.rebuild-title .el-icon {
  color: var(--el-color-primary);
}

.rebuild-purpose {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
}

.rebuild-desc {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.rebuild-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.rebuild-failed {
  color: var(--el-color-danger);
  font-weight: 600;
}

.rebuild-message {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.clear-block {
  padding: 12px 16px;
  background-color: #fff5f5;
  border: 1px solid var(--el-color-danger-light-7);
}

.clear-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.clear-title .el-icon {
  color: var(--el-color-danger);
}

.clear-desc {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.clear-warn {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-color-danger);
  font-weight: 500;
}

:deep(.el-tabs--top .el-tabs__item.is-top:nth-child(2)) {
  padding-left: 20px;
}

:deep(.el-tabs__active-bar) {
  border-radius: 0;
}
</style>
