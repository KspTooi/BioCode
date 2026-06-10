<template>
  <component :is="authComponent" />
</template>

<script setup lang="ts">
import ComAuthProvider from "@/soa/com-series/service/ComAuthProvider.ts";

const { authComponent } = ComAuthProvider.useAuthComponent();
</script>
