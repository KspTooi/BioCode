import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { GetQfTodoListDto, GetQfTodoListVo, GetQfTodoDetailsVo, EditQfTodoDto } from "@/views/qf/api/QfTodoApi.ts";
import QfTodoApi from "@/views/qf/api/QfTodoApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

type ModalMode = "edit";

export default {
  /**
   * 待办事项列表管理
   */
  useQfTodoList() {
    const listForm = ref<GetQfTodoListDto>({
      pageNum: 1,
      pageSize: 20,
      nodeName: "",
      bizFormId: "",
      status: 0, // 待办状态 0:待办 1:已办
    });

    const listData = ref<GetQfTodoListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await QfTodoApi.getQfTodoList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("qf-todo", listForm.value);
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.nodeName = "";
      listForm.value.bizFormId = "";
      listForm.value.status = 0;
      QueryPersistService.clearQuery("qf-todo");
      loadList();
    };

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
      } catch (error: unknown) {
        ElMessage.error((error as Error).message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery("qf-todo", listForm.value);
      await loadList();
    });

    return { listForm, listData, listTotal, listLoading, loadList, resetList, removeList };
  },

  /**
   * 模态框管理（仅编辑）
   */
  useQfTodoModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("edit");
    const modalForm = reactive<GetQfTodoDetailsVo>({
      id: "",
      nodeName: "",
      summary: "",
      memberId: "",
      initiatorId: "",
      routePc: "",
      routeMobile: "",
      dataId: "",
      engProcId: "",
      allowComment: 0,
      allowActions: [
        { kind: 0, name: "同意" },
        { kind: 1, name: "驳回" },
      ],
      allowEditFields: [],
    });

    const modalRules: FormRules = {};

    const openModal = async (mode: ModalMode, row: GetQfTodoListVo | null): Promise<void> => {
      modalMode.value = mode;

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
      } catch (error: unknown) {
        ElMessage.error((error as Error).message);
      }
    };

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

    const submitModal = async (): Promise<void> => {
      if (!modalFormRef.value) {
        return;
      }

      try {
        await modalFormRef.value.validate();
      } catch {
        return;
      }

      if (!modalForm.id) {
        ElMessage.error("缺少ID参数");
        return;
      }

      modalLoading.value = true;

      try {
        const editDto: EditQfTodoDto = { id: modalForm.id };
        await QfTodoApi.editQfTodo(editDto);
        ElMessage.success("编辑成功");
        modalVisible.value = false;
        resetModal();
        reloadCallback();
      } catch (error: unknown) {
        ElMessage.error((error as Error).message);
      }

      modalLoading.value = false;
    };

    return { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal };
  },
};
