<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="多选模式（multiple，跨页）" style="margin-bottom: 20px">
        <div style="height: 500px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="multiForm" :list-total="TOTAL" :load-list="loadMultiPage">
            <el-table :data="multiData" stripe border style="width: 100%" @row-click="multiColRef?.onElRowCheck">
              <StdTableCheckColumn ref="multiColRef" v-model="multiCheckedIds" v-model:checked-rows="multiCheckedRows" :data="multiData" row-key="id" mode="multiple" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>
          </StdListAreaTable>
        </div>
        <div style="margin-top: 12px">
          <el-button size="small" type="danger" @click="multiColRef?.clearCheck()">clearCheck()</el-button>
          <span style="margin-left: 12px; font-size: 13px; color: var(--el-text-color-secondary)">
            已选 ID：{{ multiCheckedIds.join(', ') || '无' }}
          </span>
          <span style="margin-left: 8px; font-size: 13px; color: var(--el-text-color-secondary)">
            已选 VO：{{ multiCheckedRows.map((r: MockRow) => r.name).join(', ') || '无' }}
          </span>
        </div>
      </el-card>

      <el-card header="多选不跨页模式（multiple-in-page）" style="margin-bottom: 20px">
        <div style="height: 500px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="inPageForm" :list-total="TOTAL" :load-list="loadInPagePage">
            <el-table :data="inPageData" stripe border style="width: 100%" @row-click="inPageColRef?.onElRowCheck">
              <StdTableCheckColumn ref="inPageColRef" v-model="inPageCheckedIds" v-model:checked-rows="inPageCheckedRows" :data="inPageData" row-key="id" mode="multiple-in-page" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>
          </StdListAreaTable>
        </div>
        <div style="margin-top: 12px">
          <el-button size="small" type="danger" @click="inPageColRef?.clearCheck()">clearCheck()</el-button>
          <span style="margin-left: 12px; font-size: 13px; color: var(--el-text-color-secondary)">
            已选 ID：{{ inPageCheckedIds.join(', ') || '无' }}
          </span>
        </div>
      </el-card>

      <el-card header="单选模式（single）" style="margin-bottom: 20px">
        <div style="height: 500px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="singleForm" :list-total="TOTAL" :load-list="loadSinglePage">
            <el-table :data="singleData" stripe border style="width: 100%" highlight-current-row @row-click="singleColRef?.onElRowCheck">
              <StdTableCheckColumn ref="singleColRef" v-model="singleCheckedIds" v-model:checked-rows="singleCheckedRows" :data="singleData" row-key="id" mode="single" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>
          </StdListAreaTable>
        </div>
        <div style="margin-top: 12px">
          <el-button size="small" type="danger" @click="singleColRef?.clearCheck()">clearCheck()</el-button>
          <span style="margin-left: 12px; font-size: 13px; color: var(--el-text-color-secondary)">
            已选 ID：{{ singleCheckedIds.join(', ') || '无' }}
          </span>
        </div>
      </el-card>

      <el-card header="只读模式 + 自定义 rowKey" style="margin-bottom: 20px">
        <el-table :data="readonlyData" stripe border style="width: 100%">
          <StdTableCheckColumn v-model="readonlyCheckedIds" :data="readonlyData" :row-key="getRowKey" mode="multiple" readonly />
          <el-table-column prop="code" label="编码" width="120" />
          <el-table-column prop="name" label="名称" width="200" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="createTime" label="创建时间" />
        </el-table>
        <div style="margin-top: 12px">
          <span style="font-size: 13px; color: var(--el-text-color-secondary)">
            rowKey 为函数 (row) => row.code，预设勾选 R002、R004
          </span>
        </div>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="280" />
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

      <el-divider content-position="left">expose 方法</el-divider>
      <el-table :data="exposeTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="方法名" width="220" />
        <el-table-column prop="params" label="参数" width="280" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import StdTableCheckColumn from "@/soa/std-series/StdTableCheckColumn.vue";
import type PageQuery from "@/commons/model/PageQuery";

interface MockRow {
  id: number;
  name: string;
  status: string;
  createTime: string;
}

interface MockRowWithCode extends MockRow {
  code: string;
}

