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
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
  box-sizing: border-box;
}

.qlc-menu-t2__scroll {
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 16px 12px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.qlc-menu-t2__scroll::-webkit-scrollbar {
  display: none;
}

.qlc-menu-t2__section + .qlc-menu-t2__section {
  margin-top: 20px;
}

.qlc-menu-t2__title {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  line-height: 1.4;
  padding: 0 10px 8px;
  user-select: none;
}

.qlc-menu-t2__item {
  display: block;
  width: 100%;
  margin: 0 0 4px;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #475569;
  font-size: 14px;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  user-select: none;
  transition:
    background-color 0.2s,
    color 0.2s;
}

.qlc-menu-t2__item:hover {
  background-color: #f1f5f9;
  color: #0f172a;
}

.qlc-menu-t2__item.is-active {
  background-color: #eff6ff;
  font-weight: 600;
  color: #2563eb;
}

.qlc-menu-t2__item.is-active:hover {
  background-color: #eff6ff;
}
</style>