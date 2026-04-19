import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetQfTodoListDto,
  GetQfTodoListVo,
  GetQfTodoDetailsVo,
  AddQfTodoDto,
  EditQfTodoDto,
} from "@/views/qf/api/QfTodoApi.ts";
import QfTodoApi from "@/views/qf/api/QfTodoApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 待办事项列表管理
   */
  useQfTodoList() {
    const listForm = ref<GetQfTodoListDto>({
      pageNum: 1,
      pageSize: 20,
      summary: "",
      memberId: "",
      initiatorId: "",
      createTime: "",
    });

    const listData = ref<GetQfTodoListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await QfTodoApi.getQfTodoList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    /**
     * 重置查询
     */
    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.summary = "";
      listForm.value.memberId = "";
      listForm.value.initiatorId = "";
      listForm.value.createTime = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetQfTodoListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该条记录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await QfTodoApi.removeQfTodo({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      await loadList();
    });

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      removeList,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useQfTodoModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetQfTodoDetailsVo>({
      id: "",
      nodeName: "",
      summary: "",
      memberId: "",
      initiatorId: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {};

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetQfTodoListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.nodeName = "";
        modalForm.summary = "";
        modalForm.memberId = "";
        modalForm.initiatorId = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await QfTodoApi.getQfTodoDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.nodeName = details.nodeName;
          modalForm.summary = details.summary;
          modalForm.memberId = details.memberId;
          modalForm.initiatorId = details.initiatorId;
          modalVisible.value = true;
        } catch (error: any) {
          ElMessage.error(error.message);
        }
      }
    };

    /**
     * 重置模态框
     */
    const resetModal = (): void => {
      if (!modalFormRef.value) {
        return;
      }
      modalFormRef.value.resetFields();
      modalForm.id = "";
      modalForm.nodeName = "";
      modalForm.summary = "";
      modalForm.memberId = "";
      modalForm.initiatorId = "";
    };

    /**
     * 提交模态框
     */
    const submitModal = async (): Promise<void> => {
      if (!modalFormRef.value) {
        return;
      }

      try {
        await modalFormRef.value.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      if (modalMode.value === "add") {
        try {
          const addDto: AddQfTodoDto = {};
          await QfTodoApi.addQfTodo(addDto);
          ElMessage.success("新增成功");
          modalVisible.value = false;
          resetModal();
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
        return;
      }

      if (modalMode.value === "edit") {
        if (!modalForm.id) {
          ElMessage.error("缺少ID参数");
          modalLoading.value = false;
          return;
        }

        try {
          const editDto: EditQfTodoDto = {
            id: modalForm.id,
          };
          await QfTodoApi.editQfTodo(editDto);
          ElMessage.success("编辑成功");
          modalVisible.value = false;
          resetModal();
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
      }
    };

    return {
      modalVisible,
      modalLoading,
      modalMode,
      modalForm,
      modalRules,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
