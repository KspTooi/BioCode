<template>
  <component :is="currentLayout" />
</template>

<script setup lang="ts">
import ComLayoutProviderService from "@/soa/com-series/service/ComLayoutProviderService.ts";

const { currentLayout } = ComLayoutProviderService.useFrameworkLayout();
</script>