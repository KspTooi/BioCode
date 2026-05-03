<!--
 * @Author: KspTooi
 * @Date: 2026-04-29 16:36:26
 * @Description: 自定义标签选择组件，这个组件除了输出CTJ格式的双向绑定外，其他与el-select组件一致。
-->
<template>
  <el-select :model-value="selectValue" multiple v-bind="$attrs" @update:model-value="onChange">
    <el-option v-for="tag in tags" :key="tag.n" :label="tag.n" :value="tag.n" />
  </el-select>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type CustomizeTagJson from "@/commons/model/json/CustomizeTagJson";

/**
 * 标签列表（数据源）
 */
const props = defineProps<{
  tags: CustomizeTagJson[];
}>();

/**
 * 双向绑定的已选标签列表
 */
const model = defineModel<CustomizeTagJson[]>({ default: () => [] });

/**
 * 将已选标签转换为 el-select 使用的字符串数组
 */
const selectValue = computed(() => {
  if (!Array.isArray(model.value)) {
    return [];
  }
  return model.value.map((t) => t.n);
});

/**
 * el-select 选中项变化时同步回 CustomizeTagJson[]
 */
function onChange(val: string[]): void {
  model.value = val.map((n) => ({ n }));
}
</script>

<style scoped></style>
