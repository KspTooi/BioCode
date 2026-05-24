<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="选择日期范围">
            <StdDateRange
              v-model:range-start="start"
              v-model:range-end="end"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-tag v-if="start" type="primary">{{ start }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-tag v-if="end" type="success">{{ end }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="类型切换" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="日期类型">
            <el-radio-group v-model="demoType">
              <el-radio value="datetimerange">datetimerange（含时分秒）</el-radio>
              <el-radio value="daterange">daterange（仅日期）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="选择日期范围">
            <StdDateRange
              v-model:range-start="typeStart"
              v-model:range-end="typeEnd"
              :type="demoType"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="已选值">
            <code style="background: var(--el-fill-color-light); padding: 4px 8px; border-radius: 4px; font-size: 12px">{{ JSON.stringify([typeStart, typeEnd]) }}</code>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="定制占位符与分隔符">
        <el-form label-width="120px">
          <el-form-item label="选择日期范围">
            <StdDateRange
              v-model:range-start="customStart"
              v-model:range-end="customEnd"
              start-placeholder="起始"
              end-placeholder="截止"
              range-separator="~"
              type="daterange"
              value-format="YYYY/MM/DD"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="已选值">
            <code style="background: var(--el-fill-color-light); padding: 4px 8px; border-radius: 4px; font-size: 12px">{{ JSON.stringify([customStart, customEnd]) }}</code>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="不可清除">
        <el-form label-width="120px">
          <el-form-item label="选择日期范围">
            <StdDateRange
              v-model:range-start="noClearStart"
              v-model:range-end="noClearEnd"
              :clearable="false"
              type="daterange"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="已选值">
            <code style="background: var(--el-fill-color-light); padding: 4px 8px; border-radius: 4px; font-size: 12px">{{ JSON.stringify([noClearStart, noClearEnd]) }}</code>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="250" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="200" />
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

      <el-divider content-position="left">透传 $attrs</el-divider>
      <el-table :data="attrsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdDateRange from "@/soa/std-series/StdDateRange.vue";

const start = ref("");
const end = ref("");

const demoType = ref<"datetimerange" | "daterange">("datetimerange");
const typeStart = ref("");
const typeEnd = ref("");

const customStart = ref("");
const customEnd = ref("");

const noClearStart = ref("");
const noClearEnd = ref("");

const propsTableData = [
  { name: "type", type: '"daterange" | "datetimerange"', required: "否", default: '"datetimerange"', desc: "日期范围类型" },
  { name: "startPlaceholder", type: "string", required: "否", default: '"开始日期"', desc: "开始日期占位符" },
  { name: "endPlaceholder", type: "string", required: "否", default: '"结束日期"', desc: "结束日期占位符" },
  { name: "valueFormat", type: "string", required: "否", default: '"YYYY-MM-DD HH:mm:ss"', desc: "日期格式化字符串" },
  { name: "clearable", type: "boolean", required: "否", default: "true", desc: "是否可清除" },
  { name: "rangeSeparator", type: "string", required: "否", default: '"至"', desc: "范围分隔符" },
];

const emitsTableData = [
  { name: "update:model-value", payload: "val: [string, string] | null", desc: "日期选择变化时通过 el-date-picker 抛出" },
];

const vModelTableData = [
  { name: "v-model:range-start", type: "string", desc: "开始日期双向绑定" },
  { name: "v-model:range-end", type: "string", desc: "结束日期双向绑定" },
];

const attrsTableData = [
  { name: "disabled", type: "boolean", desc: "透传 el-date-picker，禁用组件" },
  { name: "size", type: "string", desc: "透传 el-date-picker，控制尺寸" },
  { name: "...其他", type: "—", desc: "其余 el-date-picker 属性均可透传" },
];
</script>
