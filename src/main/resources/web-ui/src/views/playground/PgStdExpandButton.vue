<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="展开/收起">
            <StdExpandButton v-model="expanded" />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-tag :type="expanded ? 'success' : 'info'">{{ expanded ? "已展开" : "已收起" }}</el-tag>
          </el-form-item>
          <el-form-item label="模拟内容">
            <div v-if="expanded" style="background: var(--el-fill-color-light); padding: 16px; border-radius: 4px">
              这是展开后显示的内容区域
            </div>
            <span v-else style="color: var(--el-text-color-placeholder)">点击"展开"按钮查看内容</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="禁用状态" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="展开/收起">
            <StdExpandButton v-model="disabledExpanded" disabled />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-tag type="info">{{ disabledExpanded ? "已展开" : "已收起" }}</el-tag>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="预设展开状态">
        <el-form label-width="100px">
          <el-form-item label="展开/收起">
            <StdExpandButton v-model="presetExpanded" />
          </el-form-item>
          <el-form-item label="模拟内容">
            <div v-if="presetExpanded" style="background: var(--el-fill-color-light); padding: 16px; border-radius: 4px">
              默认展开，初始就能看到这段内容。组件会从 QueryPersistService 读取持久化状态。
            </div>
          </el-form-item>
        </el-form>
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
import StdExpandButton from "@/soa/std-series/StdExpandButton.vue";

const expanded = ref(false);
const disabledExpanded = ref(false);
const presetExpanded = ref(true);

const propsTableData = [
  { name: "modelValue", type: "boolean", required: "是", default: "—", desc: "展开/收起状态，true 为展开" },
  { name: "disabled", type: "boolean", required: "否", default: "false", desc: "禁用按钮" },
];

const emitsTableData = [
  { name: "update:modelValue", payload: "val: boolean", desc: "展开/收起状态变化时触发" },
];

const vModelTableData = [
  { name: "v-model（默认）", type: "boolean", desc: "双向绑定展开/收起状态" },
];
</script>
