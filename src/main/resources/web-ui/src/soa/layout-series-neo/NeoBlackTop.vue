<template>
  <header class="neo-black-top">
    <div class="neo-black-top__left">
      <img v-if="logoUrl" :src="logoUrl" alt="logo" class="neo-black-top__logo" />
    </div>
    <div class="neo-black-top__center">
      <span class="neo-black-top__title">{{ title }}</span>
    </div>
    <div class="neo-black-top__right">
      <slot />
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{
  logoUrl?: string;
  title: string;
}>();
</script>

<style scoped>
@font-face {
  font-family: "NeoTitleFont";
  src: url("@/styles/font_title.ttf") format("truetype");
  font-display: swap;
}

.neo-black-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  height: 35px;
  flex-shrink: 0;
  padding: 0 16px;
  background-color: #273542;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.neo-black-top__left {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-width: 0;
}

.neo-black-top__logo {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.neo-black-top__center {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  padding: 0 12px;
}

.neo-black-top__title {
  font-family: "NeoTitleFont", sans-serif;
  font-size: 22px;
  font-weight: normal;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.5px;
  user-select: none;
}

.neo-black-top__right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  min-width: 0;
  height: 100%;
}

.neo-black-top__right :deep(.control-btn) {
  color: rgba(255, 255, 255, 0.88);
}

.neo-black-top__right :deep(.control-btn:hover) {
  color: #fff;
}

.neo-black-top__right :deep(.notice-dropdown > div:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.neo-black-top__right :deep(.user-info:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.neo-black-top__right :deep(.username-display) {
  color: rgba(255, 255, 255, 0.92);
}
</style>
