<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择图标">
            <StdIconPicker v-model="selectedIcon" />
          </el-form-item>
          <el-form-item label="已选图标">
            <Icon v-if="selectedIcon" :icon="selectedIcon" :width="24" :height="24" />
            <span v-if="selectedIcon" style="margin-left: 8px">{{ selectedIcon }}</span>
            <span v-if="!selectedIcon" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="预设默认值">
        <el-form label-width="100px">
          <el-form-item label="选择图标">
            <StdIconPicker v-model="presetIcon" />
          </el-form-item>
          <el-form-item label="已选图标">
            <Icon v-if="presetIcon" :icon="presetIcon" :width="24" :height="24" />
            <span v-if="presetIcon" style="margin-left: 8px">{{ presetIcon }}</span>
            <span v-if="!presetIcon" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="250" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="120" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>

    <template #emits>
      <el-table :data="emitsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="事件名" width="220" />
        <el-table-column prop="payload" label="参数" width="250" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">v-model 双向绑定</el-divider>
      <el-table :data="vModelTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="绑定名" width="240" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { Icon } from "@iconify/vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdIconPicker from "@/soa/std-series/StdIconPicker.vue";

const selectedIcon = ref<string | null>(null);
const presetIcon = ref<string | null>("ep:star-filled");

const propsTableData = [
  { name: "modelValue", type: "string | null", required: "否", default: "null", desc: "当前选中的图标名称（含前缀，如 ep:search）" },
];

const emitsTableData = [
  { name: "update:modelValue", payload: "value: string", desc: "选中图标时触发，传递图标名称" },
];

const vModelTableData = [
  { name: "v-model（默认）", type: "string | null", desc: "双向绑定已选图标名称" },
];
</script>
