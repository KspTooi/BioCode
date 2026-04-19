<template>
  <div class="qfd-msg-and-signals">
    <div class="qfd-section-block">
      <div class="qfd-section-head">
        <span>消息列表</span>
        <el-button type="primary" link size="small" @click="openMessageDialog()">+ 创建新消息</el-button>
      </div>
      <el-table :data="messageRows" size="small" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="56" />
        <el-table-column prop="id" label="消息 ID" min-width="100" show-overflow-tooltip />
        <el-table-column prop="name" label="消息名称" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openMessageDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeMessage(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="qfd-section-block">
      <div class="qfd-section-head">
        <span>信号列表</span>
        <el-button type="primary" link size="small" @click="openSignalDialog()">+ 创建新信号</el-button>
      </div>
      <el-table :data="signalRows" size="small" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="56" />
        <el-table-column prop="id" label="信号 ID" min-width="100" show-overflow-tooltip />
        <el-table-column prop="name" label="信号名称" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openSignalDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="removeSignal(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="messageDialogVisible" :title="messageEditing ? '编辑消息' : '新建消息'" width="420px" destroy-on-close>
      <el-form label-width="88px" size="small">
        <el-form-item label="消息 ID">
          <el-input v-model="messageForm.id" />
        </el-form-item>
        <el-form-item label="消息名称">
          <el-input v-model="messageForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMessage">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="signalDialogVisible" :title="signalEditing ? '编辑信号' : '新建信号'" width="420px" destroy-on-close>
      <el-form label-width="88px" size="small">
        <el-form-item label="信号 ID">
          <el-input v-model="signalForm.id" />
        </el-form-item>
        <el-form-item label="信号名称">
          <el-input v-model="signalForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSignal">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount } from "vue";
import QfdPanelMsgAndSignalsService from "@/views/qf/sfc_private/flowable-designer/service/QfdPanelMsgAndSignalsService";

const props = defineProps<{
  modeler: unknown;
}>();

const {
  messageRows,
  signalRows,
  messageDialogVisible,
  signalDialogVisible,
  messageEditing,
  signalEditing,
  messageForm,
  signalForm,
  openMessageDialog,
  openSignalDialog,
  saveMessage,
  saveSignal,
  removeMessage,
  removeSignal,
  dispose,
} = QfdPanelMsgAndSignalsService.useQfdPanelMsgAndSignals(() => props.modeler);

onBeforeUnmount(() => {
  dispose();
});
</script>

<style scoped>
.qfd-msg-and-signals {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.qfd-section-block {
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
