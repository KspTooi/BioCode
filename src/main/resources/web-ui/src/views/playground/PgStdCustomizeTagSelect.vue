<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择标签">
            <StdCustomizeTagSelect v-model="selectedTags" :tags="mockTags" placeholder="请选择自定义标签" style="width: 400px" />
          </el-form-item>
          <el-form-item label="已选CTJ">
            <el-tag v-for="t in selectedTags" :key="t.n" style="margin-right: 4px">{{ t.n }}</el-tag>
            <span v-if="selectedTags.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
          <el-form-item label="CTJ JSON">
            <code style="background: var(--el-fill-color-light); padding: 4px 8px; border-radius: 4px; font-size: 12px">{{ JSON.stringify(selectedTags) }}</code>
          </el-form-item>
          <el-form-item>
            <el-button size="small" type="danger" @click="selectedTags = []">清除已选</el-button>
            <el-button size="small" type="primary" @click="selectedTags = [{ n: '紧急' }, { n: '待审核' }]">预置「紧急」+「待审核」</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="透传 el-select 属性" style="margin-bottom: 20px">
        <el-checkbox v-model="clearable" style="margin-right: 16px">可清空</el-checkbox>
        <el-checkbox v-model="disabled" style="margin-right: 16px">禁用</el-checkbox>
        <el-checkbox v-model="collapseTags" style="margin-right: 16px">折叠标签</el-checkbox>

        <el-form label-width="100px" style="margin-top: 16px">
          <el-form-item label="选择标签">
            <StdCustomizeTagSelect
              v-model="advancedTags"
              :tags="mockTags"
              style="width: 400px"
              :clearable="clearable"
              :disabled="disabled"
              :collapse-tags="collapseTags"
              placeholder="透传 el-select 属性"
            />
          </el-form-item>
          <el-form-item label="已选CTJ">
            <el-tag v-for="t in advancedTags" :key="t.n" style="margin-right: 4px">{{ t.n }}</el-tag>
            <span v-if="advancedTags.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="空数据源" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择标签">
            <StdCustomizeTagSelect v-model="emptyTags" :tags="[]" style="width: 400px" placeholder="无可选标签" />
          </el-form-item>
          <el-form-item label="已选CTJ">
            <el-tag v-for="t in emptyTags" :key="t.n" style="margin-right: 4px">{{ t.n }}</el-tag>
            <span v-if="emptyTags.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
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
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import StdCustomizeTagSelect from "@/soa/std-series/StdCustomizeTagSelect.vue";
import type CustomizeTagJson from "@/commons/model/json/CustomizeTagJson";

const mockTags: CustomizeTagJson[] = [
  { n: "紧急" },
  { n: "重要" },
  { n: "普通" },
  { n: "低优先级" },
  { n: "待审核" },
  { n: "已审批" },
  { n: "需关注" },
  { n: "长期跟进" },
];

const selectedTags = ref<CustomizeTagJson[]>([]);
const advancedTags = ref<CustomizeTagJson[]>([]);
const emptyTags = ref<CustomizeTagJson[]>([]);

const clearable = ref(false);
const disabled = ref(false);
const collapseTags = ref(false);

const propsTableData = [
  { name: "tags", type: "CustomizeTagJson[]", required: "是", default: "—", desc: "标签数据源，CustomizeTagJson = { n: string }" },
  { name: "...$attrs", type: "el-select props", required: "否", default: "—", desc: "透传所有 el-select 属性（multiple 已内置）" },
];

const emitsTableData = [
  { name: "（原生事件透传）", payload: "—", desc: "无自定义事件，el-select 原生事件通过 v-bind=\"$attrs\" 透传" },
];

const vModelTableData = [
  { name: "v-model（默认）", type: "CustomizeTagJson[]", desc: "双向绑定已选标签列表，内部自动与 el-select 的 string[] 互转" },
];
</script>
