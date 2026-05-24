<!--
 * @Description: 表格勾选列组件
 * @Author: KspTooi
 * @Since 1.6.Z(26).8
-->
<template>
  <el-table-column :width="width" :align="align" :fixed="fixed" v-bind="$attrs">
    <template #header>
      <el-checkbox
        v-if="mode === 'multiple' || mode === 'multiple-in-page'"
        style="height: auto"
        :disabled="readonly"
        :model-value="isPageChecked()"
        :indeterminate="isPageHalfChecked()"
        @change="togglePageCheck"
      />
      <span v-else></span>
    </template>

    <template #default="{ row }">
      <el-radio
        v-if="mode === 'single'"
        :disabled="readonly"
        :model-value="isRowChecked(row) ? getRowKeyValue(row) : ''"
        :label="getRowKeyValue(row)"
        @click.stop="toggleCheck(row)"
      />

      <el-checkbox
        v-else
        style="height: auto"
        :disabled="readonly"
        :model-value="isRowChecked(row)"
        @change="toggleCheck(row)"
        @click.stop
      />
    </template>
  </el-table-column>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import StdTableCheckService from "@/soa/std-series/service/StdTableCheckService"; // 引入前面写好的纯数据 Service
import type { Mode } from "@/soa/std-series/service/StdTableCheckService";

const props = withDefaults(
  defineProps<{
    data?: any[];
    rowKey?: string | ((row: any) => string | number);
    mode?: Mode;
    width?: string | number;
    align?: string;
    fixed?: string | boolean;

    //是否只读
    readonly?: boolean;
  }>(),
  {
    data: () => [],
    rowKey: "id",
    mode: "multiple",
    width: 55,
    align: "center",
    fixed: false,
    readonly: false,
  }
);

//已经勾选的行数据IDS 外部用v-model绑定
const bindCheckedIds = defineModel<string[]>({ required: true });

//已经勾选的行数据VO 外部用v-model:checked-rows绑定
const bindCheckedRows = defineModel<any[]>("checkedRows", { default: () => [] });

// 动态计算 rowKey 的值
const getRowKeyValue = (row: any): string | number => {
  if (typeof props.rowKey === "function") {
    return props.rowKey(row);
  }
  return row[props.rowKey];
};

// 注入勾选服务
const {
  isRowChecked,
  isPageChecked,
  isPageHalfChecked,
  togglePageCheck,
  clearCheck,
  toggleCheck,
  onElRowCheck: _onElRowCheck,
  checkedRows,
} = StdTableCheckService.useStdTableCheck({
  mode: props.mode,
  listData: computed(() => props.data), // 保持响应式
  checkedIds: bindCheckedIds,
  checkedRows: bindCheckedRows,
  rowKey: getRowKeyValue,
});

/**
 * 监听已经勾选的行数据VO，并同步给外部
 */
/* watch(
  checkedRows,
  (val) => {
    bindCheckedRows.value = val;
  },
  { deep: true }
);
 */
defineExpose({
  onElRowCheck: (row: any) => {
    //如果是只读模式，则不进行勾选
    if (props.readonly) {
      return;
    }

    _onElRowCheck(row);
  },
  clearCheck,
});
</script>

<style scoped>
:deep(.el-checkbox__inner) {
  transition: none !important;
  background: linear-gradient(90deg, var(--el-color-primary-light-9) 0%, white 100%);
  border-color: var(--el-color-primary-light-5);
}
:deep(.el-checkbox__input.is-checked .el-checkbox__inner),
:deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
}
:deep(.el-checkbox__inner::after) {
  transition: none !important;
}
:deep(.el-radio__label) {
  display: none;
}
</style>
