<template>
  <div class="qfd-execution-listeners">
    <div class="qfd-section-head">
      <span>{{ heading }}</span>
      <el-button type="primary" link size="small" @click="openDialog()">+ 添加监听器</el-button>
    </div>
    <el-table :data="rows" size="small" border stripe empty-text="暂无数据">
      <el-table-column type="index" label="序号" width="56" />
      <el-table-column prop="event" label="事件" width="88" />
      <el-table-column prop="implType" label="类型" width="100" />
      <el-table-column prop="implValue" label="实现" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑执行监听器' : '新建执行监听器'" width="480px" destroy-on-close>
      <el-form label-width="100px" size="small">
        <el-form-item label="事件类型">
          <el-select v-model="form.event" style="width: 100%">
            <el-option label="start" value="start" />
            <el-option label="end" value="end" />
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
import QfdPanelExecutionListenersService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelExecutionListenersService";

const props = withDefaults(
  defineProps<{
    modeler: unknown;
    element: unknown;
    heading?: string;
  }>(),
  {
    heading: "流程执行监听器",
  }
);

const { rows, dialogVisible, editing, form, implLabel, openDialog, save, removeRow, dispose } =
  QfdPanelExecutionListenersService.useQfdPanelExecutionListeners(
    () => props.modeler,
    () => props.element
  );

onBeforeUnmount(() => {
  dispose();
});
</script>

<style scoped>
.qfd-execution-listeners {
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
