<template>
  <div v-loading="recordsLoading" class="records-container">
    <el-empty v-if="!recordsLoading && records.length === 0" description="暂无流转记录" />
    <el-timeline v-if="records.length > 0" class="records-timeline">
      <el-timeline-item
        v-for="(record, idx) in records"
        :key="idx"
        placement="top"
        :color="
          record.status === 0
            ? '#409EFF'
            : record.action === 0
              ? '#67C23A'
              : record.action === 1
                ? '#f57c00'
                : record.status === 10
                  ? '#c62828'
                  : record.status === 1
                    ? '#3f51b5'
                    : '#C0C4CC'
        "
      >
        <template #dot>
          <div
            class="timeline-dot"
            :class="{
              'dot-pending': record.status === 0,
              'dot-approve': record.action === 0,
              'dot-reject': record.action === 1,
              'dot-cancel': record.status === 10,
              'dot-done': record.status === 1 && record.action !== 0 && record.action !== 1,
            }"
          >
            <el-icon size="12">
              <ClockIcon v-if="record.status === 0" />
              <CheckIcon v-else-if="record.action === 0" />
              <CloseIcon v-else-if="record.action === 1" />
              <RemoveIcon v-else-if="record.status === 10" />
              <SuccessIcon v-else />
            </el-icon>
          </div>
        </template>

        <div class="record-card">
          <div class="record-header">
            <div class="record-left">
              <span class="record-node">{{ record.nodeName }}</span>
              <el-tag v-if="record.status === 0" type="primary" size="small" effect="light" round> 待处理 </el-tag>
              <el-tag v-else-if="record.action === 0" type="success" size="small" effect="light" round> 已通过 </el-tag>
              <el-tag v-else-if="record.action === 1" type="warning" size="small" effect="light" round> 已驳回 </el-tag>
              <el-tag v-else-if="record.status === 10" type="danger" size="small" effect="light" round> 已作废 </el-tag>
              <el-tag
                v-else-if="record.status === 1"
                size="small"
                effect="light"
                round
                style="color: #3f51b5; border-color: #c5cae9; background: #e8eaf6"
              >
                已处理
              </el-tag>
              <el-tag v-else type="info" size="small" effect="light" round> — </el-tag>
            </div>
            <span v-if="record.status === 0" class="record-time record-time-pending">等待处理中…</span>
          </div>

          <div class="record-divider" />

          <div class="record-body">
            <div class="record-meta">
              <el-icon><UserIcon /></el-icon>
              <span class="meta-label">处理人:</span>
              <span class="meta-value">{{ record.finMemberName || "—" }}</span>
            </div>
            <div v-if="record.finTime" class="record-meta">
              <el-icon><CalendarIcon /></el-icon>
              <span class="meta-label">处理时间:</span>
              <span class="meta-value">{{ record.finTime }}</span>
            </div>
            <div v-if="record.comment" class="record-comment">
              <el-icon><ChatIcon /></el-icon>
              <span class="meta-label">处理意见</span>
              <span class="comment-text">{{ record.comment }}</span>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import { markRaw } from "vue";
import { User, ChatLineRound, Clock, Check, Close, Remove, Select, Calendar } from "@element-plus/icons-vue";
import QfProcHistoryService, { type QfProcHistoryProps } from "@/views/qf/public/service/QfProcHistoryService";

const UserIcon = markRaw(User);
const ChatIcon = markRaw(ChatLineRound);
const ClockIcon = markRaw(Clock);
const CheckIcon = markRaw(Check);
const CloseIcon = markRaw(Close);
const RemoveIcon = markRaw(Remove);
const SuccessIcon = markRaw(Select);
const CalendarIcon = markRaw(Calendar);

const props = defineProps<QfProcHistoryProps>();

const { records, recordsLoading } = QfProcHistoryService.useQfProcHistory(props);
</script>

<style scoped>
.records-container {
  padding: 16px 8px 16px 4px;
  width: 100%;
  box-sizing: border-box;
}

.records-timeline {
  padding-left: 4px;
}

/* 自定义 dot */
.timeline-dot {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
  margin-left: -6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.dot-pending {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.dot-approve {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #67c23a;
}

.dot-reject {
  background: #fdf6ec;
  border-color: #f57c00;
  color: #f57c00;
}

.dot-cancel {
  background: #fef0f0;
  border-color: #c62828;
  color: #c62828;
}

.dot-done {
  background: #e8eaf6;
  border-color: #3f51b5;
  color: #3f51b5;
}

/* 卡片 */
.record-card {
  background: #fff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.record-card:hover {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--el-fill-color-extra-light);
  gap: 8px;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.record-node {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-time {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.record-time-pending {
  color: #409eff;
  font-style: italic;
}

.record-divider {
  height: 1px;
  background: var(--el-border-color-lighter);
}

.record-body {
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.meta-label {
  color: var(--el-text-color-placeholder);
}

.meta-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.record-comment {
  display: flex;
  align-items: baseline;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.record-comment .meta-label {
  white-space: nowrap;
  flex-shrink: 0;
}

.comment-text {
  color: var(--el-text-color-regular);
  word-break: break-all;
  overflow-wrap: break-word;
  white-space: normal;
}
</style>
