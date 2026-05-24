<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础用法（limit=3，超过折叠）" style="margin-bottom: 20px">
        <div style="color: var(--el-text-color-secondary); margin-bottom: 16px">
          StdQueryCollapse 用于查询表单区域，超出 limit 的项默认折叠，点击"展开"显示全部。
        </div>
        <el-form inline>
          <StdQueryCollapse>
            <div>
              <el-form-item label="关键词">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select placeholder="请选择" style="width: 160px">
                  <el-option label="全部" value="" />
                  <el-option label="启用" value="1" />
                  <el-option label="禁用" value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="创建人">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
              <el-form-item label="开始时间">
                <el-date-picker type="date" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker type="date" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
            </div>
            <template #actions>
              <el-button type="primary">查询</el-button>
              <el-button>重置</el-button>
            </template>
          </StdQueryCollapse>
        </el-form>
      </el-card>

      <el-card header="少量查询项（不触发折叠）" style="margin-bottom: 20px">
        <div style="color: var(--el-text-color-secondary); margin-bottom: 16px">
          当子节点数 ≤ limit 时，不显示展开/收起按钮。
        </div>
        <el-form inline>
          <StdQueryCollapse :limit="5">
            <div>
              <el-form-item label="关键词">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select placeholder="请选择" style="width: 160px">
                  <el-option label="全部" value="" />
                  <el-option label="启用" value="1" />
                </el-select>
              </el-form-item>
            </div>
            <template #actions>
              <el-button type="primary">查询</el-button>
              <el-button>重置</el-button>
            </template>
          </StdQueryCollapse>
        </el-form>
      </el-card>

      <el-card header="自定义 limit 与仅 actions 插槽" style="margin-bottom: 20px">
        <div style="color: var(--el-text-color-secondary); margin-bottom: 16px">
          通过 limit 控制折叠阈值（当前 limit=2），actions 插槽放置操作按钮。
        </div>
        <el-form inline>
          <StdQueryCollapse :limit="2">
            <div>
              <el-form-item label="名称">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
              <el-form-item label="类型">
                <el-select placeholder="请选择" style="width: 160px">
                  <el-option label="A类" value="a" />
                  <el-option label="B类" value="b" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期">
                <el-date-picker type="date" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="负责人">
                <el-input placeholder="请输入" clearable />
              </el-form-item>
            </div>
            <template #actions>
              <el-button type="primary">查询</el-button>
              <el-button>重置</el-button>
              <el-button>导出</el-button>
            </template>
          </StdQueryCollapse>
        </el-form>
      </el-card>

      <el-card header="展开状态持久化">
        <el-alert type="info" :closable="false">
          useSearchExpand 基于当前路由 path 将展开/收起状态写入 sessionStorage，路由不变时刷新页面会恢复上次的展开状态。
        </el-alert>
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
import StdQueryCollapse from "@/soa/std-series/StdQueryCollapse.vue";

const propsTableData = [
  { name: "limit", type: "number", required: "否", default: "3", desc: "默认显示的子节点数量，超出则折叠" },
];

const emitsTableData = [{ name: "—", payload: "—", desc: "无自定义事件" }];

const slotsTableData = [
  { name: "default", params: "—", desc: "查询表单内容，每项作为一个子节点参与折叠计数" },
  { name: "actions", params: "—", desc: '右侧操作按钮区，通常放"查询"、"重置"等按钮' },
];
</script>
