<template>
  <div v-loading="loading" class="std-iframe-container">
    <iframe v-if="url" :src="url" class="std-iframe" frameborder="0" @load="onLoad" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRoute } from "vue-router";

const props = defineProps<{
  url?: string;
}>();

const route = useRoute();

const url = computed(() => props.url || (route.query.url as string) || "");

const loading = ref(true);

const onLoad = (): void => {
  loading.value = false;
};

watch(url, () => {
  loading.value = true;
});
</script>

<style scoped>
.std-iframe-container {
  width: 100%;
  height: 100%;
}

.std-iframe {
  width: 100%;
  height: 100%;
}
</style>
