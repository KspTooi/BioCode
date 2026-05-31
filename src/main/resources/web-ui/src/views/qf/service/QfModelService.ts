import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetQfModelListDto,
  GetQfModelListVo,
  GetQfModelDetailsVo,
  AddQfModelDto,
  EditQfModelDto,
} from "@/views/qf/api/QfModelApi.ts";
import QfModelApi from "@/views/qf/api/QfModelApi.ts";
import type { GetQfModelGroupListVo } from "@/views/qf/api/QfModelGroupApi.ts";
import QfModelGroupApi from "@/views/qf/api/QfModelGroupApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";
import type { GetQfBizFormListVo } from "../api/QfBizFormApi";
import QfBizFormApi from "../api/QfBizFormApi";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit" | "view";

export default {
  /**
   * 流程模型列表管理
   */
  useQfModelList() {
    const listForm = ref<GetQfModelListDto>({
      pageNum: 1,
      pageSize: 20,
      groupName: "",
      name: "",
      code: "",
      status: [0, 1], //固定查询草稿和已部署
    });

    const listData = ref<GetQfModelListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await QfModelApi.getQfModelList(listForm.value);

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
      listForm.value.groupName = "";
      listForm.value.name = "";
      listForm.value.code = "";
      listForm.value.status = [0, 1];
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetQfModelListVo): Promise<void> => {
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
        await QfModelApi.removeQfModel({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 创建新版本
     */
    const createNewVersion = async (row: GetQfModelListVo): Promise<void> => {
      try {
        //确认创建新版本
        await ElMessageBox.confirm(`确定为模型 [ ${row.name}(${row.code}) ] 创建新版本吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        listLoading.value = true;
        await QfModelApi.createNewVersionQfModel({ id: row.id });
        ElMessage.success(`已为模型[ ${row.name}(${row.code}) ]创建新版本！`);
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        listLoading.value = false;
      }
    };

    /**
     * 部署流程模型
     */
    const deployQfModel = async (row: GetQfModelListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定部署模型 [ ${row.name}(${row.code}) ] 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await QfModelApi.deployQfModel({ id: row.id });
        ElMessage.success(`已成功部署模型[ ${row.name}(${row.code}) ]！`);
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
      createNewVersion,
      deployQfModel,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useQfModelModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const groupList = ref<GetQfModelGroupListVo[]>([]);
    const modalForm = reactive<GetQfModelDetailsVo>({
      id: "",
      groupId: "",
      formId: "",
      name: "",
      code: null,
      bpmnXml: "",
      seq: 0,
    });

    //模型表单
    const modelFormList = ref<GetQfBizFormListVo[]>([]);

    /**
     * 加载分组列表
     */
    const loadGroupList = async (): Promise<void> => {
      const result = await QfModelGroupApi.getQfModelGroupList({ pageNum: 1, pageSize: 10000 });
      if (Result.isSuccess(result)) {
        groupList.value = result.data;
      }
    };

    /**
     * 加载模型表单列表
     */
    const loadModelFormList = async (): Promise<void> => {
      const result = await QfBizFormApi.getQfBizFormList({ pageNum: 1, pageSize: 10000 });
      if (Result.isSuccess(result)) {
        modelFormList.value = result.data;
      }
    };

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入模型名称", trigger: "blur" },
        { max: 80, message: "模型名称长度不能超过80个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入模型编码", trigger: "blur" },
        { max: 32, message: "模型编码长度不能超过32个字符", trigger: "blur" },
      ],
      seq: [
        { required: true, message: "请输入排序", trigger: "blur" },
        { type: "number", min: 0, message: "排序必须大于等于0", trigger: "blur" },
        { type: "number", max: 655350, message: "排序不能超过655350", trigger: "blur" },
      ],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetQfModelListVo | null): Promise<void> => {
      modalMode.value = mode;

      //加载模型分组列表
      await loadGroupList();

      //加载模型表单列表
      await loadModelFormList();

      if (mode === "add") {
        modalForm.id = "";
        modalForm.groupId = "";
        modalForm.formId = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.bpmnXml = "";
        modalForm.seq = 0;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit" || mode === "view") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await QfModelApi.getQfModelDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.groupId = details.groupId;
          modalForm.formId = details.formId ?? "";
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.bpmnXml = details.bpmnXml;
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
      modalForm.groupId = "";
      modalForm.formId = "";
      modalForm.name = "";
      modalForm.code = "";
      modalForm.bpmnXml = "";
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
          const addDto: AddQfModelDto = {
            groupId: modalForm.groupId || undefined,
            formId: modalForm.formId || undefined,
            name: modalForm.name,
            code: modalForm.code,
            seq: modalForm.seq,
          };
          await QfModelApi.addQfModel(addDto);
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
          const editDto: EditQfModelDto = {
            id: modalForm.id,
            groupId: modalForm.groupId || undefined,
            formId: modalForm.formId || undefined,
            name: modalForm.name,
            seq: modalForm.seq,
          };
          await QfModelApi.editQfModel(editDto);
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
      groupList,
      modelFormList,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
