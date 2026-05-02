<template>
  <div class="std-query-collapse">
    <div class="flex flex-wrap flex-1 std-query-collapse-content">
      <SlotNodes :nodes="visibleSlotNodes" />
    </div>
    <div class="std-query-collapse-actions">
      <slot name="actions"></slot>
      <el-button v-if="hasMore" type="primary" link class="ml-2" @click="toggleExpand">
        {{ isExpand ? "收起" : "展开" }}
        <el-icon class="el-icon--right">
          <component :is="isExpand ? ArrowUp : ArrowDown" />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, Comment, defineComponent, Fragment, Text, useSlots, type PropType, type VNode } from "vue";
import { ArrowDown, ArrowUp } from "@element-plus/icons-vue";
import { useSearchExpand } from "@/soa/std-series/service/StdQueryCollapseService.ts";

const props = withDefaults(
  defineProps<{
    limit?: number;
  }>(),
  {
    limit: 3,
  }
);

const slots = useSlots();
const { isExpand, toggleExpand } = useSearchExpand();

/**
 * 插槽节点组件，用于渲染插槽节点
 * @param componentProps 组件属性
 * @returns 插槽节点
 */
const SlotNodes = defineComponent({
  name: "SlotNodes",
  props: {
    nodes: {
      type: Array as PropType<VNode[]>,
      default: () => [],
    },
  },
  setup(componentProps) {
    return () => componentProps.nodes;
  },
});

/**
 * 规范化插槽节点，将插槽节点转换为数组
 * @param nodes 插槽节点
 * @returns 规范化后的插槽节点
 */
function normalizeSlotNodes(nodes: VNode[]): VNode[] {
  return nodes.flatMap((node) => {
    if (node.type === Comment) {
      return [];
    }

    if (node.type === Text) {
      const text = String(node.children ?? "").trim();

      if (!text) {
        return [];
      }
    }

    if (node.type !== Fragment) {
      return [node];
    }

    if (!Array.isArray(node.children)) {
      return [];
    }

    return normalizeSlotNodes(node.children as VNode[]);
  });
}
/**
 * 默认插槽节点，用于获取默认插槽节点
 * @returns 默认插槽节点
 */
const defaultSlotNodes = computed(() => {
  if (!slots.default) {
    return [];
  }

  return normalizeSlotNodes(slots.default());
});
/**
 * 可见插槽节点，用于获取可见插槽节点，如果展开则返回所有插槽节点，否则返回前limit个插槽节点
 * @returns 可见插槽节点
 */
const visibleSlotNodes = computed(() => {
  if (isExpand.value) {
    return defaultSlotNodes.value;
  }

  return defaultSlotNodes.value.slice(0, props.limit);
});
/**
 * 是否有更多插槽节点，用于判断是否有更多插槽节点
 * @returns 是否有更多插槽节点
 */
const hasMore = computed(() => defaultSlotNodes.value.length > props.limit);
</script>

<style scoped>
.std-query-collapse {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.std-query-collapse-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 16px;
}
</style>
