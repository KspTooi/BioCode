import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpAgentHubApi from "@/views/aacp/api/AacpAgentHubApi.ts";
import AacpCapApi from "@/views/aacp/api/AacpCapApi.ts";
import type { GetAgentHubListDto, GetAgentHubListVo, GetAgentHubDetailsVo } from "@/views/aacp/api/AacpAgentHubApi.ts";
import type { GetCapListVo } from "@/views/aacp/api/AacpCapApi.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

const PERSIST_KEY = "aacp-agent-hub";

type ModalMode = "add" | "edit";

export default {
  useAgentHubList() {
    const listForm = reactive<GetAgentHubListDto>({
      name: null,
      code: null,
      status: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetAgentHubListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpAgentHubApi.getAgentHubList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
        QueryPersistService.persistQuery(PERSIST_KEY, listForm);
      } catch {
        ElMessage.error("加载智能体枢纽列表失败");
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

    const removeList = async (row: GetAgentHubListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定删除智能体枢纽 [${row.name}] 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AacpAgentHubApi.removeAgentHub(row.id.toString());
        ElMessage.success("删除智能体枢纽成功");
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

  useAgentHubModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAgentHubDetailsVo>({
      id: null,
      name: null,
      code: null,
      networkKind: null,
      authKind: null,
      authPsk: null,
      status: null,
      capIds: [],
    });

    // 能力包选择器
    const capOptions = ref<GetCapListVo[]>([]);
    const capLoading = ref(false);

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入枢纽名称", trigger: "blur" },
        { max: 40, message: "枢纽名称长度不能超过40", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入唯一编码", trigger: "blur" },
        { max: 16, message: "唯一编码长度不能超过16", trigger: "blur" },
      ],
      networkKind: [{ required: true, message: "请选择通信协议", trigger: "change" }],
      authKind: [{ required: true, message: "请选择鉴权类型", trigger: "change" }],
      authPsk: [
        { max: 2000, message: "预共享密钥长度不能超过2000", trigger: "blur" },
        { required: true, message: "请输入预共享密钥", trigger: "blur" },
      ],
      status: [{ required: true, message: "请选择状态", trigger: "change" }],
      capIds: [
        {
          validator: (_rule, _value, callback) => {
            if (modalForm.capIds.length > 50) {
              callback(new Error("一个智能体枢纽最多绑定50个能力包"));
              return;
            }
            callback();
          },
          trigger: "change",
        },
      ],
    };

    /** 一次全量加载能力包选项（pageSize 写死 10000） */
    const loadCapOptions = async (): Promise<void> => {
      if (capOptions.value.length > 0) {
        return;
      }
      capLoading.value = true;
      try {
        const res = await AacpCapApi.getCapList({
          name: null,
          kind: null,
          pageNum: 1,
          pageSize: 10000,
        });
        capOptions.value = res.data;
      } catch {
        ElMessage.error("加载能力包列表失败");
      } finally {
        capLoading.value = false;
      }
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.code = null;
      modalForm.networkKind = 0;
      modalForm.authKind = 0;
      modalForm.authPsk = null;
      modalForm.status = 1;
      modalForm.capIds = [];
      capOptions.value = [];
    };

    const openModal = async (mode: ModalMode, row: GetAgentHubListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      await loadCapOptions();

      if (mode === "edit" && row) {
        try {
          const res = await AacpAgentHubApi.getAgentHubDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.code = res.code;
          modalForm.networkKind = res.networkKind;
          modalForm.authKind = res.authKind;
          modalForm.authPsk = res.authPsk;
          modalForm.status = res.status;
          modalForm.capIds = res.capIds;
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
          await AacpAgentHubApi.addAgentHub({
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
            capIds: modalForm.capIds,
          });
          ElMessage.success("新增智能体枢纽成功");
        }

        if (modalMode.value === "edit") {
          await AacpAgentHubApi.editAgentHub({
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            networkKind: modalForm.networkKind,
            authKind: modalForm.authKind,
            authPsk: modalForm.authPsk,
            status: modalForm.status,
            capIds: modalForm.capIds,
          });
          ElMessage.success("编辑智能体枢纽成功");
        }

        reloadCallback();
        modalVisible.value = false;
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
      capOptions,
      capLoading,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
