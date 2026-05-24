<template>
  <com-layout-provider />
</template>

<script setup lang="ts">
import ComLayoutProvider from "@/soa/com-series/ComLayoutProvider.vue";
</script>

<style>
/* 全局样式，禁止页面滚动 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

/* 按钮内图标样式 */
.icon-in-button {
  margin-right: 4px;
}
</style>
