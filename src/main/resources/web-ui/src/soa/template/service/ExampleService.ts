import { computed, onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetExampleListDto,
  GetExampleListVo,
  GetExampleDetailsVo,
  AddExampleDto,
  EditExampleDto,
} from "@/soa/template/api/ExampleApi.ts";
import ExampleApi from "@/soa/template/api/ExampleApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 示例列表管理
   */
  useExampleList() {
    const listForm = ref<GetExampleListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      status: null,
    });

    const listData = ref<GetExampleListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await ExampleApi.getExampleList(listForm.value);

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
      listForm.value.name = "";
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetExampleListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该条记录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch (error) {
        return;
      }

      try {
        await ExampleApi.removeExample({ id: row.id });
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
   * 模态框管理（统一处理创建和编辑）
   */
  useExampleModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetExampleDetailsVo>({
      id: "",
      name: "",
      description: "",
      status: 0,
      createTime: "",
      updateTime: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [{ required: true, message: "请输入名称", trigger: "blur" }],
      status: [{ required: true, message: "请选择状态", trigger: "change" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetExampleListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.description = "";
        modalForm.status = 0;
        modalForm.createTime = "";
        modalForm.updateTime = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await ExampleApi.getExampleDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.description = details.description;
          modalForm.status = details.status;
          modalForm.createTime = details.createTime;
          modalForm.updateTime = details.updateTime;
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
      modalForm.name = "";
      modalForm.description = "";
      modalForm.status = 0;
      modalForm.createTime = "";
      modalForm.updateTime = "";
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
      } catch (error) {
        return;
      }

      modalLoading.value = true;

      if (modalMode.value === "add") {
        try {
          const addDto: AddExampleDto = {
            name: modalForm.name,
            description: modalForm.description,
            status: modalForm.status,
          };
          await ExampleApi.addExample(addDto);
          ElMessage.success("创建成功");
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
          const editDto: EditExampleDto = {
            id: modalForm.id,
            name: modalForm.name,
            description: modalForm.description,
            status: modalForm.status,
          };
          await ExampleApi.editExample(editDto);
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
