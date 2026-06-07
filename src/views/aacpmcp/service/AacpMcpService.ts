import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAacpMcpListDto,
  GetAacpMcpListVo,
  GetAacpMcpDetailsVo,
  AddAacpMcpDto,
  EditAacpMcpDto,
} from "@/views/aacpMcp/api/AacpMcpApi.ts";
import AacpMcpApi from "@/views/aacpMcp/api/AacpMcpApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * MCP服务器列表管理
   */
  useAacpMcpList() {
    const listForm = ref<GetAacpMcpListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      networkKind: null,
      authPsk: "",
      status: null,
    });

    const listData = ref<GetAacpMcpListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AacpMcpApi.getAacpMcpList(listForm.value);

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
      listForm.value.networkKind = null;
      listForm.value.authPsk = "";
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetAacpMcpListVo): Promise<void> => {
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
        await AacpMcpApi.removeAacpMcp({ id: row.id });
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
  useAacpMcpModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpMcpDetailsVo>({
      id: "",
      name: "",
      code: "",
      networkKind: 0,
      host: "",
      port: 0,
      authKind: 0,
      authPsk: "",
      status: 0,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入服务器名称", trigger: "blur" },
        { max: 40, message: "服务器名称长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入唯一编码", trigger: "blur" },
        { max: 16, message: "唯一编码长度不能超过16个字符", trigger: "blur" },
      ],
      networkKind: [{ required: true, message: "请输入通信协议 0:HTTP+SSE 1:WS", trigger: "blur" }],
      host: [
        { required: true, message: "请输入主机", trigger: "blur" },
        { max: 45, message: "主机长度不能超过45个字符", trigger: "blur" },
      ],
      port: [{ required: true, message: "请输入端口", trigger: "blur" }],
      authKind: [{ required: true, message: "请输入鉴权类型 0:无 1:PSK", trigger: "blur" }],
      authPsk: [{ max: 2000, message: "预共享密钥长度不能超过2000个字符", trigger: "blur" }],
      status: [{ required: true, message: "请输入状态 0:离线 1:在线", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetAacpMcpListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.networkKind = 0;
        modalForm.host = "";
        modalForm.port = 0;
        modalForm.authKind = 0;
        modalForm.authPsk = "";
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
          const details = await AacpMcpApi.getAacpMcpDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.networkKind = details.networkKind;
          modalForm.host = details.host;
          modalForm.port = details.port;
          modalForm.authKind = details.authKind;
          modalForm.authPsk = details.authPsk;
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
      modalForm.id = "";
      modalForm.name = "";
      modalForm.code = "";
      modalForm.networkKind = 0;
      modalForm.host = "";
      modalForm.port = 0;
      modalForm.authKind = 0;
      modalForm.authPsk = "";
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
          const addDto: AddAacpMcpDto = {
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            host: modalForm.host,
            port: modalForm.port,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
          };
          await AacpMcpApi.addAacpMcp(addDto);
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
          const editDto: EditAacpMcpDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            host: modalForm.host,
            port: modalForm.port,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
          };
          await AacpMcpApi.editAacpMcp(editDto);
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
