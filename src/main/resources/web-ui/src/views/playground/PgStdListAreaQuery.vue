<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <StdListAreaQuery>
          <el-form :inline="true">
            <el-form-item label="名称">
              <el-input placeholder="请输入名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select placeholder="请选择状态" clearable>
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary">查询</el-button>
              <el-button>重置</el-button>
            </el-form-item>
          </el-form>
        </StdListAreaQuery>
      </el-card>

      <el-card header="持久化提示 + 教程" style="margin-bottom: 20px">
        <StdListAreaQuery show-persist-tip has-tutorial>
          <template #tutorial>
            <el-alert type="info" :closable="false" title="这是教程内容区域，通过 hasTutorial 控制显示隐藏。点击右上角问号图标切换。" />
          </template>
          <el-form :inline="true">
            <el-form-item label="编号">
              <el-input placeholder="请输入编号" clearable />
            </el-form-item>
            <el-form-item label="类型">
              <el-select placeholder="请选择类型" clearable>
                <el-option label="类型A" :value="'a'" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary">查询</el-button>
            </el-form-item>
          </el-form>
        </StdListAreaQuery>
      </el-card>

      <el-card header="仅持久化提示">
        <StdListAreaQuery show-persist-tip>
          <el-form :inline="true">
            <el-form-item label="关键字">
              <el-input placeholder="请输入关键字" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">查询</el-button>
            </el-form-item>
          </el-form>
        </StdListAreaQuery>
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
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";

const propsTableData = [
  { name: "showPersistTip", type: "boolean", required: "否", default: "false", desc: "是否显示持久化查询条件提示标识" },
  { name: "hasTutorial", type: "boolean", required: "否", default: "false", desc: "是否显示教程入口（问号图标），点击可切换教程内容显隐" },
];

const emitsTableData: { name: string; payload: string; desc: string }[] = [];

const slotsTableData = [
  { name: "default", params: "—", desc: "查询表单内容区域" },
  { name: "tutorial", params: "—", desc: "教程内容区域，仅 hasTutorial 为 true 且展开时显示" },
];
</script>
