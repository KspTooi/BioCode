<template>
  <div class="flex h-full w-full flex-col overflow-hidden min-h-0">
    <neo-black-top :logo-url="logoUrl" title="EAS CROWN管理台">
      <notice-drop-menu />
      <com-user-profile />
    </neo-black-top>

    <neo-menu-t1 />

    <div class="neo-body flex flex-1 min-h-0 w-full overflow-hidden">
      <neo-menu-t2 />

      <el-main class="admin-content flex-1 min-h-0 overflow-hidden">
        <com-multi-tab :show-prefix-controls="false" :show-suffix-controls="false" />
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
import NeoBlackTop from "@/soa/layout-series-neo/NeoBlackTop.vue";
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

//初始化框架快捷键服务 这是为了CTRL+1~9 快速切换标签
DefaultLayoutService.useComTabHotkey();

//获取框架服务(这包含菜单折叠、菜单展开、面包屑导航等)
DefaultLayoutService.useComFramework();
</script>

<style scoped>
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
