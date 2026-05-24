<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示（模拟分页请求）" style="margin-bottom: 20px">
        <div style="height: 480px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="fetchList">
            <el-table :data="tableData" stripe border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>
          </StdListAreaTable>
        </div>
      </el-card>

      <el-card header="自定义分页插槽" style="margin-bottom: 20px">
        <div style="height: 480px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="customForm" :list-total="listTotal" :load-list="fetchList">
            <el-table :data="tableData" stripe border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>

            <template #pagination>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-size: 13px; color: var(--el-text-color-secondary)">自定义插槽 — 共 {{ listTotal }} 条</span>
                <el-pagination
                  v-model:current-page="customForm.pageNum"
                  v-model:page-size="customForm.pageSize"
                  :page-sizes="[15, 30, 60]"
                  layout="prev, pager, next"
                  :total="listTotal"
                  background
                  @size-change="fetchList"
                  @current-change="fetchList"
                />
              </div>
            </template>
          </StdListAreaTable>
        </div>
      </el-card>

      <el-card header="空数据状态">
        <div style="height: 320px; display: flex; flex-direction: column">
          <StdListAreaTable v-model:list-form="emptyForm" :list-total="0" :load-list="() => {}">
            <el-table :data="[]" stripe border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column prop="status" label="状态" width="120" />
              <el-table-column prop="createTime" label="创建时间" />
              <template #empty>
                <span style="color: var(--el-text-color-placeholder)">暂无数据</span>
              </template>
            </el-table>
          </StdListAreaTable>
        </div>
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

      <el-divider content-position="left">插槽</el-divider>
      <el-table :data="slotsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="插槽名" width="160" />
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
import type PageQuery from "@/commons/model/PageQuery";

interface MockRow {
  id: number;
  name: string;
  status: string;
  createTime: string;
}

const TOTAL = 86;
const mockSource: MockRow[] = Array.from({ length: TOTAL }, (_, i) => ({
  id: i + 1,
  name: `记录 #${i + 1}`,
  status: i % 3 === 0 ? "已完成" : i % 3 === 1 ? "进行中" : "待处理",
  createTime: `2026-05-${String((i % 20) + 1).padStart(2, "0")} 10:00:00`,
}));

const listForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });
const listTotal = ref(TOTAL);
const tableData = ref<MockRow[]>([]);

const fetchList = (): void => {
  const start = (listForm.pageNum - 1) * listForm.pageSize;
  tableData.value = mockSource.slice(start, start + listForm.pageSize);
};

const customForm = reactive<PageQuery>({ pageNum: 1, pageSize: 15 });

const emptyForm = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });

fetchList();

const propsTableData = [
  { name: "listTotal", type: "number", required: "否", default: "—", desc: "数据总条数，用于分页组件 total 显示" },
  { name: "loadList", type: "() => any", required: "否", default: "—", desc: "分页 size-change / current-change 时触发的回调" },
];

const emitsTableData = [
  { name: "（由 el-pagination 内置处理）", payload: "—", desc: "无自定义事件，分页变化直接调用 loadList" },
];

const vModelTableData = [
  { name: "v-model:list-form", type: "PageQuery", desc: "双向绑定分页参数 { pageNum, pageSize }，非必填" },
];

const slotsTableData = [
  { name: "default", params: "—", desc: "表格主体区域（el-table 等）" },
  { name: "pagination", params: "—", desc: "覆盖默认分页组件，可自定义分页布局与样式" },
];
</script>
