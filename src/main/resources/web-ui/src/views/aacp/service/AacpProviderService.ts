import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetProviderListDto,
  GetProviderListVo,
  GetProviderDetailsVo,
  AddProviderDto,
  EditProviderDto,
} from "@/views/aacp/api/AacpProviderApi";
import ProviderApi from "@/views/aacp/api/AacpProviderApi";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 模型供应商列表管理
   */
  useProviderList() {
    const listForm = ref<GetProviderListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      status: null,
    });

    const listData = ref<GetProviderListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await ProviderApi.getProviderList(listForm.value);

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
    const removeList = async (row: GetProviderListVo): Promise<void> => {
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
        await ProviderApi.removeProvider({ id: row.id });
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
  useProviderModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetProviderDetailsVo>({
      id: "",
      name: "",
      code: "",
      apiKind: 0,
      apiKey: "",
      apiHost: "",
      apiUrl: "",
      proxyKind: 0,
      proxyUrl: "",
      status: 1,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入供应商名称", trigger: "blur" },
        { max: 80, message: "供应商名称长度不能超过80个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入供应商代码", trigger: "blur" },
        { max: 32, message: "供应商代码长度不能超过32个字符", trigger: "blur" },
      ],
      apiKey: [{ max: 2000, message: "接口密钥长度不能超过2000个字符", trigger: "blur" }],
      apiHost: [
        { required: true, message: "请输入接口地址", trigger: "blur" },
        { max: 512, message: "接口地址长度不能超过512个字符", trigger: "blur" },
      ],
      apiUrl: [
        { required: true, message: "请输入接口端点", trigger: "blur" },
        { max: 512, message: "接口端点长度不能超过512个字符", trigger: "blur" },
      ],
      proxyKind: [{ required: true, message: "请选择代理类型", trigger: "change" }],
      proxyUrl: [{ max: 512, message: "代理地址长度不能超过512个字符", trigger: "blur" }],
      status: [{ required: true, message: "请选择状态", trigger: "change" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetProviderListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.apiKind = 0;
        modalForm.apiKey = "";
        modalForm.apiHost = "";
        modalForm.apiUrl = "";
        modalForm.proxyKind = 0;
        modalForm.proxyUrl = "";
        modalForm.status = 1;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await ProviderApi.getProviderDetails({ id: row.id });
          modalForm.id = row.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.apiKind = details.apiKind;
          modalForm.apiHost = details.apiHost;
          modalForm.apiUrl = details.apiUrl;
          modalForm.proxyKind = details.proxyKind;
          modalForm.proxyUrl = details.proxyUrl;
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
      modalForm.apiKind = 0;
      modalForm.apiHost = "";
      modalForm.apiUrl = "";
      modalForm.proxyKind = 0;
      modalForm.proxyUrl = "";
      modalForm.status = 1;
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
          const addDto: AddProviderDto = {
            name: modalForm.name,
            code: modalForm.code,
            apiKind: modalForm.apiKind,
            apiKey: modalForm.apiKey,
            apiHost: modalForm.apiHost,
            apiUrl: modalForm.apiUrl,
            proxyKind: modalForm.proxyKind,
            proxyUrl: modalForm.proxyUrl,
            status: modalForm.status,
          };
          await ProviderApi.addProvider(addDto);
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
          const editDto: EditProviderDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            apiKind: modalForm.apiKind,
            apiKey: modalForm.apiKey,
            apiHost: modalForm.apiHost,
            apiUrl: modalForm.apiUrl,
            proxyKind: modalForm.proxyKind,
            proxyUrl: modalForm.proxyUrl,
            status: modalForm.status,
          };
          await ProviderApi.editProvider(editDto);
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
