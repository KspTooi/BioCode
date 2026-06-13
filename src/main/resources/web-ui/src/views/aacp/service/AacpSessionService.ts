import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpSessionApi from "@/views/aacp/api/AacpSessionApi.ts";
import type { GetOnlineSessionListVo } from "@/views/aacp/api/AacpSessionApi.ts";

export default {
  /**
   * 在线会话列表状态与方法
   */
  useOnlineSessionList() {
    const listForm = reactive({
      pageNum: 1,
      pageSize: 20,
    });

    const serverCode = ref<string | null>(null);

    const listData = ref<GetOnlineSessionListVo[]>([]);
    const listLoading = ref(false);
    const selectionRows = ref<GetOnlineSessionListVo[]>([]);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpSessionApi.getOnlineSessionList();
        let data = res.data;
        if (serverCode.value) {
          data = data.filter((item) => item.serverCode.includes(serverCode.value ?? ""));
        }
        listData.value = data;
      } catch {
        ElMessage.error("加载在线会话列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      serverCode.value = null;
      loadList();
    };

    const removeList = async (row: GetOnlineSessionListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定要关闭 [${row.serverName}] 的在线会话吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AacpSessionApi.closeSession([row.sessionId]);
        ElMessage.success("关闭会话成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const onSelectionChange = (rows: GetOnlineSessionListVo[]): void => {
      selectionRows.value = rows;
    };

    const batchClose = async (): Promise<void> => {
      if (selectionRows.value.length === 0) {
        ElMessage.warning("请先选择要关闭的会话");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定要关闭选中的 ${selectionRows.value.length} 个在线会话吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        const sessionIds = selectionRows.value.map((row) => row.sessionId);
        await AacpSessionApi.closeSession(sessionIds);
        ElMessage.success(`已关闭 ${sessionIds.length} 个会话`);
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(() => {
      loadList();
    });

    return {
      listForm,
      serverCode,
      listData,
      listLoading,
      selectionRows,
      loadList,
      resetList,
      removeList,
      onSelectionChange,
      batchClose,
    };
  },
};
