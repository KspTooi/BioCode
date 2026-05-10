<template>
  <StdAdvTree
    ref="advTreeRef"
    v-bind="$attrs"
    :data="dataWithIcons"
    :loading="loading"
    :ni="ICON_FIELD"
    @on-select="emit('on-select', $event)"
    @on-root-select="emit('on-root-select', $event)"
    @on-add="emit('on-add', $event)"
    @on-edit="emit('on-edit', $event)"
    @on-remove="emit('on-remove', $event)"
    @on-search="emit('on-search', $event)"
    @on-refresh="emit('on-refresh', $event)"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

const emit = defineEmits<{
  (e: "on-select", node: GetOrgTreeVo): void;
  (e: "on-root-select", value: string): void;
  (e: "on-add", node: GetOrgTreeVo): void;
  (e: "on-edit", node: GetOrgTreeVo): void;
  (e: "on-remove", node: GetOrgTreeVo): void;
  (e: "on-search", value: string): void;
  (e: "on-refresh", value: string): void;
}>();

const ICON_FIELD = "_icon";

const rawData = ref<GetOrgTreeVo[]>([]);
const loading = ref(false);

const iconMap: Record<number, string> = {
  0: "ep:office-building",
  1: "mdi:domain",
  2: "mdi:sitemap",
  3: "mdi:account-group",
};

const mapIcons = (nodes: GetOrgTreeVo[]): GetOrgTreeVo[] =>
  nodes.map((node) => ({
    ...node,
    [ICON_FIELD]: iconMap[node.kind] ?? "",
    children: node.children?.length ? mapIcons(node.children) : [],
  }));

const dataWithIcons = computed(() => mapIcons(rawData.value));

const advTreeRef = ref();

const loadTreeData = async (): Promise<void> => {
  loading.value = true;
  try {
    rawData.value = await OrgApi.getOrgTree({});
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadTreeData();
});

defineExpose({ loadTreeData, advTreeRef });
</script>
