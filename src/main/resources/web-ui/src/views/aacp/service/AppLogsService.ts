import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAppLogsListDto,
  GetAppLogsListVo,
  GetAppLogsDetailsVo,
  AddAppLogsDto,
  EditAppLogsDto,
} from "@/views/aacp/api/AppLogsApi.ts";
import AppLogsApi from "@/views/aacp/api/AppLogsApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 模型调用记录列表管理
   */
  useAppLogsList() {
    const listForm = ref<GetAppLogsListDto>({
      pageNum: 1,
      pageSize: 20,
      appId: "",
      providerId: "",
      modelId: "",
      inputToken: null,
      outputToken: null,
      cost: "",
      startTime: "",
      endTime: "",
      durationMs: null,
      ttfbMs: null,
      statusCode: "",
      clientIp: "",
    });

    const listData = ref<GetAppLogsListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AppLogsApi.getAppLogsList(listForm.value);

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
      listForm.value.appId = "";
      listForm.value.providerId = "";
      listForm.value.modelId = "";
      listForm.value.inputToken = null;
      listForm.value.outputToken = null;
      listForm.value.cost = "";
      listForm.value.startTime = "";
      listForm.value.endTime = "";
      listForm.value.durationMs = null;
      listForm.value.ttfbMs = null;
      listForm.value.statusCode = "";
      listForm.value.clientIp = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetAppLogsListVo): Promise<void> => {
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
        await AppLogsApi.removeAppLogs({ id: row.id });
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
  useAppLogsModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAppLogsDetailsVo>({
      appId: "",
      providerId: "",
      modelId: "",
      inputToken: 0,
      outputToken: 0,
      cost: "",
      startTime: "",
      endTime: "",
      durationMs: 0,
      ttfbMs: 0,
      statusCode: "",
      clientIp: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetAppLogsListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.appId = "";
        modalForm.providerId = "";
        modalForm.modelId = "";
        modalForm.inputToken = 0;
        modalForm.outputToken = 0;
        modalForm.cost = "";
        modalForm.startTime = "";
        modalForm.endTime = "";
        modalForm.durationMs = 0;
        modalForm.ttfbMs = 0;
        modalForm.statusCode = "";
        modalForm.clientIp = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await AppLogsApi.getAppLogsDetails({ id: row.id });
          modalForm.appId = details.appId;
          modalForm.providerId = details.providerId;
          modalForm.modelId = details.modelId;
          modalForm.inputToken = details.inputToken;
          modalForm.outputToken = details.outputToken;
          modalForm.cost = details.cost;
          modalForm.startTime = details.startTime;
          modalForm.endTime = details.endTime;
          modalForm.durationMs = details.durationMs;
          modalForm.ttfbMs = details.ttfbMs;
          modalForm.statusCode = details.statusCode;
          modalForm.clientIp = details.clientIp;
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
      modalForm.appId = "";
      modalForm.providerId = "";
      modalForm.modelId = "";
      modalForm.inputToken = 0;
      modalForm.outputToken = 0;
      modalForm.cost = "";
      modalForm.startTime = "";
      modalForm.endTime = "";
      modalForm.durationMs = 0;
      modalForm.ttfbMs = 0;
      modalForm.statusCode = "";
      modalForm.clientIp = "";
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
          const addDto: AddAppLogsDto = {
          };
          await AppLogsApi.addAppLogs(addDto);
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
          const editDto: EditAppLogsDto = {
            id: modalForm.id,
          };
          await AppLogsApi.editAppLogs(editDto);
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
