<template>
  <StdPgLayout>
    <div style="padding: 24px; height: 100%; box-sizing: border-box">
      <el-card header="基础能力演示" style="height: calc(100% - 20px); display: flex; flex-direction: column">
        <template #header>
          <div style="display: flex; align-items: center; gap: 12px">
            <span>基础能力演示</span>
            <el-input v-model="inputUrl" placeholder="输入 URL 后回车" style="width: 360px" size="small" clearable @keyup.enter="applyUrl" />
            <el-button type="primary" size="small" @click="applyUrl">加载</el-button>
            <el-tooltip content="通过 route.query.url 传入也可展示" placement="top">
              <el-button size="small" text type="primary" @click="useRouteQueryDemo">模拟 query 参数</el-button>
            </el-tooltip>
          </div>
        </template>

        <div style="margin-bottom: 8px; display: flex; gap: 8px; flex-wrap: wrap">
          <template v-for="u in presetUrls" :key="u.label">
            <el-tag
              :type="currentUrl === u.url ? 'primary' : 'info'"
              style="cursor: pointer"
              disable-transitions
              @click="currentUrl = u.url"
            >{{ u.label }}</el-tag>
          </template>
          <el-button v-if="currentUrl" size="small" text type="danger" @click="currentUrl = ''">清空</el-button>
        </div>

        <div style="flex: 1; border: 1px solid var(--el-border-color); border-radius: 4px; overflow: hidden; min-height: 400px">
          <StdIframe :url="currentUrl" />
        </div>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="250" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="150" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>

    <template #emits>
      <el-table :data="emitsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="事件名" width="220" />
        <el-table-column prop="payload" label="参数" width="250" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdIframe from "@/soa/std-series/StdIframe.vue";

const inputUrl = ref("");
const currentUrl = ref("");

const applyUrl = (): void => {
  currentUrl.value = inputUrl.value;
};

const useRouteQueryDemo = (): void => {
  currentUrl.value = "https://www.baidu.com";
};

const presetUrls = [
  { label: "百度", url: "https://www.baidu.com" },
  { label: "必应", url: "https://www.bing.com" },
  { label: "example.com", url: "https://example.com" },
];

const propsTableData = [
  { name: "url", type: "string", required: "否", default: "route.query.url", desc: "iframe 加载地址，不传则自动从路由 query 参数 url 读取" },
];

const emitsTableData = [
  { name: "—", payload: "—", desc: "无自定义事件" },
];
</script>
