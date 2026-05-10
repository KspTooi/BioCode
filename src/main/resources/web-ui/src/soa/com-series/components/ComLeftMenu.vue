<template>
  <el-aside width="210px" class="admin-sidebar">
    <div class="admin-side-panel">
      <!-- LOGO与标题区域 -->
      <div class="title-container">
        <div v-if="title" class="panel-title">
          <img :src="logoUrl" alt="EAS Logo" class="logo-image" />
          {{ title }}
        </div>
        <div v-if="version" class="panel-version">
          {{ version }}
        </div>
      </div>

      <!-- 菜单区域 -->
      <el-menu
        v-loading="loading"
        :default-active="activeMenuId"
        :default-openeds="menuOpened"
        :unique-opened="false"
        class="panel-menu"
        @open="
          (id: string) => {
            expandMenu(id, true);
          }
        "
        @close="
          (id: string) => {
            expandMenu(id, false);
          }
        "
      >
        <!-- 备用维护中心菜单 -->
        <!-- <el-menu-item
          v-if="!hasMaintainCenter"
          index="fallback-maintenance-center"
          @click="openMenu(getMenuByPath('/core/application-maintain'))"
        >
          <el-icon>
            <component :is="resolveIcon('Setting')" />
          </el-icon>
          <span>维护中心(备用)</span>
        </el-menu-item> -->
        <template v-for="item in menuTree" :key="item.id">
          <!-- 菜单项 一级菜单 目录类型 -->
          <el-sub-menu v-if="item.kind === 0" :key="item.id" :index="item.id">
            <template #title>
              <el-icon>
                <component :is="resolveIcon(item.icon)" v-if="item.icon" />
              </el-icon>
              <span>{{ item.name }}</span>
            </template>

            <!-- 菜单项 二级菜单 目录类型 -->
            <el-sub-menu v-for="child in filterDirectoryMenu(item.children)" :key="child.id" :index="child.id">
              <template #title>
                <el-icon>
                  <component :is="resolveIcon(child.icon)" v-if="child.icon" />
                </el-icon>
                <span>{{ child.name }}</span>
              </template>
              <!-- 三级菜单 目录类型 -->
              <el-sub-menu
                v-for="grandChild in filterDirectoryMenu(child.children)"
                :key="grandChild.id"
                :index="grandChild.id"
              >
                <template #title>
                  <el-icon>
                    <component :is="resolveIcon(grandChild.icon)" v-if="grandChild.icon" />
                  </el-icon>
                  <span>{{ grandChild.name }}</span>
                </template>
              </el-sub-menu>
              <!-- 三级菜单 菜单项类型 -->
              <el-menu-item
                v-for="grandChild in filterItemMenu(child.children)"
                :key="grandChild.id"
                :index="grandChild.id"
                @click="openMenu(grandChild)"
              >
                <el-icon>
                  <component :is="resolveIcon(grandChild.icon)" v-if="grandChild.icon" />
                </el-icon>
                <span>{{ grandChild.name }}</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 菜单项 二级菜单 菜单项类型 -->
            <el-menu-item
              v-for="child in filterItemMenu(item.children)"
              :key="child.id"
              :index="child.id"
              @click="openMenu(child)"
            >
              <el-icon>
                <component :is="resolveIcon(child.icon)" v-if="child.icon" />
              </el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 菜单项 一级菜单 菜单项类型 -->
          <el-menu-item v-if="item.kind === 1" :key="item.id" :index="item.id" @click="openMenu(item)">
            <el-icon>
              <component :is="resolveIcon(item.icon)" v-if="item.icon" />
            </el-icon>
            <span>{{ item.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { ElMenu, ElMenuItem, ElSubMenu, ElIcon, ElAside } from "element-plus";
import logoUrl from "@/assets/EAS_CROWN.png";
import ComMenuService from "@/soa/com-series/service/ComMenuService.js";
import ComIconService from "@/soa/com-series/service/ComIconService.js";

// 使用菜单服务
const { menuTree, loading, menuOpened, activeMenuId, loadMenus, expandMenu, openMenu, filterDirectoryMenu, filterItemMenu } =
  ComMenuService.useMenuService();

// 初始化菜单展开状态
onMounted(() => {
  loadMenus();
});

const { resolveIcon } = ComIconService.useIconService();

// 定义组件props
const props = defineProps<{
  title?: string; //标题
  version?: string;
}>();

// 定义事件
const emit = defineEmits<{
  (e: "item-click", itemId: string): void;
  (e: "update:activeItemId", itemId: string): void;
}>();
</script>

<style scoped>
.admin-side-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(to bottom, #f8f9fa, #ffffff);
}

.title-container {
  padding: 8px 8px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-bottom: 1px solid #f0f0f0;
}

.logo-image {
  max-width: 30px;
  height: auto;
}

.panel-title {
  padding: 0;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  color: #430675;
  flex-shrink: 0;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.panel-version {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
  color: #868e96;
  background-color: #f1f3f5;
  border: 1px solid #dee2e6;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  line-height: 1.5;
}

.panel-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
  background: transparent;
  /* 隐藏滚动条但保持滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.panel-menu::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

.panel-menu {
  width: 220px;
}

.item-badge {
  margin-left: 8px;
}

/* 菜单项样式 */
.panel-menu :deep(.el-menu) {
  border-right: none;
  background: transparent;
}

/* 菜单项基础样式 */
.panel-menu :deep(.el-menu-item),
.panel-menu :deep(.el-sub-menu__title) {
  margin: 3px 0;
  height: 42px;
  line-height: 42px;
  /* 移除 transform 过渡 */
  transition:
    background 0.2s,
    color 0.2s;
  position: relative;
}

.panel-menu :deep(.el-menu-item span),
.panel-menu :deep(.el-sub-menu__title span) {
  user-select: none;
}

/* 激活状态 */
.panel-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  font-weight: 500;
}

/* 激活态图标颜色 */
.panel-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff;
}

/* 悬停效果 */
.panel-menu :deep(.el-menu-item:hover),
.panel-menu :deep(.el-sub-menu__title:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

/* 激活且悬停时保持背景 */
.panel-menu :deep(.el-menu-item.is-active:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 子菜单项样式 */
.panel-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  height: 38px;
  line-height: 38px;
  padding-left: 48px !important;
  margin: 2px 0;
}

.panel-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.3);
}

/* 图标基础样式 */
.panel-menu :deep(.el-icon) {
  margin-right: 10px;
  font-size: 17px;
  transition: color 0.2s;
}

/* 展开箭头 */
.panel-menu :deep(.el-sub-menu__icon-arrow) {
  transition: transform 0.3s;
}

.panel-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  transform: rotateZ(180deg);
}
</style>
