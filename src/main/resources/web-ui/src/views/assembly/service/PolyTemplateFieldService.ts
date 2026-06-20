import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetPolyTemplateFieldListDto,
  GetPolyTemplateFieldListVo,
  GetPolyTemplateFieldDetailsVo,
  AddPolyTemplateFieldDto,
  EditPolyTemplateFieldDto,
} from "@/views/assembly/api/PolyTemplateFieldApi.ts";
import PolyTemplateFieldApi from "@/views/assembly/api/PolyTemplateFieldApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 聚合模板字段列表管理
   */
  usePolyTemplateFieldList() {
    const listForm = ref<GetPolyTemplateFieldListDto>({
      pageNum: 1,
      pageSize: 20,
      polyTemplateId: "",
      name: "",
      policyCrudJson: "",
      policyQuery: null,
      policyView: null,
      seq: null,
    });

    const listData = ref<GetPolyTemplateFieldListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await PolyTemplateFieldApi.getPolyTemplateFieldList(listForm.value);

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
      listForm.value.polyTemplateId = "";
      listForm.value.name = "";
      listForm.value.policyCrudJson = "";
      listForm.value.policyQuery = null;
      listForm.value.policyView = null;
      listForm.value.seq = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetPolyTemplateFieldListVo): Promise<void> => {
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
        await PolyTemplateFieldApi.removePolyTemplateField({ id: row.id });
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
  usePolyTemplateFieldModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetPolyTemplateFieldDetailsVo>({
      id: "",
      polyTemplateId: "",
      name: "",
      policyCrudJson: "",
      policyQuery: 0,
      policyView: 0,
      seq: 0,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      polyTemplateId: [{ required: true, message: "请输入聚合模板ID", trigger: "blur" }],
      name: [
        { required: true, message: "请输入字段名", trigger: "blur" },
        { max: 255, message: "字段名长度不能超过255个字符", trigger: "blur" },
      ],
      policyCrudJson: [{ required: true, message: "请输入可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW", trigger: "blur" }],
      policyQuery: [{ required: true, message: "请输入查询策略 0:等于", trigger: "blur" }],
      policyView: [
        { required: true, message: "请输入显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT", trigger: "blur" },
      ],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetPolyTemplateFieldListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.polyTemplateId = "";
        modalForm.name = "";
        modalForm.policyCrudJson = "";
        modalForm.policyQuery = 0;
        modalForm.policyView = 0;
        modalForm.seq = 0;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await PolyTemplateFieldApi.getPolyTemplateFieldDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.polyTemplateId = details.polyTemplateId;
          modalForm.name = details.name;
          modalForm.policyCrudJson = details.policyCrudJson;
          modalForm.policyQuery = details.policyQuery;
          modalForm.policyView = details.policyView;
          modalForm.seq = details.seq;
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
      modalForm.polyTemplateId = "";
      modalForm.name = "";
      modalForm.policyCrudJson = "";
      modalForm.policyQuery = 0;
      modalForm.policyView = 0;
      modalForm.seq = 0;
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
          const addDto: AddPolyTemplateFieldDto = {
            polyTemplateId: modalForm.polyTemplateId,
            name: modalForm.name,
            policyCrudJson: modalForm.policyCrudJson,
            policyQuery: modalForm.policyQuery,
            policyView: modalForm.policyView,
            seq: modalForm.seq,
          };
          await PolyTemplateFieldApi.addPolyTemplateField(addDto);
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
          const editDto: EditPolyTemplateFieldDto = {
            id: modalForm.id,
            polyTemplateId: modalForm.polyTemplateId,
            name: modalForm.name,
            policyCrudJson: modalForm.policyCrudJson,
            policyQuery: modalForm.policyQuery,
            policyView: modalForm.policyView,
            seq: modalForm.seq,
          };
          await PolyTemplateFieldApi.editPolyTemplateField(editDto);
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
