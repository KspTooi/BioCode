<template>
  <el-dialog
    :model-value="visible"
    title="审批"
    class="h-full"
    fullscreen
    :close-on-click-modal="false"
    @close="service.close()"
  >
    <div v-loading="service.detailsLoading.value">
      <!-- 待办信息 -->
      <el-descriptions :column="1" border class="mb-4">
        <el-descriptions-item label="当前节点">
          {{ service.details.value?.nodeName || "—" }}
        </el-descriptions-item>
        <el-descriptions-item label="摘要">
          {{ service.details.value?.summary || "—" }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 审批表单 -->
      <el-form label-width="80px">
        <el-form-item label="审批动作" required>
          <el-radio-group v-model="service.action.value">
            <el-radio-button :value="0">
              <el-icon class="mr-1"><CircleCheck /></el-icon>同意
            </el-radio-button>
            <el-radio-button :value="1">
              <el-icon class="mr-1"><CircleClose /></el-icon>驳回
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批意见">
          <el-input
            v-model="service.comment.value"
            type="textarea"
            :rows="4"
            :placeholder="service.action.value === 0 ? '请输入同意意见（可选）' : '请输入驳回原因'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="service.close()">取消</el-button>
      <el-button
        v-if="service.action.value === 1"
        type="danger"
        :loading="service.submitLoading.value"
        :disabled="service.detailsLoading.value"
        @click="service.submit()"
      >
        驳回
      </el-button>
      <el-button
        v-else
        type="primary"
        :loading="service.submitLoading.value"
        :disabled="service.detailsLoading.value"
        @click="service.submit()"
      >
        同意
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { CircleCheck, CircleClose } from "@element-plus/icons-vue";
import QfApproveModalService from "@/views/qf/public/QfApproveModalService.ts";

const props = defineProps<{
  todoId: string | null;
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: "update:visible", val: boolean): void;
  (e: "approved"): void;
}>();

const service = QfApproveModalService.useQfApproveModal(
  () => props.todoId,
  () => props.visible,
  emit
);
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}
.mr-1 {
  margin-right: 4px;
}
</style>
