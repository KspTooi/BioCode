import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetModelListDto,
  GetModelListVo,
  GetModelDetailsVo,
  AddModelDto,
  EditModelDto,
} from "@/views/aacp/api/ModelApi.ts";
import ModelApi from "@/views/aacp/api/ModelApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 模型变体列表管理
   */
  useModelList() {
    const listForm = ref<GetModelListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      kind: null,
      status: null,
    });

    const listData = ref<GetModelListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await ModelApi.getModelList(listForm.value);

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
      listForm.value.kind = null;
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetModelListVo): Promise<void> => {
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
        await ModelApi.removeModel({ id: row.id });
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
  useModelModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetModelDetailsVo>({
      name: "",
      code: "",
      kind: 0,
      maxContext: 0,
      maxOutputToken: 0,
      apiReasoning: 0,
      apiReasoningEffort: 0,
      apiAppendParam: "",
      apiAppendHeaders: "",
      fincInput: "",
      fincInputCached: "",
      fincOutput: "",
      testTtfb: 0,
      testRate: 0,
      testTime: "",
      remark: "",
      seq: 0,
      status: 0,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入模型变体名称", trigger: "blur" },
        { max: 80, message: "模型变体名称长度不能超过80个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入模型标识", trigger: "blur" },
        { max: 64, message: "模型标识长度不能超过64个字符", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请输入类型 0:文本 1:图形 2:音频 3:多模态", trigger: "blur" }],
      maxContext: [{ required: true, message: "请输入最大上下文长度", trigger: "blur" }],
      maxOutputToken: [{ required: true, message: "请输入最大输出词元", trigger: "blur" }],
      apiReasoning: [{ required: true, message: "请输入推理 0:不支持 1:支持", trigger: "blur" }],
      apiReasoningEffort: [{ required: true, message: "请输入推理强度 0:关 1:低 2:中 3:高 4:极高", trigger: "blur" }],
      apiAppendParam: [{ required: true, message: "请输入附加参数", trigger: "blur" }],
      apiAppendHeaders: [{ required: true, message: "请输入附加请求头", trigger: "blur" }],
      fincInput: [{ required: true, message: "请输入输入单价", trigger: "blur" }],
      fincInputCached: [{ required: true, message: "请输入输入单价(缓存)", trigger: "blur" }],
      fincOutput: [{ required: true, message: "请输入输出单价", trigger: "blur" }],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
      status: [{ required: true, message: "请输入状态 0:禁用 1:启用", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetModelListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.name = "";
        modalForm.code = "";
        modalForm.kind = 0;
        modalForm.maxContext = 0;
        modalForm.maxOutputToken = 0;
        modalForm.apiReasoning = 0;
        modalForm.apiReasoningEffort = 0;
        modalForm.apiAppendParam = "";
        modalForm.apiAppendHeaders = "";
        modalForm.fincInput = "";
        modalForm.fincInputCached = "";
        modalForm.fincOutput = "";
        modalForm.testTtfb = 0;
        modalForm.testRate = 0;
        modalForm.testTime = "";
        modalForm.remark = "";
        modalForm.seq = 0;
        modalForm.status = 0;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await ModelApi.getModelDetails({ id: row.id });
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.kind = details.kind;
          modalForm.maxContext = details.maxContext;
          modalForm.maxOutputToken = details.maxOutputToken;
          modalForm.apiReasoning = details.apiReasoning;
          modalForm.apiReasoningEffort = details.apiReasoningEffort;
          modalForm.apiAppendParam = details.apiAppendParam;
          modalForm.apiAppendHeaders = details.apiAppendHeaders;
          modalForm.fincInput = details.fincInput;
          modalForm.fincInputCached = details.fincInputCached;
          modalForm.fincOutput = details.fincOutput;
          modalForm.testTtfb = details.testTtfb;
          modalForm.testRate = details.testRate;
          modalForm.testTime = details.testTime;
          modalForm.remark = details.remark;
          modalForm.seq = details.seq;
          modalForm.status = details.status;
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
      modalForm.name = "";
      modalForm.code = "";
      modalForm.kind = 0;
      modalForm.maxContext = 0;
      modalForm.maxOutputToken = 0;
      modalForm.apiReasoning = 0;
      modalForm.apiReasoningEffort = 0;
      modalForm.apiAppendParam = "";
      modalForm.apiAppendHeaders = "";
      modalForm.fincInput = "";
      modalForm.fincInputCached = "";
      modalForm.fincOutput = "";
      modalForm.testTtfb = 0;
      modalForm.testRate = 0;
      modalForm.testTime = "";
      modalForm.remark = "";
      modalForm.seq = 0;
      modalForm.status = 0;
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
          const addDto: AddModelDto = {
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            maxContext: modalForm.maxContext,
            maxOutputToken: modalForm.maxOutputToken,
            apiReasoning: modalForm.apiReasoning,
            apiReasoningEffort: modalForm.apiReasoningEffort,
            apiAppendParam: modalForm.apiAppendParam,
            apiAppendHeaders: modalForm.apiAppendHeaders,
            fincInput: modalForm.fincInput,
            fincInputCached: modalForm.fincInputCached,
            fincOutput: modalForm.fincOutput,
            testTtfb: modalForm.testTtfb,
            testRate: modalForm.testRate,
            testTime: modalForm.testTime,
            remark: modalForm.remark,
            seq: modalForm.seq,
            status: modalForm.status,
          };
          await ModelApi.addModel(addDto);
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
          const editDto: EditModelDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            maxContext: modalForm.maxContext,
            maxOutputToken: modalForm.maxOutputToken,
            apiReasoning: modalForm.apiReasoning,
            apiReasoningEffort: modalForm.apiReasoningEffort,
            apiAppendParam: modalForm.apiAppendParam,
            apiAppendHeaders: modalForm.apiAppendHeaders,
            fincInput: modalForm.fincInput,
            fincInputCached: modalForm.fincInputCached,
            fincOutput: modalForm.fincOutput,
            testTtfb: modalForm.testTtfb,
            testRate: modalForm.testRate,
            testTime: modalForm.testTime,
            remark: modalForm.remark,
            seq: modalForm.seq,
            status: modalForm.status,
          };
          await ModelApi.editModel(editDto);
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
