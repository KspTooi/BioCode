<template>
  <div class="flex h-full w-full overflow-hidden min-h-0">
    <el-container class="h-full w-full overflow-hidden min-h-0">
      <!-- 桌面版侧边栏 -->
      <component
        :is="isMenuCollapse ? DefaultMenuShort : DefaultMenu"
        title="EAS服务管理控制台"
        :version="`版本:${appVersion}`"
      />

      <el-container class="h-full w-full overflow-hidden min-h-0">
        <!-- 多标签页区域 -->
        <com-multi-tab>
          <template #controls>
            <div class="flex h-full w-full items-center mr-[15px]">
              <!-- 用户通知下拉菜单 -->
              <core-user-notice-drop-menu />
              <!-- 用户信息和下拉菜单-->
              <com-user-profile />
            </div>
          </template>
        </com-multi-tab>

        <!-- 头部区域 -->
        <el-header class="admin-header" height="35px">
          <div class="flex items-center h-full">
            <div class="menu-toggle" @click="toggleMenu">
              <ILineMdMenuUnfoldRight />
            </div>
            <!-- 面包屑导航，放在头部区域 -->
            <ComBreadcrumb />
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="admin-content">
          <div class="content-wrapper">
            <!-- 路由视图 -->
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
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ElBreadcrumb, ElBreadcrumbItem, ElContainer, ElHeader, ElMain } from "element-plus";
import { useRoute } from "vue-router";
import { computed } from "vue";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import DefaultLayoutService from "@/soa/layout-series-default/service/DefaultLayoutService.ts";
import ComMultiTab from "@/soa/com-series/components/ComMultiTab.vue";
import DefaultMenu from "@/soa/layout-series-default/DefaultMenu.vue";
import ComUserProfile from "@/soa/com-series/components/ComUserProfile.vue";
import DefaultMenuShort from "@/soa/layout-series-default/DefaultMenuShort.vue";
import CoreUserNoticeDropMenu from "@/views/core/components/public/CoreUserNoticeDropMenu.vue";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";
import ComBreadcrumb from "@/soa/com-series/ComBreadcrumb.vue";

const route = useRoute();

//获取标签服务（含路由同步）
const { refreshCounter } = ComTabService.useRouterTabService();

//viewKey随路径或刷新计数器变化，用于强制重建非keep-alive页面组件
const viewKey = computed(() => `${route.fullPath}__${refreshCounter.value}`);

//初始化框架快捷键服务 这是为了CTRL+1~9 快速切换标签
DefaultLayoutService.useComTabHotkey();

//获取框架服务(这包含菜单折叠、菜单展开、面包屑导航等)
const { isMenuCollapse, toggleMenu, autoBreadcrumbs } = DefaultLayoutService.useComFramework();

//获取用户信息
const authStore = UserAuthService.AuthStore();

//获取应用版本
const appVersion = computed(() => {
  const userInfo = authStore.getUserInfo;

  if (!userInfo) {
    return "未知";
  }

  return userInfo.appVersion;
});
</script>

<style scoped>
.admin-header {
  background-color: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 35px;
  flex-shrink: 0;
}

.menu-toggle {
  font-size: 18px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
  height: 100%;
}

.menu-toggle:hover {
  color: #764ba2;
}

.admin-breadcrumb {
  font-size: 13px;
  line-height: 1;
  margin-left: 15px;
  user-select: none;
  cursor: default !important;
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

/* 路由容器占满剩余高度，允许内部滚动 */
.content-wrapper > div {
  flex: 1;
  display: flex;
  min-height: 0;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
