import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAacpFuncListDto,
  GetAacpFuncListVo,
  GetAacpFuncDetailsVo,
  AddAacpFuncDto,
  EditAacpFuncDto,
} from "@/views/aacpFunc/api/AacpFuncApi.ts";
import AacpFuncApi from "@/views/aacpFunc/api/AacpFuncApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 微函数列表管理
   */
  useAacpFuncList() {
    const listForm = ref<GetAacpFuncListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      description: "",
    });

    const listData = ref<GetAacpFuncListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AacpFuncApi.getAacpFuncList(listForm.value);

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
      listForm.value.code = "";
      listForm.value.description = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetAacpFuncListVo): Promise<void> => {
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
        await AacpFuncApi.removeAacpFunc({ id: row.id });
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
  useAacpFuncModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpFuncDetailsVo>({
      id: "",
      name: "",
      code: "",
      description: "",
      schema: "",
      target: "",
      remark: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入微函数名称", trigger: "blur" },
        { max: 40, message: "微函数名称长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入微函数标识", trigger: "blur" },
        { max: 32, message: "微函数标识长度不能超过32个字符", trigger: "blur" },
      ],
      description: [
        { required: true, message: "请输入意图词", trigger: "blur" },
        { max: 1000, message: "意图词长度不能超过1000个字符", trigger: "blur" },
      ],
      target: [
        { required: true, message: "请输入调用目标Bean", trigger: "blur" },
        { max: 1000, message: "调用目标Bean长度不能超过1000个字符", trigger: "blur" },
      ],
      remark: [{ max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetAacpFuncListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.description = "";
        modalForm.schema = "";
        modalForm.target = "";
        modalForm.remark = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await AacpFuncApi.getAacpFuncDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.description = details.description;
          modalForm.schema = details.schema;
          modalForm.target = details.target;
          modalForm.remark = details.remark;
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
      modalForm.code = "";
      modalForm.description = "";
      modalForm.schema = "";
      modalForm.target = "";
      modalForm.remark = "";
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
          const addDto: AddAacpFuncDto = {
            name: modalForm.name,
            code: modalForm.code,
            description: modalForm.description,
            schema: modalForm.schema,
            target: modalForm.target,
            remark: modalForm.remark,
          };
          await AacpFuncApi.addAacpFunc(addDto);
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
          const editDto: EditAacpFuncDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            description: modalForm.description,
            schema: modalForm.schema,
            target: modalForm.target,
            remark: modalForm.remark,
          };
          await AacpFuncApi.editAacpFunc(editDto);
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
