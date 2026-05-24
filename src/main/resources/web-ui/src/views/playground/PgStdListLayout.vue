<template>
  <StdPgLayout>
    <!-- ======================== 演示 Tab ======================== -->
    <el-scrollbar style="height: 100%">
      <StdListLayout
        :show-persist-tip="showPersistTip"
        :has-tutorial="hasTutorial"
      >
        <!-- 查询区 -->
        <template #query>
          <el-form :inline="true" :model="mockQuery" size="default">
            <el-form-item label="关键词">
              <el-input v-model="mockQuery.keyword" placeholder="名称/编号" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="mockQuery.status" placeholder="全部" clearable style="width: 140px">
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker
                v-model="mockQuery.dateRange"
                type="daterange"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM-DD"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 说明文档 -->
        <template #tutorial>
          <el-alert
            title="列表页布局使用说明"
            type="info"
            :closable="false"
            show-icon
          >
            <ul style="margin: 4px 0; padding-left: 20px; font-size: 13px">
              <li>查询区域支持持久化提示（showPersistTip）和说明文档指示器（hasTutorial）</li>
              <li>操作按钮区位于查询区下方，通常放置新增、导出等操作按钮</li>
              <li>表格区域自动撑满剩余高度，分页器固定在底部右侧</li>
              <li>modal 插槽用于放置弹窗等不可见组件，不会产生额外 DOM 包裹</li>
            </ul>
          </el-alert>
        </template>

        <!-- 操作按钮 -->
        <template #actions>
          <el-button type="primary" @click="onAdd">新增</el-button>
          <el-button @click="onExport">导出</el-button>
          <el-button type="danger" text @click="handleBatchDelete">批量删除</el-button>
        </template>

        <!-- 表格 -->
        <template #table>
          <el-table :data="mockTableData" stripe border style="width: 100%" @selection-change="onSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creator" label="创建人" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default>
                <el-button type="primary" link size="small">编辑</el-button>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <!-- 分页 -->
        <template #pagination>
          <el-pagination
            v-model:current-page="mockPagination.pageNum"
            v-model:page-size="mockPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="mockTotal"
            background
          />
        </template>

        <!-- 模态框 -->
        <template #modal>
          <el-dialog v-model="addDialogVisible" title="新增记录" width="500px" :close-on-click-modal="false">
            <el-form :model="addForm" label-width="80px">
              <el-form-item label="名称">
                <el-input v-model="addForm.name" placeholder="请输入名称" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="addForm.status" style="width: 100%">
                  <el-option label="启用" value="启用" />
                  <el-option label="禁用" value="禁用" />
                </el-select>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="addDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="addDialogVisible = false">确定</el-button>
            </template>
          </el-dialog>
        </template>
      </StdListLayout>
    </el-scrollbar>

    <!-- ======================== Props Tab ======================== -->
    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="120" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>

    <!-- ======================== Emits Tab ======================== -->
    <template #emits>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        StdListLayout 为纯布局组件，不对外暴露事件，仅负责插槽分发。各子区域事件由使用者自行在插槽内容中绑定。
      </el-alert>

      <el-divider content-position="left">插槽</el-divider>
      <el-table :data="slotsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="插槽名" width="160" />
        <el-table-column prop="params" label="参数" width="160" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">演示区控件</el-divider>
      <el-form label-width="140px" style="padding: 0 12px">
        <el-form-item label="showPersistTip">
          <el-switch v-model="showPersistTip" />
          <span style="margin-left: 8px; font-size: 13px; color: var(--el-text-color-secondary)">显示查询持久化指示器</span>
        </el-form-item>
        <el-form-item label="hasTutorial">
          <el-switch v-model="hasTutorial" />
          <span style="margin-left: 8px; font-size: 13px; color: var(--el-text-color-secondary)">显示说明文档入口</span>
        </el-form-item>
      </el-form>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";

const showPersistTip = ref(false);
const hasTutorial = ref(false);

const mockQuery = reactive({
  keyword: "",
  status: "",
  dateRange: null as [string, string] | null,
});

const mockPagination = reactive({
  pageNum: 1,
  pageSize: 10,
});

const mockTotal = 3;

const mockTableData = [
  { id: 1, name: "数据项 Alpha", status: "启用", creator: "张三", createTime: "2026-05-01 10:30:00" },
  { id: 2, name: "数据项 Beta", status: "启用", creator: "李四", createTime: "2026-05-02 14:20:00" },
  { id: 3, name: "数据项 Gamma", status: "禁用", creator: "王五", createTime: "2026-05-03 09:15:00" },
];

const selectedRows = ref<any[]>([]);
const addDialogVisible = ref(false);
const addForm = reactive({ name: "", status: "启用" });

const handleQuery = (): void => {
  // 模拟查询
};

const handleReset = (): void => {
  mockQuery.keyword = "";
  mockQuery.status = "";
  mockQuery.dateRange = null;
};

const onAdd = (): void => {
  addForm.name = "";
  addForm.status = "启用";
  addDialogVisible.value = true;
};

const onExport = (): void => {
  // 模拟导出
};

const onSelectionChange = (rows: any[]): void => {
  selectedRows.value = rows;
};

const handleBatchDelete = (): void => {
  if (selectedRows.value.length === 0) {
    return;
  }
  selectedRows.value = [];
};

const propsTableData = [
  { name: "showPersistTip", type: "boolean", required: "否", default: "false", desc: "查询区显示持久化指示器，提示用户查询条件会被保留" },
  { name: "hasTutorial", type: "boolean", required: "否", default: "false", desc: "查询区显示说明文档指示器，可展开 tutorial 插槽内容" },
];

const slotsTableData = [
  { name: "query", params: "—", desc: "查询区内容，被 StdListAreaQuery 包裹" },
  { name: "tutorial", params: "—", desc: "说明文档内容，点击问号图标后展开，需配合 hasTutorial" },
  { name: "actions", params: "—", desc: "操作按钮区，被 StdListAreaAction 包裹" },
  { name: "table", params: "—", desc: "表格区，被 StdListAreaTable 包裹" },
  { name: "pagination", params: "—", desc: "分页区，嵌套在表格区底部（插槽 props：listForm、listTotal、loadList）" },
  { name: "modal", params: "—", desc: "模态框等不可见组件区，不被任何容器包裹" },
];
</script>
