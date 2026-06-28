<template>
  <StdListContainer nopadding>
    <el-scrollbar class="pool-scroll">
      <div class="pool-content">
        <el-card v-loading="loading" shadow="never" class="pool-card">
          <template #header>
            <div class="card-header">
              <span class="title-with-icon">
                <el-icon><FolderOpened /></el-icon>
                QSP 文件池
              </span>
              <div class="header-actions">
                <el-button size="small" :loading="loading" @click="loadRecord">刷新</el-button>
                <el-button size="small" type="primary" :loading="scanning" @click="onScan">扫描附件池</el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!record && !loading" description="暂无扫描记录，请点击「扫描附件池」开始首次扫描" />

          <template v-if="record">
            <div class="stat-grid">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">已索引附件</div>
                <div class="stat-value">{{ record.indexedCount ?? 0 }}</div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">游离附件</div>
                <div class="stat-value" :class="{ 'stat-warn': (record.driftCount ?? 0) > 0 }">
                  {{ record.driftCount ?? 0 }}
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">附件占用</div>
                <div class="stat-value stat-sm">{{ formatBytes(record.poolAttachesBytes) }}</div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">磁盘总容量</div>
                <div class="stat-value stat-sm">{{ formatBytes(record.poolCapacityBytes) }}</div>
              </el-card>
            </div>

            <el-card shadow="never" class="detail-card">
              <template #header>
                <div class="card-header">
                  <span class="title-with-icon">
                    <el-icon><Document /></el-icon>
                    扫描详情
                  </span>
                  <el-tag
                    size="small"
                    :type="record.scanStatus === 1 ? 'success' : 'warning'"
                    effect="plain"
                  >
                    {{ record.scanStatus === 1 ? "扫描成功" : "正在扫描" }}
                  </el-tag>
                </div>
              </template>

              <div v-if="Number(record.poolCapacityBytes) > 0" class="usage-block">
                <div class="usage-label">
                  <span>磁盘使用率</span>
                  <span>
                    {{
                      Math.min(
                        100,
                        Math.round((Number(record.poolAttachesBytes) / Number(record.poolCapacityBytes)) * 100)
                      )
                    }}%
                  </span>
                </div>
                <el-progress
                  :percentage="
                    Math.min(
                      100,
                      Math.round((Number(record.poolAttachesBytes) / Number(record.poolCapacityBytes)) * 100)
                    )
                  "
                  :stroke-width="14"
                  :status="
                    Number(record.poolAttachesBytes) / Number(record.poolCapacityBytes) > 0.9
                      ? 'exception'
                      : Number(record.poolAttachesBytes) / Number(record.poolCapacityBytes) > 0.7
                        ? 'warning'
                        : 'success'
                  "
                />
              </div>

              <el-descriptions :column="2" border size="small" class="custom-descriptions">
                <el-descriptions-item label="存储池路径" :span="2">{{ record.poolPath }}</el-descriptions-item>
                <el-descriptions-item label="扫描开始时间">{{ record.scanStartTime ?? "-" }}</el-descriptions-item>
                <el-descriptions-item label="扫描结束时间">{{ record.scanEndTime ?? "-" }}</el-descriptions-item>
                <el-descriptions-item label="附件占用字节">{{ formatBytes(record.poolAttachesBytes) }}</el-descriptions-item>
                <el-descriptions-item label="磁盘总容量">{{ formatBytes(record.poolCapacityBytes) }}</el-descriptions-item>
                <el-descriptions-item label="已索引附件数">{{ record.indexedCount ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="游离附件数">{{ record.driftCount ?? 0 }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </template>
        </el-card>
      </div>
    </el-scrollbar>
  </StdListContainer>
</template>

<script setup lang="ts">
import { FolderOpened, Document } from "@element-plus/icons-vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import AttachPoolService from "@/views/core/service/AttachPoolService.ts";

const { record, loading, scanning, loadRecord, onScan, formatBytes } = AttachPoolService.useAttachPoolStatus();
</script>

<style scoped>
.pool-scroll {
  height: 100%;
  background-color: #fff;
}

.pool-content {
  padding: 16px;
}

.pool-card,
.detail-card,
.stat-card {
  border-radius: 0 !important;
  border: 1px solid var(--el-border-color-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 24px;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card :deep(.el-card__body) {
  padding: 16px;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.stat-value.stat-sm {
  font-size: 20px;
}

.stat-warn {
  color: var(--el-color-warning);
}

.usage-block {
  margin-bottom: 16px;
}

.usage-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

:deep(.el-card__header) {
  padding: 8px 16px;
  background-color: #f8f9fb;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-card__body) {
  padding: 16px;
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

:deep(.el-progress-bar__outer) {
  border-radius: 0 !important;
}

:deep(.el-progress-bar__inner) {
  border-radius: 0 !important;
}
</style>
