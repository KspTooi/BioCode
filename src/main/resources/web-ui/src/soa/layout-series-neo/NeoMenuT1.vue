<template>
  <nav v-loading="loading" class="neo-menu-t1">
    <div ref="scrollRef" class="neo-menu-t1__scroll" @wheel="onWheel">
      <div class="neo-menu-t1__list">
        <button
          v-for="item in menuTree"
          :key="item.id"
          type="button"
          class="neo-menu-t1__item"
          :class="{ 'is-active': isTopMenuActive(item) }"
          @click="onTopMenuClick(item)"
        >
          <el-icon v-if="item.icon" class="neo-menu-t1__icon">
            <component :is="resolveIcon(item.icon)" />
          </el-icon>
          <span class="neo-menu-t1__label">{{ item.name }}</span>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElIcon } from "element-plus";
import ComIconService from "@/soa/com-series/service/ComIconService.ts";
import NeoMenuService from "@/soa/layout-series-neo/service/NeoMenuService.ts";

const scrollRef = ref<HTMLElement | null>(null);

const { menuTree, loading, isTopMenuActive, onTopMenuClick } = NeoMenuService.useNeoMenuT1();
const { resolveIcon } = ComIconService.useIconService();

const onWheel = (event: WheelEvent): void => {
  const el = scrollRef.value;
  if (!el) {
    return;
  }

  if (el.scrollWidth <= el.clientWidth) {
    return;
  }

  event.preventDefault();
  el.scrollLeft += event.deltaY;
};
</script>

<style scoped>
.neo-menu-t1 {
  flex-shrink: 0;
  height: 35px;
  box-sizing: border-box;
  background-color: var(--com-menu-bg, #4195e8);
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.12);
}

.neo-menu-t1__scroll {
  height: 35px;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.neo-menu-t1__scroll::-webkit-scrollbar {
  display: none;
}

.neo-menu-t1__list {
  display: flex;
  flex-wrap: nowrap;
  align-items: stretch;
  height: 100%;
  min-width: min-content;
}

.neo-menu-t1__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  height: 100%;
  padding: 0 16px;
  border: none;
  background: transparent;
  color: var(--com-menu-text, #fff);
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
  transition:
    background-color 0.2s,
    color 0.2s;
}

.neo-menu-t1__item:hover {
  background-color: var(--com-menu-hover-bg, rgba(255, 255, 255, 0.1));
}

.neo-menu-t1__item.is-active {
  background-color: #fff;
  color: var(--com-menu-bg, #4195e8);
  font-weight: 500;
}

.neo-menu-t1__item.is-active:hover {
  background-color: #fff;
}

.neo-menu-t1__icon {
  font-size: 16px;
  color: inherit;
}

.neo-menu-t1__label {
  line-height: 1;
}
</style>
