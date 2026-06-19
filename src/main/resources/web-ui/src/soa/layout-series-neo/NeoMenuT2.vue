<template>
  <aside v-if="menuT2Visible" v-loading="loading" class="qlc-menu-t2">
    <div class="qlc-menu-t2__scroll">
      <template v-for="section in menuSections" :key="section.id">
        <div class="qlc-menu-t2__section">
          <div class="qlc-menu-t2__title">{{ section.title }}</div>
          <button
            v-for="item in section.items"
            :key="item.id"
            type="button"
            class="qlc-menu-t2__item"
            :class="{ 'is-active': item.id === activeMenuId }"
            @click="onMenuItemClick(item)"
          >
            {{ item.name }}
          </button>
        </div>
      </template>
    </div>
  </aside>
</template>

<script setup lang="ts">
import NeoMenuService from "@/soa/layout-series-neo/service/NeoMenuService.ts";

const { loading, activeMenuId, menuSections, menuT2Visible, onMenuItemClick } = NeoMenuService.useNeoMenuT2();
</script>

<style scoped>
.qlc-menu-t2 {
  flex-shrink: 0;
  width: 180px;
  height: 100%;
  background-color: var(--com-menu-bg-t2, #297ecd);
  box-sizing: border-box;
}

.qlc-menu-t2__scroll {
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 12px 10px 16px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.qlc-menu-t2__scroll::-webkit-scrollbar {
  display: none;
}

.qlc-menu-t2__section + .qlc-menu-t2__section {
  margin-top: 18px;
}

.qlc-menu-t2__title {
  font-size: 13px;
  font-weight: bold;
  color: var(--com-menu-text, #fff);
  line-height: 1.4;
  padding: 0 10px 10px;
  user-select: none;
}

.qlc-menu-t2__item {
  display: block;
  width: 100%;
  margin: 0 0 4px;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--com-menu-text-item, rgba(255, 255, 255, 0.85));
  font-size: 14px;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  user-select: none;
  transition: background-color 0.2s;
}

.qlc-menu-t2__item:hover {
  background-color: var(--com-menu-hover-bg, rgba(255, 255, 255, 0.1));
  color: #ffffff;
}

.qlc-menu-t2__item.is-active {
  background-color: var(--com-menu-active-bg, #2a7ac6);
  font-weight: 500;
  color: #ffffff;
}

.qlc-menu-t2__item.is-active:hover {
  background-color: var(--com-menu-active-bg, #2a7ac6);
}
</style>
