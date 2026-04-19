import { ref, watch } from "vue";
import type { Ref } from "vue";
import { ElMessage } from "element-plus";
import QfTodoApi from "@/views/qf/api/QfTodoApi.ts";
import type { GetQfTodoDetailsVo } from "@/views/qf/api/QfTodoApi.ts";

export type ApproveAction = 0 | 1;

export default {
  useQfApproveModal(
    todoIdGetter: () => string | null | undefined,
    visibleGetter: () => boolean,
    emit: (event: "update:visible" | "approved", ...args: unknown[]) => void
  ) {
    const details = ref<GetQfTodoDetailsVo | null>(null);
    const detailsLoading = ref(false);
    const submitLoading = ref(false);
    const action = ref<ApproveAction>(0);
    const comment = ref("");

    const close = (): void => {
      emit("update:visible", false);
    };

    const reset = (): void => {
      details.value = null;
      action.value = 0;
      comment.value = "";
    };

    const loadDetails = async (): Promise<void> => {
      const id = todoIdGetter();
      if (!id) {
        return;
      }
      detailsLoading.value = true;
      try {
        details.value = await QfTodoApi.getQfTodoDetails({ id });
      } catch (error: unknown) {
        ElMessage.error((error as Error).message ?? "加载失败");
      }
      detailsLoading.value = false;
    };

    const submit = async (): Promise<void> => {
      const id = todoIdGetter();
      if (!id) {
        return;
      }
      submitLoading.value = true;
      try {
        const msg = await QfTodoApi.approveQfTodo({
          id,
          action: action.value,
          comment: comment.value,
        });
        ElMessage.success(msg || "审批成功");
        emit("approved");
        close();
        reset();
      } catch (error: unknown) {
        ElMessage.error((error as Error).message ?? "审批失败");
      }
      submitLoading.value = false;
    };

    watch(
      visibleGetter,
      (visible) => {
        if (!visible) {
          reset();
          return;
        }
        void loadDetails();
      },
      { immediate: true }
    );

    return {
      details,
      detailsLoading,
      submitLoading,
      action,
      comment,
      submit,
      close,
    };
  },
};
