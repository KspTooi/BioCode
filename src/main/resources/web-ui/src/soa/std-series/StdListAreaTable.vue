<!--
 * @Description: 列表页表格区容器，可选内置分页或 #pagination 插槽
 * @Author: luohangyu osv6249@dingtalk.com
 * @Date: 2026-04-30 16:49:45
 * @LastEditors: luohangyu osv6249@dingtalk.com
 * @LastEditTime: 2026-05-20 10:07:33
 * @Since 1.6.Z(26).5
-->
<template>
  <div class="std-list-area-table">
    <!-- 默认插槽：表格主体 -->
    <slot></slot>
    <!-- 有自定义分页插槽，或内置分页三要素齐全时才渲染分页区 -->
    <div v-if="showPaginationArea" class="std-pagination-container">
      <slot name="pagination">
        <!-- 未提供 #pagination 且 listForm + loadList + listTotal 齐全时使用内置分页 -->
        <el-pagination
          v-if="showBuiltinPagination && listForm"
          v-model:current-page="listForm.pageNum"
          v-model:page-size="listForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="listTotal!"
          background
          @size-change="onPageSizeChange"
          @current-change="onPageNumChange"
        />
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from "vue";
import type PageQuery from "@/commons/model/PageQuery";

const slots = useSlots();

/** 分页参数，非必填；树形/全量列表等场景可不传 */
const listForm = defineModel<PageQuery>("listForm", { required: false });

const props = defineProps<{
  /** 数据总条数，传给 el-pagination 的 total */
  listTotal?: number;
  /** 翻页回调，size-change / current-change 时触发 */
  loadList?: () => any;
}>();

/** 内置分页三要素：listForm、loadList、listTotal（0 也算有效） */
const showBuiltinPagination = computed(() => listForm.value != null && props.loadList != null && props.listTotal !== undefined);

/** 显示分页区：自定义 #pagination，或满足内置分页条件 */
const showPaginationArea = computed(() => !!slots.pagination || showBuiltinPagination.value);

/** 每页条数变化 */
const onPageSizeChange = (val: number): void => {
  if (!listForm.value || !props.loadList) {
    return;
  }
  listForm.value.pageSize = val;
  props.loadList();
};

/** 当前页变化 */
const onPageNumChange = (val: number): void => {
  if (!listForm.value || !props.loadList) {
    return;
  }
  listForm.value.pageNum = val;
  props.loadList();
};
</script>

<style scoped>
.std-list-area-table {
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.std-list-area-table :deep(.el-table) {
  flex: 1;
  overflow: hidden;
}

.std-pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  padding-bottom: 15px;
  flex-shrink: 0;
}
</style>
