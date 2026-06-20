import { onMounted, reactive, ref, watch, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import RelayServerApi from "@/views/relay/api/RelayServerApi.ts";
import type {
  GetRelayServerListDto,
  GetRelayServerListVo,
  GetRelayServerDetailsVo,
  RelayServerRouteRuleVo,
  GetRelayServerRouteStateVo,
} from "@/views/relay/api/RelayServerApi.ts";
import RouteRuleApi from "@/views/relay/api/RouteRuleApi.ts";
import type { GetRouteRuleListVo } from "@/views/relay/api/RouteRuleApi.ts";
import { Result } from "@/commons/model/Result.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

type ModalMode = "add" | "edit";

export default {
  useRelayServerList() {
    const listForm = reactive<GetRelayServerListDto>({
      name: null,
      forwardUrl: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetRelayServerListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await RelayServerApi.getRelayServerList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
        QueryPersistService.persistQuery("relay-server-manager", listForm);
      } catch {
        ElMessage.error("加载中继通道列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.name = null;
      listForm.forwardUrl = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      loadList();
      QueryPersistService.clearQuery("relay-server-manager");
    };

    const removeList = async (row: GetRelayServerListVo): Promise<void> => {
      if (row.status === 2) {
        ElMessage.error("无法删除一个正在运行中的中继通道");
        return;
      }

      try {
        await ElMessageBox.confirm("确定删除中继通道 [" + row.name + "] 吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await RelayServerApi.removeRelayServer(row.id.toString());
        ElMessage.success("删除中继通道成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const startRelayServer = async (row: GetRelayServerListVo): Promise<void> => {
      try {
        await RelayServerApi.startRelayServer(row.id.toString());
        ElMessage.success("启动中继通道成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const stopRelayServer = async (row: GetRelayServerListVo): Promise<void> => {
      try {
        await RelayServerApi.stopRelayServer(row.id.toString());
        ElMessage.success("停止中继通道成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery("relay-server-manager", listForm);
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
      startRelayServer,
      stopRelayServer,
    };
  },

  useRelayServerModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalRouteRuleData = ref<GetRouteRuleListVo[]>([]);
    const modalForm = reactive<GetRelayServerDetailsVo>({
      id: null,
      name: null,
      host: null,
      port: null,
      forwardType: 0,
      routeRules: [] as RelayServerRouteRuleVo[],
      forwardUrl: null,
      autoStart: null,
      status: null,
      errorMessage: null,
      overrideRedirect: null,
      overrideRedirectUrl: null,
      requestIdStrategy: null,
      requestIdHeaderName: null,
      bizErrorStrategy: null,
      bizErrorCodeField: null,
      bizSuccessCodeValue: null,
      createTime: null,
    });

    const modalRules: FormRules = {
      name: [{ required: true, message: "请输入中继通道名称", trigger: "blur" }],
      host: [
        { required: true, message: "请输入中继服务器主机", trigger: "blur" },
        { pattern: /^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$/, message: "主机名必须为有效IP地址", trigger: "blur" },
      ],
      port: [
        { required: true, message: "请输入中继服务器端口", trigger: "blur" },
        { pattern: /^[0-9]{1,5}$/, message: "端口必须为1-65535之间的整数", trigger: "blur" },
      ],
      forwardType: [{ required: true, message: "请选择桥接目标类型", trigger: "blur" }],
      routeRules: [
        {
          validator: (rule: any, value: any, callback: (error?: Error) => void) => {
            if (modalForm.forwardType === 1 && value.length === 0) {
              callback(new Error("请选择路由规则"));
              return;
            }
            callback();
          },
          trigger: "change",
        },
      ],
      forwardUrl: [
        {
          validator: (rule: any, value: any, callback: (error?: Error) => void) => {
            if (modalForm.forwardType !== 0) {
              callback();
              return;
            }
            if (!value) {
              callback(new Error("请输入桥接目标URL"));
              return;
            }
            if (!/^https?:\/\/[^\s/$.?#].[^\s]*$/i.test(value)) {
              callback(new Error("桥接目标URL必须为有效URL"));
              return;
            }
            callback();
          },
          trigger: "blur",
        },
      ],
      autoStart: [{ required: true, message: "请选择是否自动运行", trigger: "blur" }],
      requestIdStrategy: [{ required: true, message: "请选择请求ID策略", trigger: "blur" }],
      requestIdHeaderName: [
        {
          validator: (rule: any, value: any, callback: (error?: Error) => void) => {
            if (modalForm.requestIdStrategy === 1 && !value) {
              callback(new Error("当请求ID策略为从请求头获取时，请求ID头名称不能为空"));
              return;
            }
            callback();
          },
          trigger: "blur",
        },
      ],
      bizErrorStrategy: [{ required: true, message: "请选择业务错误策略", trigger: "blur" }],
      bizErrorCodeField: [
        {
          validator: (rule: any, value: any, callback: (error?: Error) => void) => {
            if (modalForm.bizErrorStrategy === 1 && !value) {
              callback(new Error("当业务错误策略为业务错误码决定时，业务错误码字段不能为空"));
              return;
            }
            callback();
          },
          trigger: "blur",
        },
      ],
      bizSuccessCodeValue: [
        {
          validator: (rule: any, value: any, callback: (error?: Error) => void) => {
            if (modalForm.bizErrorStrategy === 1 && !value) {
              callback(new Error("当业务错误策略为业务错误码决定时，业务错误码值不能为空"));
              return;
            }
            callback();
          },
          trigger: "blur",
        },
      ],
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.host = "0.0.0.0";
      modalForm.port = 8080;
      modalForm.forwardType = 0;
      modalForm.routeRules = [];
      modalForm.forwardUrl = null;
      modalForm.autoStart = 1;
      modalForm.status = null;
      modalForm.errorMessage = null;
      modalForm.overrideRedirect = 0;
      modalForm.overrideRedirectUrl = null;
      modalForm.requestIdStrategy = 0;
      modalForm.requestIdHeaderName = null;
      modalForm.bizErrorStrategy = null;
      modalForm.bizErrorCodeField = null;
      modalForm.bizSuccessCodeValue = null;
      modalForm.createTime = null;
    };

    const openModal = async (mode: ModalMode, row: GetRelayServerListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "edit" && row) {
        if (row.status === 2) {
          ElMessage.error("无法修改一个正在运行中的中继通道");
          return;
        }

        try {
          const res = await RelayServerApi.getRelayServerDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.host = res.host;
          modalForm.port = res.port;
          modalForm.forwardType = res.forwardType;
          modalForm.routeRules = res.routeRules;
          modalForm.forwardUrl = res.forwardUrl;
          modalForm.autoStart = res.autoStart;
          modalForm.status = res.status;
          modalForm.errorMessage = res.errorMessage;
          modalForm.overrideRedirect = res.overrideRedirect;
          modalForm.overrideRedirectUrl = res.overrideRedirectUrl;
          modalForm.requestIdStrategy = res.requestIdStrategy;
          modalForm.requestIdHeaderName = res.requestIdHeaderName;
          modalForm.bizErrorStrategy = res.bizErrorStrategy;
          modalForm.bizErrorCodeField = res.bizErrorCodeField;
          modalForm.bizSuccessCodeValue = res.bizSuccessCodeValue;
          modalForm.createTime = res.createTime;
        } catch (error: any) {
          ElMessage.error(error.message);
          return;
        }
      }

      const ruleRes = await RouteRuleApi.getRouteRuleList({ pageNum: 1, pageSize: 100000 });
      if (Result.isSuccess(ruleRes)) {
        modalRouteRuleData.value = ruleRes.data;
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
          await RelayServerApi.addRelayServer({
            name: modalForm.name,
            host: modalForm.host,
            port: modalForm.port,
            forwardType: modalForm.forwardType,
            routeRules: modalForm.routeRules,
            forwardUrl: modalForm.forwardUrl,
            autoStart: modalForm.autoStart,
            overrideRedirect: modalForm.overrideRedirect,
            overrideRedirectUrl: modalForm.overrideRedirectUrl,
            requestIdStrategy: modalForm.requestIdStrategy,
            requestIdHeaderName: modalForm.requestIdHeaderName,
            bizErrorStrategy: modalForm.bizErrorStrategy,
            bizErrorCodeField: modalForm.bizErrorCodeField,
            bizSuccessCodeValue: modalForm.bizSuccessCodeValue,
          });
          ElMessage.success("添加中继通道成功");
          resetModal();
        }

        if (modalMode.value === "edit") {
          await RelayServerApi.editRelayServer({
            id: modalForm.id,
            name: modalForm.name,
            host: modalForm.host,
            port: modalForm.port,
            forwardType: modalForm.forwardType,
            routeRules: modalForm.routeRules,
            forwardUrl: modalForm.forwardUrl,
            autoStart: modalForm.autoStart,
            overrideRedirect: modalForm.overrideRedirect,
            overrideRedirectUrl: modalForm.overrideRedirectUrl,
            requestIdStrategy: modalForm.requestIdStrategy,
            requestIdHeaderName: modalForm.requestIdHeaderName,
            bizErrorStrategy: modalForm.bizErrorStrategy,
            bizErrorCodeField: modalForm.bizErrorCodeField,
            bizSuccessCodeValue: modalForm.bizSuccessCodeValue,
          });
          ElMessage.success("编辑中继通道成功");
        }

        reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        modalLoading.value = false;
      }
    };

    watch(
      () => modalForm.forwardType,
      (newVal: number | null) => {
        if (newVal === 0) {
          modalForm.routeRules = [];
        }
        if (newVal === 1) {
          modalForm.forwardUrl = null;
        }
      }
    );

    return {
      modalVisible,
      modalLoading,
      modalMode,
      modalForm,
      modalRules,
      modalRouteRuleData,
      openModal,
      resetModal,
      submitModal,
    };
  },

  useRouteStateModal() {
    const routeStateModalVisible = ref(false);
    const routeStateData = ref<GetRelayServerRouteStateVo[]>([]);
    const routeStateLoading = ref(false);
    const currentRelayServerId = ref<string>("");

    const _reloadRouteState = async (): Promise<void> => {
      const res = await RelayServerApi.getRelayServerRouteState(currentRelayServerId.value);
      routeStateData.value = res;
    };

    const showRouteStateModal = async (row: GetRelayServerListVo): Promise<void> => {
      routeStateLoading.value = true;
      currentRelayServerId.value = row.id.toString();

      try {
        await _reloadRouteState();
      } catch (error: any) {
        ElMessage.error(error.message);
        return;
      } finally {
        routeStateLoading.value = false;
      }

      routeStateModalVisible.value = true;
    };

    const refreshRouteState = async (): Promise<void> => {
      routeStateLoading.value = true;
      try {
        await _reloadRouteState();
        ElMessage.success("刷新成功");
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        routeStateLoading.value = false;
      }
    };

    const resetBreaker = async (row: GetRelayServerRouteStateVo): Promise<void> => {
      try {
        await RelayServerApi.resetRelayServerBreaker({
          id: currentRelayServerId.value,
          host: row.targetHost,
          port: row.targetPort,
          kind: 0,
        });
        ElMessage.success("复位熔断状态成功");
        await _reloadRouteState();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const resetAllBreaker = async (): Promise<void> => {
      try {
        await RelayServerApi.resetRelayServerBreaker({
          id: currentRelayServerId.value,
          host: null,
          port: null,
          kind: 0,
        });
        ElMessage.success("复位全部熔断状态成功");
        await _reloadRouteState();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const breakHost = async (row: GetRelayServerRouteStateVo): Promise<void> => {
      try {
        await RelayServerApi.resetRelayServerBreaker({
          id: currentRelayServerId.value,
          host: row.targetHost,
          port: row.targetPort,
          kind: 1,
        });
        ElMessage.success("置为熔断成功");
        await _reloadRouteState();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    return {
      routeStateModalVisible,
      routeStateData,
      routeStateLoading,
      showRouteStateModal,
      refreshRouteState,
      resetBreaker,
      resetAllBreaker,
      breakHost,
    };
  },
};
