<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示（日期时间范围）" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="时间范围">
            <StdTimeRange
              v-model:range-start="rangeStart"
              v-model:range-end="rangeEnd"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-tag v-if="rangeStart" type="primary">{{ rangeStart }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
          <el-form-item label="结束时间">
            <el-tag v-if="rangeEnd" type="success">{{ rangeEnd }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="仅日期范围（daterange）" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="日期范围">
            <StdTimeRange
              v-model:range-start="dateStart"
              v-model:range-end="dateEnd"
              type="daterange"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-tag v-if="dateStart" type="primary">{{ dateStart }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-tag v-if="dateEnd" type="success">{{ dateEnd }}</el-tag>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="自定义占位符和分隔符" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="时间范围">
            <StdTimeRange
              v-model:range-start="customStart"
              v-model:range-end="customEnd"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              range-separator="~"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="已选值">
            <template v-if="customStart && customEnd">
              <el-tag type="primary">{{ customStart }}</el-tag>
              <span style="margin: 0 8px">~</span>
              <el-tag type="success">{{ customEnd }}</el-tag>
            </template>
            <span v-else style="color: var(--el-text-color-placeholder)">未选择</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="禁用状态" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="全部禁用">
            <StdTimeRange
              v-model:range-start="disabledStart"
              v-model:range-end="disabledEnd"
              :disableds="[true, true]"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="仅禁用开始">
            <StdTimeRange
              v-model:range-start="disabledStart2"
              v-model:range-end="disabledEnd2"
              :disableds="[true, false]"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="禁用未来日期">
        <el-form label-width="120px">
          <el-form-item label="日期范围">
            <StdTimeRange
              v-model:range-start="noFutureStart"
              v-model:range-end="noFutureEnd"
              type="daterange"
              value-format="YYYY-MM-DD"
              :disable-start-date="noFutureDate"
              :disable-end-date="noFutureDate"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="说明">
            <span style="color: var(--el-text-color-secondary); font-size: 13px">通过 disableStartDate / disableEndDate 禁用今天之后的日期</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="280" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="160" />
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
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdTimeRange from "@/soa/std-series/StdTimeRange.vue";

const rangeStart = ref("");
const rangeEnd = ref("");

const dateStart = ref("");
const dateEnd = ref("");

const customStart = ref("");
const customEnd = ref("");

const disabledStart = ref("2026-01-01 00:00:00");
const disabledEnd = ref("2026-06-30 23:59:59");
const disabledStart2 = ref("");
const disabledEnd2 = ref("");

const noFutureStart = ref("");
const noFutureEnd = ref("");

const noFutureDate = (date: Date): boolean => {
  return date.getTime() > Date.now();
};

const propsTableData = [
  { name: "type", type: "\"daterange\" | \"datetimerange\"", required: "否", default: "\"datetimerange\"", desc: "日期范围类型" },
  { name: "startPlaceholder", type: "string", required: "否", default: "\"开始日期\"", desc: "开始日期占位符" },
  { name: "endPlaceholder", type: "string", required: "否", default: "\"结束日期\"", desc: "结束日期占位符" },
  { name: "valueFormat", type: "string", required: "否", default: "\"YYYY-MM-DD HH:mm:ss\"", desc: "日期格式" },
  { name: "clearable", type: "boolean", required: "否", default: "true", desc: "是否可清除（仅结束日期）" },
  { name: "rangeSeparator", type: "string", required: "否", default: "\"至\"", desc: "范围分隔符" },
  { name: "disableds", type: "boolean[]", required: "否", default: "[false, false]", desc: "是否禁用 [开始, 结束]" },
  { name: "showMsg", type: "boolean", required: "否", default: "true", desc: "是否显示验证消息" },
  { name: "width", type: "string", required: "否", default: "\"100%\"", desc: "日期选择器宽度" },
  { name: "isFocus", type: "boolean", required: "否", default: "true", desc: "打开面板时是否自动聚焦" },
  { name: "disableStartDate", type: "(date: Date) => boolean", required: "否", default: "undefined", desc: "开始日期禁用逻辑" },
  { name: "disableEndDate", type: "(date: Date) => boolean", required: "否", default: "undefined", desc: "结束日期禁用逻辑" },
];

const emitsTableData: { name: string; payload: string; desc: string }[] = [];

const vModelTableData = [
  { name: "v-model:range-start", type: "string", desc: "开始日期值" },
  { name: "v-model:range-end", type: "string", desc: "结束日期值" },
];
</script>
