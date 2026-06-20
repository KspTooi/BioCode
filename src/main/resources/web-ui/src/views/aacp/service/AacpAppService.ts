import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAacpAppListDto,
  GetAacpAppListVo,
  GetAacpAppDetailsVo,
  AddAacpAppDto,
  EditAacpAppDto,
} from "@/views/aacp/api/AacpAppApi.ts";
import AacpAppApi from "@/views/aacp/api/AacpAppApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * AACP应用列表管理
   */
  useAacpAppList() {
    const listForm = ref<GetAacpAppListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      status: null,
    });

    const listData = ref<GetAacpAppListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AacpAppApi.getAacpAppList(listForm.value);

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
    const removeList = async (row: GetAacpAppListVo): Promise<void> => {
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
        await AacpAppApi.removeAacpApp({ id: row.id });
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
  useAacpAppModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpAppDetailsVo>({
      id: "",
      name: "",
      code: "",
      appKey: "",
      isPublic: 0,
      ips: "",
      remark: "",
      status: 0,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入应用名称", trigger: "blur" },
        { max: 40, message: "应用名称长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入应用代码", trigger: "blur" },
        { max: 16, message: "应用代码长度不能超过16个字符", trigger: "blur" },
      ],
      isPublic: [{ required: true, message: "请输入是否公开 0:不公开 1:公开", trigger: "blur" }],
      ips: [{ required: true, message: "请输入IP白名单列表", trigger: "blur" }],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      status: [{ required: true, message: "请输入状态 0:禁用 1:启用", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetAacpAppListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.name = "";
        modalForm.code = "";
        modalForm.appKey = "";
        modalForm.isPublic = 0;
        modalForm.ips = "";
        modalForm.remark = "";
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
          const details = await AacpAppApi.getAacpAppDetails({ id: row.id });
          modalForm.id = row.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.appKey = details.appKey;
          modalForm.isPublic = details.isPublic;
          modalForm.ips = details.ips;
          modalForm.remark = details.remark;
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
      modalForm.appKey = "";
      modalForm.isPublic = 0;
      modalForm.ips = "";
      modalForm.remark = "";
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
          const addDto: AddAacpAppDto = {
            name: modalForm.name,
            code: modalForm.code,
            isPublic: modalForm.isPublic,
            ips: modalForm.ips,
            remark: modalForm.remark,
            status: modalForm.status,
          };
          await AacpAppApi.addAacpApp(addDto);
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
          const editDto: EditAacpAppDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            isPublic: modalForm.isPublic,
            ips: modalForm.ips,
            remark: modalForm.remark,
            status: modalForm.status,
          };
          await AacpAppApi.editAacpApp(editDto);
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
