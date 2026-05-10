<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="core-org-dept-select-modal"
    @opened="onOpened"
  >
    <div v-loading="loading" class="modal-body">
      <div class="tree-container">
        <OrgTree ref="orgTreeRef" @on-select="onSelect" @on-check="onCheck" />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" @click="onConfirm">
          确定{{ multiple && validDeptCount > 0 ? `(${validDeptCount})` : "" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from "vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import CoreOrgDeptSelectModalService from "@/views/core/service/CoreOrgDeptSelectModalService";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

const props = withDefaults(
  defineProps<{
    modelValue?: boolean; // 弹窗显隐控制 (支持 v-model)
    title?: string; // 弹窗标题，默认为 "选择部门"
    width?: string | number; // 弹窗宽度，默认为 "450px"
    multiple?: boolean; // 是否开启多选模式，默认为 false
    defaultSelected?: string | string[]; // 默认选中的节点 ID 或 ID 数组
    type?: "dept" | "all"; // 选择类型，默认为 "dept"
  }>(),
  {
    modelValue: false,
    title: "选择部门",
    width: "450px",
    multiple: false,
    defaultSelected: undefined,
    type: "dept",
  }
);

const emit = defineEmits<{
  (e: "update:modelValue", visible: boolean): void;
  (e: "confirm", data: GetOrgTreeVo | GetOrgTreeVo[]): void;
  (e: "cancel"): void;
}>();

const visible = ref(false);
const loading = ref(false);
const orgTreeRef = ref();
const { selectedNode, selectedNodes, onSelect, onCheck } = CoreOrgDeptSelectModalService.useDeptSelect();

// 计算有效的部门节点数量（过滤掉企业节点 kind === 1）
const validDeptCount = computed(() => {
  return selectedNodes.value.filter((node) => node.kind === 0).length;
});

// 用于 Promise 式调用的状态
let promiseResolve: (value: GetOrgTreeVo | GetOrgTreeVo[]) => void;
let promiseReject: (reason?: any) => void;

// 同步外部 visible
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
  },
  { immediate: true }
);

/**
 * 弹窗打开后处理逻辑
 */
const onOpened = async (): Promise<void> => {
  loading.value = true;
  try {
    // 等待树数据加载完成后再初始化选中状态
    if (orgTreeRef.value) {
      await orgTreeRef.value.loadTreeData();
    }
    initSelection();
  } finally {
    loading.value = false;
  }
};

// 同步内部 visible 到外部
watch(
  () => visible.value,
  (val) => {
    emit("update:modelValue", val);
  }
);

/**
 * 命令式调用方法：打开弹窗并等待选择结果
 */
const select = async (): Promise<GetOrgTreeVo | GetOrgTreeVo[]> => {
  visible.value = true;

  // 等待组件挂载后再访问 orgTreeRef
  await nextTick();

  // 等待树数据加载完成
  if (orgTreeRef.value) {
    await orgTreeRef.value.loadTreeData();
  }

  initSelection();

  return new Promise((resolve, reject) => {
    promiseResolve = resolve;
    promiseReject = reject;
  });
};

const initSelection = (): void => {
  if (!orgTreeRef.value) {
    return;
  }

  // 清除旧的选中状态
  orgTreeRef.value.setCheckedKeys([]);
  selectedNode.value = null;
  selectedNodes.value = [];

  // 如果有默认选中，则恢复选中状态
  if (props.defaultSelected && Array.isArray(props.defaultSelected) && props.defaultSelected.length > 0) {
    if (props.multiple) {
      // 恢复树组件的视觉勾选状态
      orgTreeRef.value.setCheckedKeys(props.defaultSelected);

      // 手动同步 selectedNodes（setCheckedKeys 不会触发 on-check 事件）
      nextTick(() => {
        if (orgTreeRef.value) {
          const checkedNodes = orgTreeRef.value.getCheckedNodes();
          onCheck(checkedNodes);
        }
      });
    }
  }
};

const onConfirm = (): void => {
  let result: GetOrgTreeVo | GetOrgTreeVo[] | null = null;

  if (props.multiple) {
    // 多选模式：过滤掉企业节点（kind === 1），只保留部门节点（kind === 0）
    result = selectedNodes.value.filter((node) => node.kind === 0);
  }
  if (!props.multiple) {
    result = selectedNode.value;
  }

  if (!result) {
    return;
  }

  if (promiseResolve) {
    promiseResolve(result);
  }
  emit("confirm", selectedNodes.value);
  visible.value = false;
};

const onCancel = (): void => {
  visible.value = false;
  if (promiseReject) {
    promiseReject("cancel");
  }
  emit("cancel");
};

defineExpose({
  open: () => (visible.value = true),
  close: () => (visible.value = false),
  select,
});
</script>

<style scoped>
.core-org-dept-select-modal :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.modal-body {
  height: 500px;
  display: flex;
  flex-direction: column;
  border-radius: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.tree-container {
  flex: 1;
  overflow: hidden;
  padding: 10px;
}

.dialog-footer {
  padding-top: 10px;
}

/* 直角风格适配 */
:deep(.el-dialog) {
  border-radius: 0;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: var(--el-text-color-primary);
}
</style>
