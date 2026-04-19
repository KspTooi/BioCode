import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetQfModelDeployRcdListDto,
  GetQfModelDeployRcdListVo,
  GetQfModelDeployRcdDetailsVo,
  LaunchQfProcessDto,
} from "@/views/qf/api/QfModelDeployRcdApi.ts";
import QfModelDeployRcdApi from "@/views/qf/api/QfModelDeployRcdApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";
import type { GetQfBizFormListVo } from "@/views/qf/api/QfBizFormApi";
import QfBizFormApi from "@/views/qf/api/QfBizFormApi";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 流程模型部署历史列表管理
   */
  useQfModelDeployRcdList() {
    const listForm = ref<GetQfModelDeployRcdListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      status: null,
    });

    const listData = ref<GetQfModelDeployRcdListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await QfModelDeployRcdApi.getQfModelDeployRcdList(listForm.value);

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
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetQfModelDeployRcdListVo): Promise<void> => {
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
        await QfModelDeployRcdApi.removeQfModelDeployRcd({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 挂起流程模型部署
     */
    const suspendQfModelDeployRcd = async (row: GetQfModelDeployRcdListVo): Promise<void> => {
      try {
        await QfModelDeployRcdApi.suspendQfModelDeployRcd({ id: row.id });
        ElMessage.success("挂起成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 激活流程模型部署
     */
    const activateQfModelDeployRcd = async (row: GetQfModelDeployRcdListVo): Promise<void> => {
      try {
        await QfModelDeployRcdApi.activateQfModelDeployRcd({ id: row.id });
        ElMessage.success("激活成功");
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
      suspendQfModelDeployRcd,
      activateQfModelDeployRcd,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useQfModelDeployRcdModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetQfModelDeployRcdDetailsVo>({
      id: "",
      bpmnXml: "",
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
    const openModal = async (mode: ModalMode, row: GetQfModelDeployRcdListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.bpmnXml = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await QfModelDeployRcdApi.getQfModelDeployRcdDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.bpmnXml = details.bpmnXml;
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
      modalForm.bpmnXml = "";
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
        modalLoading.value = false;
        return;
      }

      if (modalMode.value === "edit") {
        if (!modalForm.id) {
          ElMessage.error("缺少ID参数");
          modalLoading.value = false;
          return;
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
   * 发起流程模态框管理
   */
  useLaunchModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const launchVisible = ref(false);
    const launchLoading = ref(false);

    // 当前要发起流程的行数据（用于回显模型编码）
    const launchRow = ref<GetQfModelDeployRcdListVo | null>(null);

    //流程业务表单选择数据
    const launchBizFormList = ref<GetQfBizFormListVo[]>([]);

    const launchForm = reactive<LaunchQfProcessDto>({
      code: "",
      bizFormCode: "",
      dataId: null,
    });

    const launchRules: FormRules = {
      code: [{ required: true, message: "模型编码不能为空", trigger: "blur" }],
      bizFormCode: [{ required: true, message: "业务表单不能为空", trigger: "blur" }],
      dataId: [{ required: true, message: "业务数据ID不能为空", trigger: "blur" }],
    };

    /**
     * 打开发起流程模态框
     */
    const openLaunchModal = async (row: GetQfModelDeployRcdListVo): Promise<void> => {
      launchRow.value = row;
      launchForm.code = row.code;
      launchForm.bizFormCode = "";
      launchForm.dataId = null;
      launchVisible.value = true;

      //获取流程业务表单选择数据
      const result = await QfBizFormApi.getQfBizFormList({
        pageNum: 1,
        pageSize: 50000,
        name: null,
        code: null,
        status: 0,
      });

      if (Result.isSuccess(result)) {
        launchBizFormList.value = result.data;
      }
    };

    /**
     * 关闭并重置模态框
     */
    const resetLaunchModal = (): void => {
      if (modalFormRef.value) {
        modalFormRef.value.resetFields();
      }
      launchForm.code = "";
      launchForm.bizFormCode = "";
      launchForm.dataId = null;
      launchRow.value = null;
    };

    /**
     * 提交发起流程
     */
    const submitLaunchModal = async (): Promise<void> => {
      if (!modalFormRef.value) {
        return;
      }

      try {
        await modalFormRef.value.validate();
      } catch {
        return;
      }

      launchLoading.value = true;

      try {
        const msg = await QfModelDeployRcdApi.launchQfProcess(launchForm);
        ElMessage.success(msg || "流程已发起");
        launchVisible.value = false;
        reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      }

      launchLoading.value = false;
    };

    return {
      launchVisible,
      launchLoading,
      launchRow,
      launchForm,
      launchRules,
      launchBizFormList,
      openLaunchModal,
      resetLaunchModal,
      submitLaunchModal,
    };
  },
};
