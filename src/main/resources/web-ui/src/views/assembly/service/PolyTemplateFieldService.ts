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

type ModalMode = "add" | "edit";

export default {
  /**
   * 聚合模板字段列表管理，polyTemplateId 由 CDRC 传入
   */
  usePolyTemplateFieldList(polyTemplateId: Ref<string>) {
    const listForm = ref<GetPolyTemplateFieldListDto>({
      pageNum: 1,
      pageSize: 20,
      polyTemplateId: polyTemplateId.value,
      name: "",
    });

    const listData = ref<GetPolyTemplateFieldListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载字段列表
     */
    const loadList = async (): Promise<void> => {
      listForm.value.polyTemplateId = polyTemplateId.value;
      listLoading.value = true;
      const result = await PolyTemplateFieldApi.getPolyTemplateFieldList(listForm.value);
      listLoading.value = false;
      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        return;
      }
      ElMessage.error(result.message || "加载字段列表失败");
    };

    /**
     * 重置查询条件并刷新
     */
    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.polyTemplateId = polyTemplateId.value;
      listForm.value.name = "";
      loadList();
    };

    /**
     * 删除单条字段记录（含二次确认）
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
        await PolyTemplateFieldApi.removePolyTemplateField({ id: String(row.id) });
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
   * 字段模态框管理，polyTemplateId 由 CDRC 传入，新增时自动注入父模板ID
   */
  usePolyTemplateFieldModal(
    modalFormRef: Ref<FormInstance | undefined>,
    polyTemplateId: Ref<string>,
    reloadCallback: () => void
  ) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetPolyTemplateFieldDetailsVo>({
      id: "",
      polyTemplateId: "",
      name: "",
      policyCrudJson: [],
      policyQuery: 0,
      policyView: 0,
      seq: 0,
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入字段名", trigger: "blur" },
        { max: 255, message: "字段名长度不能超过255个字符", trigger: "blur" },
      ],
      policyQuery: [{ required: true, message: "请选择查询策略", trigger: "change" }],
      policyView: [{ required: true, message: "请选择显示策略", trigger: "change" }],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    };

    /**
     * 打开字段模态框，add 模式自动注入 polyTemplateId
     */
    const openModal = async (mode: ModalMode, row: GetPolyTemplateFieldListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.polyTemplateId = polyTemplateId.value;
        modalForm.name = "";
        modalForm.policyCrudJson = [];
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
          const details = await PolyTemplateFieldApi.getPolyTemplateFieldDetails({ id: String(row.id) });
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
     * 重置模态框状态
     */
    const resetModal = (): void => {
      if (!modalFormRef.value) {
        return;
      }
      modalFormRef.value.resetFields();
      modalForm.id = "";
      modalForm.polyTemplateId = polyTemplateId.value;
      modalForm.name = "";
      modalForm.policyCrudJson = [];
      modalForm.policyQuery = 0;
      modalForm.policyView = 0;
      modalForm.seq = 0;
    };

    /**
     * 提交字段表单（校验 → 构造 Dto → 调 Api → 提示 → 关闭 → 刷新）
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
            polyTemplateId: polyTemplateId.value,
            name: modalForm.name,
            policyCrudJson: modalForm.policyCrudJson,
            policyQuery: modalForm.policyQuery,
            policyView: modalForm.policyView,
            seq: modalForm.seq,
          };
          await PolyTemplateFieldApi.addPolyTemplateField(addDto);
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
          const editDto: EditPolyTemplateFieldDto = {
            id: modalForm.id,
            polyTemplateId: polyTemplateId.value,
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

  /**
   * 行内编辑提交
   */
  usePolyTemplateFieldCellEdit() {
    const submitRow = async (row: GetPolyTemplateFieldListVo): Promise<boolean> => {
      try {
        const details = await PolyTemplateFieldApi.getPolyTemplateFieldDetails({ id: String(row.id) });
        const editDto: EditPolyTemplateFieldDto = { ...details, ...row };
        await PolyTemplateFieldApi.editPolyTemplateField(editDto);
        //ElMessage.success("更新成功");
        return true;
      } catch (error: any) {
        ElMessage.error(error.message);
        return false;
      }
    };

    const commitField = async (row: GetPolyTemplateFieldListVo, field: string, value: any): Promise<boolean> => {
      try {
        const details = await PolyTemplateFieldApi.getPolyTemplateFieldDetails({ id: String(row.id) });
        const editDto: EditPolyTemplateFieldDto = { ...details, [field]: value };
        await PolyTemplateFieldApi.editPolyTemplateField(editDto);
        //ElMessage.success("更新成功");
        return true;
      } catch (error: any) {
        ElMessage.error(error.message);
        return false;
      }
    };

    return {
      submitRow,
      commitField,
    };
  },
};
