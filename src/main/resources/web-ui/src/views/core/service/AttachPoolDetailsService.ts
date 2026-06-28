import { inject, onMounted, ref, watch, type Ref } from "vue";
import { ElMessage } from "element-plus";
import type { GetAttachListDto, GetAttachListVo } from "@/views/core/api/AttachPoolApi";
import AttachPoolApi from "@/views/core/api/AttachPoolApi";
import { Result } from "@/commons/model/Result.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

export default {
  /**
   * 附件存储池诊断列表
   */
  useAttachPoolDetailsList() {
    const listForm = ref<GetAttachListDto>({
      pageNum: 1,
      pageSize: 20,
      indexFilter: 1,
    });

    const listData = ref<GetAttachListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AttachPoolApi.getAttachList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("attach-pool-details", listForm.value);
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
      listForm.value.indexFilter = 1;
      QueryPersistService.clearQuery("attach-pool-details");
      loadList();
    };

    /**
     * 格式化字节数为可读字符串
     */
    const formatBytes = (bytes: number | undefined | null): string => {
      const val = Number(bytes);
      if (bytes == null || Number.isNaN(val) || val <= 0) {
        return "0 B";
      }
      if (val < 1024) {
        return `${val} B`;
      }
      if (val < 1024 * 1024) {
        return `${(val / 1024).toFixed(1)} KB`;
      }
      if (val < 1024 * 1024 * 1024) {
        return `${(val / 1024 / 1024).toFixed(1)} MB`;
      }
      if (val < 1024 * 1024 * 1024 * 1024) {
        return `${(val / 1024 / 1024 / 1024).toFixed(2)} GB`;
      }
      return `${(val / 1024 / 1024 / 1024 / 1024).toFixed(2)} TB`;
    };

    onMounted(async () => {
      QueryPersistService.loadQuery("attach-pool-details", listForm.value);
      await loadList();
    });

    const activeTab = inject<Ref<string>>("attachPoolActiveTab");
    watch(
      () => activeTab?.value,
      (tab, prev) => {
        if (tab !== "details") {
          return;
        }
        if (prev === undefined || prev === tab) {
          return;
        }
        loadList();
      },
    );

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      formatBytes,
    };
  },
};
