<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="组件说明" style="margin-bottom: 20px">
        <p style="margin: 0; line-height: 1.8">
          <code>StdPgLayout</code> 是 Playground 演示页的统一外壳，提供「演示 / props / emits」三个 Tab。
          所有组件演示页必须使用此布局，禁止自行写 <code>el-tabs</code> 替代。
        </p>
      </el-card>

      <el-card header="插槽结构示意" style="margin-bottom: 20px">
        <el-form label-width="120px">
          <el-form-item label="默认插槽">
            <el-tag>「演示」Tab 内容</el-tag>
          </el-form-item>
          <el-form-item label="#props 插槽">
            <el-tag type="success">Props 文档表格</el-tag>
          </el-form-item>
          <el-form-item label="#emits 插槽">
            <el-tag type="warning">事件 / v-model / 插槽文档</el-tag>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="最小使用示例">
        <div style="background: var(--el-fill-color); padding: 16px; border-radius: 4px">
          <pre style="margin: 0; font-size: 13px; line-height: 1.6"><code>&lt;template&gt;
  &lt;StdPgLayout&gt;
    &lt;div&gt;演示区内容&lt;/div&gt;

    &lt;template #props&gt;
      &lt;el-table :data="propsTableData" /&gt;
    &lt;/template&gt;

    &lt;template #emits&gt;
      &lt;el-table :data="emitsTableData" /&gt;
    &lt;/template&gt;
  &lt;/StdPgLayout&gt;
&lt;/template&gt;</code></pre>
        </div>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
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

      <el-divider content-position="left">插槽</el-divider>
      <el-table :data="slotsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="插槽名" width="160" />
        <el-table-column prop="params" label="参数" width="200" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";

const propsTableData = [
  { name: "—", type: "—", required: "—", default: "—", desc: "无外部 Props，仅提供三个插槽" },
];

const emitsTableData: { name: string; payload: string; desc: string }[] = [];

const slotsTableData = [
  { name: "default", params: "—", desc: "「演示」Tab 内容区" },
  { name: "props", params: "—", desc: "「props」Tab，放 Props 文档表格" },
  { name: "emits", params: "—", desc: "「emits」Tab，放事件文档表格及 v-model/插槽/expose 扩展区" },
];
</script>
