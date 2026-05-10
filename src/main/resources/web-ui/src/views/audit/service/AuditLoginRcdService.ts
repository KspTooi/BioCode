import { onMounted, reactive, ref } from "vue";
import type { GetAuditLoginListDto, GetAuditLoginListVo, GetAuditLoginDetailsVo } from "@/views/audit/api/AuditLoginApi.ts";
import AuditLoginApi from "@/views/audit/api/AuditLoginApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

type ModalMode = "view";

export default {
  /**
   * 登录审计列表打包
   */
  useAuditLoginList() {
    const listForm = ref<GetAuditLoginListDto>({
      pageNum: 1,
      pageSize: 20,
      username: "",
      status: null,
    });

    const listData = ref<GetAuditLoginListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AuditLoginApi.getAuditLoginList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("audit-login-rcd", listForm.value);
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
      listForm.value.username = "";
      listForm.value.status = null;
      QueryPersistService.clearQuery("audit-login-rcd");
      loadList();
    };

    /**
     * 删除日志
     */
    const removeList = async (row: GetAuditLoginListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该条审计日志吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AuditLoginApi.removeAuditLogin({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 批量删除日志
     */
    const removeListBatch = async (selectedItems: GetAuditLoginListVo[]): Promise<void> => {
      if (selectedItems.length === 0) {
        ElMessage.warning("请选择要删除的审计日志");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定删除选中的${selectedItems.length}条审计日志吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        const ids = selectedItems.map((item) => item.id);
        await AuditLoginApi.removeAuditLogin({ ids });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery("audit-login-rcd", listForm.value);
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
      removeListBatch,
    };
  },

  /**
   * 登录日志查看模态框
   */
  useAuditLoginModal() {
    const modalVisible = ref(false);
    const modalMode = ref<ModalMode>("view");
    const modalForm = reactive<GetAuditLoginDetailsVo>({
      id: "",
      userId: 0,
      username: "",
      loginKind: 0,
      ipAddr: "",
      location: "",
      browser: "",
      os: "",
      status: 0,
      message: "",
      createTime: "",
    });

    const openModal = async (mode: ModalMode, row: GetAuditLoginListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (!row) {
        ElMessage.error("未选择要查看的数据");
        return;
      }

      try {
        const details = await AuditLoginApi.getAuditLoginDetails({ id: row.id });
        modalForm.id = details.id;
        modalForm.userId = details.userId;
        modalForm.username = details.username;
        modalForm.loginKind = details.loginKind;
        modalForm.ipAddr = details.ipAddr;
        modalForm.location = details.location;
        modalForm.browser = details.browser;
        modalForm.os = details.os;
        modalForm.status = details.status;
        modalForm.message = details.message;
        modalForm.createTime = details.createTime;
        modalVisible.value = true;
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    return {
      modalVisible,
      modalMode,
      modalForm,
      openModal,
    };
  },
};
