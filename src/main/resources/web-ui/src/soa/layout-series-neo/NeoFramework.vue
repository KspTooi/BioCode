<template>
  <div class="flex h-full w-full flex-col overflow-hidden min-h-0">
    <header class="neo-header">
      <div class="neo-header__left">
        <img v-if="logoUrl" :src="logoUrl" alt="logo" class="neo-header__logo" />
        <span class="neo-header__title">EAS CROWN管理台</span>
      </div>
      <div class="neo-header__center">
        <neo-menu-t1 />
      </div>
      <div class="neo-header__right">
        <notice-drop-menu />
        <com-user-profile />
      </div>
    </header>

    <div class="neo-body flex flex-1 min-h-0 w-full overflow-hidden">
      <neo-menu-t2 />

      <el-main class="admin-content flex-1 min-h-0 overflow-hidden">
        <com-multi-tab :show-prefix-controls="false" :show-suffix-controls="false" />
        <div class="content-wrapper">
          <router-view v-slot="{ Component, route: routeSlot }">
            <transition name="fade" mode="out-in">
              <div :key="routeSlot.name || routeSlot.path">
                <keep-alive v-if="routeSlot.meta.keepAlive">
                  <component :is="Component" :key="routeSlot.name || routeSlot.path" />
                </keep-alive>
                <component :is="Component" v-if="!routeSlot.meta.keepAlive" :key="viewKey" />
              </div>
            </transition>
          </router-view>
        </div>
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMain } from "element-plus";
import { useRoute } from "vue-router";
import { computed } from "vue";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import ComUserProfile from "@/soa/com-series/components/ComUserProfile.vue";
import NoticeDropMenu from "@/views/core/public/NoticeDropMenu.vue";
import NeoMenuT1 from "@/soa/layout-series-neo/NeoMenuT1.vue";
import NeoMenuT2 from "@/soa/layout-series-neo/NeoMenuT2.vue";
import logoUrl from "@/assets/EAS_CROWN.png";
import ComMultiTab from "@/soa/com-series/components/ComMultiTab.vue";
import DefaultLayoutService from "@/soa/layout-series-default/service/DefaultLayoutService.ts";

const route = useRoute();

//获取标签服务（含路由同步）
const { refreshCounter } = ComTabService.useRouterTabService();

//viewKey随路径或刷新计数器变化，用于强制重建非keep-alive页面组件
const viewKey = computed(() => `${route.fullPath}__${refreshCounter.value}`);

//初始化框架快捷键服务
DefaultLayoutService.useComTabHotkey();

//获取框架服务(这包含菜单折叠、菜单展开、面包屑导航等)
DefaultLayoutService.useComFramework();
</script>

<style scoped>
@font-face {
  font-family: "NeoTitleFont";
  src: url("@/styles/font_title.ttf") format("truetype");
  font-display: swap;
}

.neo-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  flex-shrink: 0;
  padding: 0 16px;
  background-color: #0f172a;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.neo-header__left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  min-width: 0;
}

.neo-header__logo {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.neo-header__title {
  font-family: "NeoTitleFont", sans-serif;
  font-size: 20px;
  font-weight: normal;
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.5px;
  user-select: none;
}

.neo-header__center {
  flex: 1;
  min-width: 0;
  margin: 0 24px;
  height: 100%;
}

.neo-header__right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  flex-shrink: 0;
  min-width: 0;
  height: 100%;
}

.neo-header__right :deep(.control-btn) {
  color: rgba(255, 255, 255, 0.88);
}

.neo-header__right :deep(.control-btn:hover) {
  color: #ffffff;
}

.neo-header__right :deep(.notice-dropdown > div:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.neo-header__right :deep(.user-info:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.neo-header__right :deep(.username-display) {
  color: rgba(255, 255, 255, 0.92);
}

.neo-body {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.admin-content {
  background-color: var(--el-bg-color-page);
  padding: 0;
  height: 100%;
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}

.content-wrapper {
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.content-wrapper > div {
  flex: 1;
  display: flex;
  min-height: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>