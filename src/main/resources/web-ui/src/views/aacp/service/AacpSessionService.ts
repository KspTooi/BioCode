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
      serverCode: null as string | null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetOnlineSessionListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpSessionApi.getOnlineSessionList();
        let data = res.data;
        if (listForm.serverCode) {
          data = data.filter((item) => item.serverCode.includes(listForm.serverCode!));
        }
        listData.value = data;
        listTotal.value = data.length;
      } catch {
        ElMessage.error("加载在线会话列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.serverCode = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
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
        await AacpSessionApi.closeSession(row.sessionId);
        ElMessage.success("关闭会话成功");
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
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      removeList,
    };
  },
};
