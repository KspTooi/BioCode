<!--
 * @Description: 页面布局组件
 * @Author: KspTooi
 * @Since 1.6.Z(26).8
-->
<template>
  <el-tabs v-model="activeTab" class="std-pg-tabs w-full h-full px-4">
    <el-tab-pane label="演示" name="demo">
      <el-scrollbar class="std-pg-pane-scroll">
        <slot></slot>
      </el-scrollbar>
    </el-tab-pane>
    <el-tab-pane label="props" name="props">
      <el-scrollbar class="std-pg-pane-scroll">
        <slot name="props"></slot>
      </el-scrollbar>
    </el-tab-pane>
    <el-tab-pane label="emits" name="emits">
      <el-scrollbar class="std-pg-pane-scroll">
        <slot name="emits"></slot>
      </el-scrollbar>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { ref } from "vue";

const activeTab = ref("demo");
</script>

<style scoped>
.std-pg-tabs {
  display: flex;
  flex-direction: column;
}

.std-pg-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 0;
}

.std-pg-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.std-pg-tabs :deep(.el-tab-pane) {
  height: 100%;
}

.std-pg-pane-scroll {
  height: 100%;
}
</style>
