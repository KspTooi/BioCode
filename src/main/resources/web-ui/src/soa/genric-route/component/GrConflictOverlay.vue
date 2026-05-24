<template>
  <Teleport to="body">
    <div class="grs-overlay" :class="[positionClass, { 'grs-overlay--collapsed': collapsed }]">
      <!-- 折叠态：感叹号按钮 -->
      <button v-if="collapsed" class="grs-overlay__pill" @click="collapsed = false">
        <span style="font-size: 24px"><component :is="resolveIcon('ep:warning')" /></span>
        <span class="grs-overlay__pill-text">检测到路由冲突 ({{ conflicts.length }})</span>
      </button>

      <!-- 展开态 -->
      <template v-else>
        <div class="grs-overlay__header">
          <span class="grs-overlay__title">检测到路由冲突 ({{ conflicts.length }})</span>
          <button class="grs-overlay__btn" @click="collapsed = true">×</button>
        </div>
        <div class="grs-overlay__body">
          <table class="grs-table">
            <thead>
              <tr>
                <th>#</th>
                <th>注册域</th>
                <th>注册路径</th>
                <th>冲突原因</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(c, i) in conflicts" :key="i">
                <td class="grs-table__idx">{{ i + 1 }}</td>
                <td>{{ c.biz }}</td>
                <td>{{ c.path }}</td>
                <td class="grs-table__reason">{{ c.reason }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { RouteEntryWithConflict } from "@/soa/genric-route/api/RouteEntryPo";
import ComIconService from "@/soa/com-series/service/ComIconService";

const { resolveIcon } = ComIconService.useIconService();

defineProps<{ conflicts: RouteEntryWithConflict[] }>();

const collapsed = ref(true);
const positionClass = "grs-overlay--tr";
</script>

<style scoped>
.grs-overlay {
  position: fixed;
  z-index: 99999;
  font-family: ui-monospace, "JetBrains Mono", Menlo, monospace;
  font-size: 12px;
}
.grs-overlay--tr {
  top: 16px;
  right: 16px;
}

/* 折叠态：胶囊按钮 */
.grs-overlay__pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px 6px 8px;
  background: #1e1e1e;
  color: #ff7a7a;
  border: 1px solid #ff5252;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.45);
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  transition: background 0.15s;
}
.grs-overlay__pill:hover {
  background: #2a2a2a;
}
.grs-overlay__pill-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background: #ff5252;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  flex-shrink: 0;
}
.grs-overlay__pill-text {
  white-space: nowrap;
}

/* 展开态 */
.grs-overlay:not(.grs-overlay--collapsed) {
  max-width: 720px;
  max-height: 60vh;
  overflow: auto;
  background: #1e1e1e;
  color: #f5f5f5;
  border: 1px solid #ff5252;
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.45);
}
.grs-overlay__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #2a2a2a;
  border-bottom: 1px solid #444;
  position: sticky;
  top: 0;
  z-index: 1;
}
.grs-overlay__title {
  flex: 1;
  color: #ff7a7a;
  font-weight: 600;
}
.grs-overlay__btn {
  background: transparent;
  color: #ddd;
  border: 1px solid #555;
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  font-family: inherit;
  font-size: 12px;
}
.grs-overlay__body {
  padding: 8px 12px;
}

/* 表格 */
.grs-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
}
.grs-table th,
.grs-table td {
  padding: 5px 10px;
  text-align: left;
  border: 1px solid #3a3a3a;
  word-break: break-all;
}
.grs-table thead tr {
  background: #2a2a2a;
  color: #aaa;
  font-weight: 600;
}
.grs-table tbody tr:nth-child(odd) {
  background: #232323;
}
.grs-table tbody tr:hover {
  background: #2e2e2e;
}
.grs-table__idx {
  color: #888;
  text-align: center;
  width: 28px;
}
.grs-table__reason {
  color: #ffb454;
}
</style>
