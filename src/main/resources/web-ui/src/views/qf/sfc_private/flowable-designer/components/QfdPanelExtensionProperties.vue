<template>
  <div class="qfd-extension-properties">
    <div class="qfd-section-head">
      <span>自定义属性（flowable:property）</span>
      <el-button type="primary" link size="small" @click="openDialog()">+ 添加属性</el-button>
    </div>
    <el-table :data="rows" size="small" border stripe empty-text="暂无数据">
      <el-table-column type="index" label="序号" width="56" />
      <el-table-column prop="name" label="名称" min-width="100" show-overflow-tooltip />
      <el-table-column prop="value" label="值" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑扩展属性' : '新建扩展属性'" width="420px" destroy-on-close>
      <el-form label-width="72px" size="small">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="form.value" type="textarea" :autosize="{ minRows: 2 }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount } from "vue";
import QfdPanelExtensionPropertiesService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelExtensionPropertiesService";

const props = defineProps<{
  modeler: unknown;
  element: unknown;
}>();

const { rows, dialogVisible, editing, form, openDialog, save, removeRow, dispose } =
  QfdPanelExtensionPropertiesService.useQfdPanelExtensionProperties(
    () => props.modeler,
    () => props.element
  );

onBeforeUnmount(() => {
  dispose();
});
</script>

<style scoped>
.qfd-extension-properties {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.qfd-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-regular);
}
</style>
