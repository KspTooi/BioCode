<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础用法" style="margin-bottom: 20px">
        <div style="color: var(--el-text-color-secondary); margin-bottom: 16px">
          StdListAreaAction 为操作按钮区提供统一样式（下边距 15px），配合 StdListLayout 使用。
        </div>
        <StdListAreaAction>
          <el-button type="primary">新增</el-button>
          <el-button type="danger">批量删除</el-button>
          <el-button>导出</el-button>
        </StdListAreaAction>
        <el-alert type="info" :closable="false" style="margin-top: 12px">
          下方虚线框标注了组件实际占用的区域（红色虚线），可见组件仅提供 margin-bottom 间距，不改变内部布局。
        </el-alert>
      </el-card>

      <el-card header="StdListLayout 集成示意" style="margin-bottom: 20px">
        <div style="border: 2px dashed var(--el-color-primary); padding: 16px; border-radius: 4px">
          <div style="color: var(--el-text-color-secondary); font-size: 13px; margin-bottom: 8px">
            StdListAreaQuery
          </div>
          <div style="background: var(--el-fill-color-light); padding: 12px; border-radius: 4px; margin-bottom: 8px">
            <el-input placeholder="搜索关键词" style="width: 200px" />
          </div>

          <div style="color: var(--el-text-color-secondary); font-size: 13px; margin-bottom: 8px">
            StdListAreaAction
          </div>
          <StdListAreaAction>
            <el-button type="primary">新增</el-button>
            <el-button type="danger">批量删除</el-button>
          </StdListAreaAction>

          <div style="color: var(--el-text-color-secondary); font-size: 13px; margin-bottom: 8px">
            StdListAreaTable
          </div>
          <div style="background: var(--el-fill-color-light); padding: 12px; border-radius: 4px">
            <span style="color: var(--el-text-color-placeholder)">表格区域占位</span>
          </div>
        </div>
      </el-card>

      <el-card header="多组操作按钮">
        <StdListAreaAction>
          <el-button type="primary">新增</el-button>
          <el-button>编辑</el-button>
          <el-divider direction="vertical" />
          <el-button type="danger">删除</el-button>
        </StdListAreaAction>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
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
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";

const propsTableData = [
  { name: "—", type: "—", required: "—", default: "—", desc: "无 Props，仅提供默认插槽" },
];

const emitsTableData = [
  { name: "—", payload: "—", desc: "无自定义事件" },
];

const slotsTableData = [
  { name: "default", params: "—", desc: "操作按钮区内容，通常放置 el-button 等" },
];
</script>
