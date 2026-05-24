<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="默认（有内边距）" style="margin-bottom: 20px">
        <div style="height: 200px; border: 1px dashed var(--el-border-color)">
          <StdListContainer>
            <div style="background: var(--el-color-primary-light-9); padding: 12px; border-radius: 4px">
              内容区域 — 默认有 20px 内边距
            </div>
            <div style="background: var(--el-color-success-light-9); padding: 12px; margin-top: 8px; border-radius: 4px; flex: 1">
              列表项示例
            </div>
          </StdListContainer>
        </div>
      </el-card>

      <el-card header="nopadding（无内边距）" style="margin-bottom: 20px">
        <div style="height: 200px; border: 1px dashed var(--el-border-color)">
          <StdListContainer nopadding>
            <div style="background: var(--el-color-primary-light-9); padding: 12px; border-radius: 4px">
              内容区域 — 无默认内边距，内容紧贴边缘
            </div>
            <div style="background: var(--el-color-success-light-9); padding: 12px; margin-top: 8px; border-radius: 4px; flex: 1">
              列表项示例
            </div>
          </StdListContainer>
        </div>
      </el-card>

      <el-card header="对比展示">
        <div style="display: flex; gap: 20px">
          <div style="flex: 1">
            <div style="margin-bottom: 8px; font-weight: 500">默认 padding: 20px</div>
            <div style="height: 160px; border: 1px dashed var(--el-border-color)">
              <StdListContainer>
                <div style="background: var(--el-color-primary-light-7); height: 100%; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff">
                  有内边距
                </div>
              </StdListContainer>
            </div>
          </div>
          <div style="flex: 1">
            <div style="margin-bottom: 8px; font-weight: 500">nopadding</div>
            <div style="height: 160px; border: 1px dashed var(--el-border-color)">
              <StdListContainer nopadding>
                <div style="background: var(--el-color-success-light-7); height: 100%; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff">
                  无内边距
                </div>
              </StdListContainer>
            </div>
          </div>
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
import StdListContainer from "@/soa/std-series/StdListContainer.vue";

const propsTableData = [
  { name: "nopadding", type: "boolean", required: "否", default: "false", desc: "为 true 时移除默认的 20px 内边距" },
];

const emitsTableData: { name: string; payload: string; desc: string }[] = [];

const slotsTableData = [
  { name: "default", params: "—", desc: "容器内容，自动继承 flex column 布局" },
];
</script>
