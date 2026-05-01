<template>
  <div class="oper-time-edit">
    <div class="oper-time-edit__item">
      <el-date-picker
        ref="startPickerRef"
        v-model="rangeStartValue"
        class="min-w-0!"
        :type="type === 'datetimerange' ? 'datetime' : 'date'"
        :placeholder="startPlaceholder"
        :value-format="valueFormat"
        :format="valueFormat"
        :disabled="disableds?.[0]"
        :disabled-date="resolveDisableStartDate"
        :disabled-hours="resolveDisableStartHours"
        :disabled-minutes="resolveDisableStartMinutes"
        :disabled-seconds="resolveDisableStartSeconds"
        @visible-change="onStartPickerVisibleChange"
      />
      <div v-if="showMsg && startValidateMessage" class="oper-time-edit__message">{{ startValidateMessage }}</div>
    </div>
    <span class="oper-time-edit__separator">{{ rangeSeparator }}</span>
    <div class="oper-time-edit__item">
      <el-date-picker
        ref="endPickerRef"
        v-model="rangeEndValue"
        class="min-w-0!"
        :type="type === 'datetimerange' ? 'datetime' : 'date'"
        :placeholder="endPlaceholder"
        :value-format="valueFormat"
        :format="valueFormat"
        clearable
        :disabled="disableds?.[1]"
        :disabled-date="resolveDisableEndDate"
        :disabled-hours="resolveDisableEndHours"
        :disabled-minutes="resolveDisableEndMinutes"
        :disabled-seconds="resolveDisableEndSeconds"
        @visible-change="onEndPickerVisibleChange"
      />
      <div v-if="showMsg && endValidateMessage" class="oper-time-edit__message">{{ endValidateMessage }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import StdTimeRangeService from "@/soa/std-series/service/StdTimeRangeService";

/**
 * 默认属性
 * type: 日期范围类型
 * startPlaceholder: 开始日期占位符
 * endPlaceholder: 结束日期占位符
 * valueFormat: 日期格式
 * clearable: 是否可清除
 * rangeSeparator: 范围分隔符
 */
const props = withDefaults(
  defineProps<{
    type?: "daterange" | "datetimerange"; //日期范围类型，daterange: 日期范围，datetimerange: 日期时间范围
    startPlaceholder?: string; //开始日期占位符
    endPlaceholder?: string; //结束日期占位符
    valueFormat?: string; //日期格式
    clearable?: boolean; //是否可清除
    rangeSeparator?: string; //范围分隔符
    disableds?: boolean[]; //是否禁用
    showMsg?: boolean; //是否显示验证消息
    width?: string; //宽度
    isFocus?: boolean; //是否聚焦
    disableStartDate?: (date: Date) => boolean; //开始日期禁用逻辑
    disableEndDate?: (date: Date) => boolean; //结束日期禁用逻辑
  }>(),
  {
    type: "datetimerange",
    startPlaceholder: "开始日期",
    endPlaceholder: "结束日期",
    valueFormat: "YYYY-MM-DD HH:mm:ss",
    clearable: true,
    rangeSeparator: "至",
    disableds: () => [false, false],
    showMsg: true,
    width: "100%",
    isFocus: true,
    disableStartDate: undefined,
    disableEndDate: undefined,
  }
);

/**
 * 开始日期   --- table筛选 选中单个时间重置时要注意 需要把传值置为null 为空监听不到 重置会无效
 */
const rangeStart = defineModel<string>("rangeStart", { default: "" });
/**
 * 结束日期   --- table筛选 选中单个时间重置时要注意 需要把传值置为null 为空监听不到 重置会无效
 */
const rangeEnd = defineModel<string>("rangeEnd", { default: "" });

const {
  startPickerRef,
  endPickerRef,
  rangeStartValue,
  rangeEndValue,
  onStartPickerVisibleChange,
  onEndPickerVisibleChange,
  startValidateMessage,
  endValidateMessage,
  resolveDisableStartDate,
  resolveDisableEndDate,
  resolveDisableStartHours,
  resolveDisableStartMinutes,
  resolveDisableStartSeconds,
  resolveDisableEndHours,
  resolveDisableEndMinutes,
  resolveDisableEndSeconds,
} = StdTimeRangeService.useStdTimeRange({
  rangeStart,
  rangeEnd,
  getType: () => props.type,
  getIsFocus: () => props.isFocus,
  getDisableStartDate: () => props.disableStartDate,
  getDisableEndDate: () => props.disableEndDate,
});
</script>

<style scoped>
.oper-time-edit {
  display: flex;
  align-items: flex-start !important;
  gap: 8px;
  width: 100%;
}

.oper-time-edit__item {
  flex: 1;
  min-width: 0;
}

.oper-time-edit__message {
  margin-top: 4px;
  /* color: var(--el-color-danger); */
  color: #a8abb2;
  font-size: 12px;
  line-height: 1.2;
}

.oper-time-edit :deep(.el-date-editor) {
  width: v-bind(width) !important;
  min-width: 0;
}

.oper-time-edit__separator {
  color: var(--el-text-color-regular);
  white-space: nowrap;
}
</style>