const TOTAL = 56;
const mockSource: MockRow[] = Array.from({ length: TOTAL }, (_, i) => ({
  id: i + 1,
  name: `记录 #${i + 1}`,
  status: i % 3 === 0 ? "已完成" : i % 3 === 1 ? "进行中" : "待处理",
  createTime: `2026-05-${String((i % 20) + 1).padStart(2, "0")} 10:00:00`,
}));

// ========== 多选模式 ==========
const multiForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });
const multiData = ref<MockRow[]>([]);
const multiCheckedIds = ref<string[]>([]);
const multiCheckedRows = ref<MockRow[]>([]);
const multiColRef = ref<InstanceType<typeof StdTableCheckColumn>>();

const loadMultiPage = (): void => {
  const start = (multiForm.pageNum - 1) * multiForm.pageSize;
  multiData.value = mockSource.slice(start, start + multiForm.pageSize);
};
loadMultiPage();

// ========== 多选不跨页模式 ==========
const inPageForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });
const inPageData = ref<MockRow[]>([]);
const inPageCheckedIds = ref<string[]>([]);
const inPageCheckedRows = ref<MockRow[]>([]);
const inPageColRef = ref<InstanceType<typeof StdTableCheckColumn>>();

const loadInPagePage = (): void => {
  const start = (inPageForm.pageNum - 1) * inPageForm.pageSize;
  inPageData.value = mockSource.slice(start, start + inPageForm.pageSize);
};
loadInPagePage();

// ========== 单选模式 ==========
const singleForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });
const singleData = ref<MockRow[]>([]);
const singleCheckedIds = ref<string[]>([]);
const singleCheckedRows = ref<MockRow[]>([]);
const singleColRef = ref<InstanceType<typeof StdTableCheckColumn>>();

const loadSinglePage = (): void => {
  const start = (singleForm.pageNum - 1) * singleForm.pageSize;
  singleData.value = mockSource.slice(start, start + singleForm.pageSize);
};
loadSinglePage();

// ========== 只读模式 + 自定义 rowKey ==========
const readonlyData: MockRowWithCode[] = [
  { code: "R001", id: 1, name: "记录 #1", status: "已完成", createTime: "2026-05-01 10:00:00" },
  { code: "R002", id: 2, name: "记录 #2", status: "进行中", createTime: "2026-05-02 10:00:00" },
  { code: "R003", id: 3, name: "记录 #3", status: "待处理", createTime: "2026-05-03 10:00:00" },
  { code: "R004", id: 4, name: "记录 #4", status: "已完成", createTime: "2026-05-04 10:00:00" },
];
const getRowKey = (row: MockRowWithCode): string => row.code;
const readonlyCheckedIds = ref<string[]>(["R002", "R004"]);

const propsTableData = [
  { name: "data", type: "any[]", required: "否", default: "[]", desc: "当前页数据，用于表头全选判断" },
  { name: "rowKey", type: "string | (row) => string | number", required: "否", default: '"id"', desc: "行唯一标识字段名或取值函数" },
  { name: "mode", type: '"single" | "multiple" | "multiple-in-page"', required: "否", default: '"multiple"', desc: "选择模式：单选 / 多选跨页 / 多选不跨页" },
  { name: "width", type: "string | number", required: "否", default: "55", desc: "列宽" },
  { name: "align", type: "string", required: "否", default: '"center"', desc: "列对齐方式" },
  { name: "fixed", type: "string | boolean", required: "否", default: "false", desc: "列固定位置" },
  { name: "readonly", type: "boolean", required: "否", default: "false", desc: "只读模式，禁止勾选" },
];

const emitsTableData = [
  { name: "（无自定义事件）", payload: "—", desc: "通过 v-model 和 expose 与外部交互" },
];

const vModelTableData = [
  { name: "v-model（默认）", type: "string[]", desc: "双向绑定已勾选行 ID 数组，必填" },
  { name: "v-model:checked-rows", type: "any[]", desc: "双向绑定已勾选行完整数据数组" },
];

const exposeTableData = [
  { name: "onElRowCheck(row)", params: "row: any", desc: "触发行勾选切换，配合 el-table @row-click 使用" },
  { name: "clearCheck()", params: "—", desc: "清空所有勾选" },
];
</script>
