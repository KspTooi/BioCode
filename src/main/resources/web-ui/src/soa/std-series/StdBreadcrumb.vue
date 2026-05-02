<template>
  <!-- 面包屑导航，放在头部区域 -->
  <el-breadcrumb v-if="autoBreadcrumbs.length" separator="/" class="admin-breadcrumb">
    <el-breadcrumb-item v-for="(item, index) in autoBreadcrumbs" v-show="isFilterBreadcrumb" :key="index" :to="item.to">
      {{ item.text }}
    </el-breadcrumb-item>
    <el-breadcrumb-item v-show="!isFilterBreadcrumb">
      {{ route.meta.breadcrumb }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watchEffect, computed } from "vue";
import { useRoute } from "vue-router";
import { ElBreadcrumb, ElBreadcrumbItem } from "element-plus";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";

const props = defineProps({
  isDShowHome: {
    type: Boolean,
    default: true,
  },
});
const initBreadcrumbs = computed(() => (props.isDShowHome ? [{ text: "首页", to: { path: "/index" } }] : []));
const route = useRoute();
const autoBreadcrumbs = ref<any[]>([...initBreadcrumbs.value]);
const menuTree = ComMenuService.useMenuService().menuTree;

//是否后端菜单中筛选出面包屑
const isFilterBreadcrumb = ref(false);

/**
 * 查找面包屑路径
 * @param nodes 节点列表
 * @param targetPath 目标路径
 * @returns 面包屑路径
 */
const findBreadcrumbPath = (nodes: any[], targetPath: string): any[] | null => {
  if (!nodes) {
    return null;
  }
  for (let i = 0; i < nodes.length; i++) {
    const node = nodes[i];
    if (node.path === targetPath) {
      return [node];
    }
    if (!node.children) {
      continue;
    }
    if (node.children.length === 0) {
      continue;
    }
    const childPath = findBreadcrumbPath(node.children, targetPath);
    if (childPath) {
      return [node, ...childPath];
    }
  }
  return null;
};

/**
 * 获取第一个路径
 * @param node 节点
 * @returns 第一个路径
 */
const getFirstPath = (node: any): string => {
  if (node.path) {
    return node.path;
  }
  if (!node.children) {
    return "";
  }
  if (node.children.length === 0) {
    return "";
  }
  return getFirstPath(node.children[0]);
};

/**
 * 监听路由变化
 */
watchEffect(() => {
  const currentPath = route.path;
  const pathNodes = findBreadcrumbPath(menuTree.value, currentPath);

  if (!pathNodes) {
    autoBreadcrumbs.value = [...initBreadcrumbs.value];
    isFilterBreadcrumb.value = false;
    return;
  }

  const breadcrumbs = [...initBreadcrumbs.value];

  for (let i = 0; i < pathNodes.length; i++) {
    const node = pathNodes[i];
    const isLast = i === pathNodes.length - 1;
    if (isLast) {
      breadcrumbs.push({ text: node.name, to: undefined });
      continue;
    }
    breadcrumbs.push({ text: node.name, to: { path: getFirstPath(node) } });
  }
  autoBreadcrumbs.value = breadcrumbs;
  isFilterBreadcrumb.value = true;
});
</script>

<style scoped></style>
