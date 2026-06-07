import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpMcpApi from "@/views/aacp/api/AacpMcpApi.ts";
import type {
  GetAacpMcpListDto,
  GetAacpMcpListVo,
  GetAacpMcpDetailsVo,
} from "@/views/aacp/api/AacpMcpApi.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

const PERSIST_KEY = "aacp-mcp-server";

type ModalMode = "add" | "edit";

export default {
  useAacpMcpList() {
    const listForm = reactive<GetAacpMcpListDto>({
      name: null,
      code: null,
      status: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetAacpMcpListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpMcpApi.getAacpMcpList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
        QueryPersistService.persistQuery(PERSIST_KEY, listForm);
      } catch {
        ElMessage.error("加载MCP服务器列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.name = null;
      listForm.code = null;
      listForm.status = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      loadList();
      QueryPersistService.clearQuery(PERSIST_KEY);
    };

    const removeList = async (row: GetAacpMcpListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定删除MCP服务器 [${row.name}] 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AacpMcpApi.removeAacpMcp(row.id.toString());
        ElMessage.success("删除MCP服务器成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery(PERSIST_KEY, listForm);
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

  useAacpMcpModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpMcpDetailsVo>({
      id: null,
      name: null,
      code: null,
      networkKind: null,
      host: null,
      port: null,
      authKind: null,
      authPsk: null,
      status: null,
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入服务器名称", trigger: "blur" },
        { max: 40, message: "服务器名称长度不能超过40", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入唯一编码", trigger: "blur" },
        { max: 16, message: "唯一编码长度不能超过16", trigger: "blur" },
      ],
      networkKind: [
        { required: true, message: "请选择通信协议", trigger: "change" },
      ],
      host: [
        { required: true, message: "请输入主机", trigger: "blur" },
        { max: 45, message: "主机长度不能超过45", trigger: "blur" },
      ],
      port: [
        { required: true, message: "请输入端口", trigger: "blur" },
        {
          pattern: /^([1-9]\d{0,4})$|^([1-5]\d{4})$|^(6[0-4]\d{3})$|^(65[0-4]\d{2})$|^(655[0-2]\d)$|^(6553[0-5])$/,
          message: "端口必须在1-65535之间",
          trigger: "blur",
        },
      ],
      authKind: [
        { required: true, message: "请选择鉴权类型", trigger: "change" },
      ],
      authPsk: [
        { max: 2000, message: "预共享密钥长度不能超过2000", trigger: "blur" },
      ],
      status: [
        { required: true, message: "请选择状态", trigger: "change" },
      ],
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.code = null;
      modalForm.networkKind = 0;
      modalForm.host = "0.0.0.0";
      modalForm.port = 40000;
      modalForm.authKind = 0;
      modalForm.authPsk = null;
      modalForm.status = 1;
    };

    const openModal = async (mode: ModalMode, row: GetAacpMcpListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "edit" && row) {
        try {
          const res = await AacpMcpApi.getAacpMcpDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.code = res.code;
          modalForm.networkKind = res.networkKind;
          modalForm.host = res.host;
          modalForm.port = res.port;
          modalForm.authKind = res.authKind;
          modalForm.authPsk = res.authPsk;
          modalForm.status = res.status;
        } catch (error: any) {
          ElMessage.error(error.message);
          return;
        }
      }

      modalVisible.value = true;
    };

    const submitModal = async (): Promise<void> => {
      try {
        await modalFormRef.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      try {
        if (modalMode.value === "add") {
          await AacpMcpApi.addAacpMcp({
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            host: modalForm.host,
            port: modalForm.port,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
          });
          ElMessage.success("新增MCP服务器成功");
          resetModal();
        }

        if (modalMode.value === "edit") {
          await AacpMcpApi.editAacpMcp({
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            host: modalForm.host,
            port: modalForm.port,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
          });
          ElMessage.success("编辑MCP服务器成功");
        }

        reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
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
