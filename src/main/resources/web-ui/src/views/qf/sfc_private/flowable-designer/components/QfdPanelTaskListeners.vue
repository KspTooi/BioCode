<template>
  <div class="qfd-task-listeners">
    <div class="qfd-section-head">
      <span>{{ heading }}</span>
      <el-button type="primary" link size="small" @click="openDialog()">+ 添加监听器</el-button>
    </div>
    <el-table :data="rows" size="small" border stripe empty-text="暂无数据">
      <el-table-column type="index" label="序号" width="56" />
      <el-table-column prop="event" label="事件" width="100" />
      <el-table-column prop="implType" label="类型" width="100" />
      <el-table-column prop="implValue" label="实现" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑任务监听器' : '新建任务监听器'" width="480px" destroy-on-close>
      <el-form label-width="100px" size="small">
        <el-form-item label="事件类型">
          <el-select v-model="form.event" style="width: 100%">
            <el-option label="create" value="create" />
            <el-option label="assignment" value="assignment" />
            <el-option label="complete" value="complete" />
            <el-option label="delete" value="delete" />
            <el-option label="timeout" value="timeout" />
          </el-select>
        </el-form-item>
        <el-form-item label="监听器类型">
          <el-select v-model="form.implKind" style="width: 100%">
            <el-option label="Java 类" value="class" />
            <el-option label="表达式" value="expression" />
            <el-option label="委托表达式" value="delegateExpression" />
          </el-select>
        </el-form-item>
        <el-form-item :label="implLabel">
          <el-input v-model="form.implText" type="textarea" :rows="2" />
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
import QfdPanelTaskListenersService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelTaskListenersService";

const props = withDefaults(
  defineProps<{
    modeler: unknown;
    element: unknown;
    heading?: string;
  }>(),
  {
    heading: "任务监听器",
  }
);

const { rows, dialogVisible, editing, form, implLabel, openDialog, save, removeRow, dispose } =
  QfdPanelTaskListenersService.useQfdPanelTaskListeners(
    () => props.modeler,
    () => props.element
  );

onBeforeUnmount(() => {
  dispose();
});
</script>

<style scoped>
.qfd-task-listeners {
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
