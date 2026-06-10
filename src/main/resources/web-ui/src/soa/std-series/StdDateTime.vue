<template>
  <el-date-picker
    v-model="dateRange"
    class="std-date-time"
    :type="type"
    :format="format"
    :value-format="dateFormat"
    v-bind="$attrs"
    :start-placeholder="startPlaceholder"
    :end-placeholder="endPlaceholder"
    :range-separator="rangeSeparator"
    :style="{ width: width }"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { ElDatePicker } from "element-plus";
const props = withDefaults(
  defineProps<{
    startTime?: string;
    endTime?: string;
    type?: "datetimerange" | "daterange";
    format?: string;
    dateFormat?: string;
    startPlaceholder?: string;
    endPlaceholder?: string;
    rangeSeparator?: string;
  }>(),
  {
    startTime: undefined,
    endTime: undefined,
    type: undefined,
    format: undefined,
    dateFormat: undefined,
    startPlaceholder: "开始时间",
    endPlaceholder: "结束时间",
    rangeSeparator: "-",
  }
);

const emit = defineEmits<{
  (e: "update:startTime", val: string): void;
  (e: "update:endTime", val: string): void;
}>();

const width = computed(() => {
  if (props.type === "datetimerange") {
    return "355px";
  }
  if (props.type === "daterange") {
    return "260px";
  }
  return "auto";
});

const dateRange = computed({
  get() {
    if (!props.startTime && !props.endTime) {
      return null;
    }
    return [props.startTime ?? "", props.endTime ?? ""];
  },
  set(val: [string, string] | null) {
    if (!val) {
      emit("update:startTime", "");
      emit("update:endTime", "");
      return;
    }
    emit("update:startTime", val[0]);
    emit("update:endTime", val[1]);
  },
});

function handleChange(val: [string, string] | null): void {
  if (!val) {
    emit("update:startTime", "");
    emit("update:endTime", "");
    return;
  }
  emit("update:startTime", val[0]);
  emit("update:endTime", val[1]);
}
</script>

<style scoped>
.std-date-time {
  container-type: inline-size;
}

/* @container (min-width: 355px) { */
/* :global(.el-range-separator) {
  display: inline-block !important;
  width: auto !important;
  flex: none !important;
} */
:global(.el-range-input) {
  width: 43% !important;
}
/* } */
</style>
