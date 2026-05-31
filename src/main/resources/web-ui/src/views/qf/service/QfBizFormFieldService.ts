import { reactive, ref, type Ref } from "vue";
import type { GetQfBizFormListVo } from "@/views/qf/api/QfBizFormApi.ts";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetQfBizFormFieldListDto,
  GetQfBizFormFieldListVo,
  GetQfBizFormFieldDetailsVo,
  AddQfBizFormFieldDto,
  EditQfBizFormFieldDto,
} from "@/views/qf/api/QfBizFormFieldApi.ts";
import QfBizFormFieldApi from "@/views/qf/api/QfBizFormFieldApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 字段配置列表模态框（列表 + 弹窗上下文）
   */
  useBizFormFieldListModal() {
    const modalVisible = ref(false);
    const modalFormName = ref("");

    const listForm = ref<GetQfBizFormFieldListDto>({
      pageNum: 1,
      pageSize: 20,
      formId: "",
    });

    const listData = ref<GetQfBizFormFieldListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await QfBizFormFieldApi.getBizFormFieldList(listForm.value);

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
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetQfBizFormFieldListVo): Promise<void> => {
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
        await QfBizFormFieldApi.removeBizFormField({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 打开字段配置列表模态框
     */
    const openModal = (row: GetQfBizFormListVo): void => {
      listForm.value.formId = String(row.id);
      listForm.value.pageNum = 1;
      modalFormName.value = row.name;
      modalVisible.value = true;
      loadList();
    };

    /**
     * 关闭字段配置列表模态框
     */
    const closeModal = (): void => {
      modalVisible.value = false;
      modalFormName.value = "";
      listForm.value.formId = "";
    };

    return {
      modalVisible,
      modalFormName,
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      removeList,
      openModal,
      closeModal,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useBizFormFieldModal(
    modalFormRef: Ref<FormInstance | undefined>,
    reloadCallback: () => void,
    listForm: Ref<GetQfBizFormFieldListDto>
  ) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetQfBizFormFieldDetailsVo>({
      id: "",
      formId: "",
      fieldName: "",
      remark: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      formId: [{ required: true, message: "请输入业务表ID", trigger: "blur" }],
      fieldName: [
        { required: true, message: "请输入字段名", trigger: "blur" },
        { max: 32, message: "字段名长度不能超过32个字符", trigger: "blur" },
      ],
      remark: [
        { required: true, message: "请输入备注", trigger: "blur" },
        { max: 32, message: "备注长度不能超过32个字符", trigger: "blur" },
      ],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetQfBizFormFieldListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.formId = listForm.value.formId ?? "";
        modalForm.fieldName = "";
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
          const details = await QfBizFormFieldApi.getBizFormFieldDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.formId = details.formId;
          modalForm.fieldName = details.fieldName;
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
      modalForm.formId = "";
      modalForm.fieldName = "";
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
          const addDto: AddQfBizFormFieldDto = {
            formId: modalForm.formId,
            fieldName: modalForm.fieldName,
            remark: modalForm.remark,
          };
          await QfBizFormFieldApi.addBizFormField(addDto);
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
          const editDto: EditQfBizFormFieldDto = {
            id: modalForm.id,
            formId: modalForm.formId,
            fieldName: modalForm.fieldName,
            remark: modalForm.remark,
          };
          await QfBizFormFieldApi.editBizFormField(editDto);
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
