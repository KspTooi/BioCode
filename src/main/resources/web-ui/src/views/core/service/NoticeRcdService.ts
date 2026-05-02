import { computed, ref, onMounted } from "vue";
import type { GetUserNoticeRcdListDto, GetUserNoticeRcdListVo, GetNoticeRcdDetailsVo } from "@/views/core/api/NoticeRcdApi";
import NoticeRcdApi from "@/views/core/api/NoticeRcdApi";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  /**
   * 用户通知记录数量打包
   */
  useNoticeRcdCount() {
    const count = ref(0);
    const countLoading = ref(false);

    //处理后的count (超过99显示99+)
    const processedCount = computed(() => {
      return count.value > 99 ? 99 + "+" : count.value;
    });

    const loadCount = async (): Promise<void> => {
      countLoading.value = true;
      try {
        const result = await NoticeRcdApi.getUserNoticeCount();
        count.value = result;
      } catch {
        //ElMessage.error(error.message);
      } finally {
        countLoading.value = false;
      }
    };

    return {
      count,
      countLoading,
      processedCount,
      loadCount,
    };
  },

  /**
   * 用户通知记录列表打包
   */
  useNoticeRcdList() {
    const listForm = ref<GetUserNoticeRcdListDto>({
      pageNum: 1,
      pageSize: 20,
      title: undefined,
      kind: undefined,
      content: undefined,
      setRead: 0,
    });

    const listData = ref<GetUserNoticeRcdListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);
    const listSelected = ref<GetUserNoticeRcdListVo[]>([]);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;

      try {
        const result = await NoticeRcdApi.getUserNoticeRcdList(listForm.value);

        if (Result.isSuccess(result)) {
          listData.value = result.data;
          listTotal.value = result.total;
        }
      } catch {
        //ElMessage.error(error.message);
      } finally {
        listLoading.value = false;
      }
    };

    /**
     * 重置查询表单
     */
    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.title = undefined;
      listForm.value.kind = undefined;
      listForm.value.content = undefined;
      listSelected.value = [];
      loadList();
    };

    onMounted(() => {
      loadList();
    });

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      listSelected,
      loadList,
      resetList,
    };
  },

  /**
   * 用户通知记录下拉列表打包（支持懒加载）
   */
  useNoticeRcdRollingList(onCountChange?: () => void) {
    const listData = ref<GetUserNoticeRcdListVo[]>([]);
    const listLoading = ref(false);
    const pageNum = ref(1);
    const pageSize = ref(5);
    const listTotal = ref(0);

    // 是否没有更多数据
    const noMore = computed(() => {
      return listData.value.length >= listTotal.value && listTotal.value > 0;
    });

    // 是否禁用加载
    const disabled = computed(() => {
      return listLoading.value || noMore.value;
    });

    /**
     * 加载更多数据
     */
    const loadMore = async (): Promise<void> => {
      if (disabled.value) {
        return;
      }

      listLoading.value = true;
      try {
        const params: GetUserNoticeRcdListDto = {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          setRead: 1,
        };

        const result = await NoticeRcdApi.getUserNoticeRcdList(params);

        if (Result.isSuccess(result)) {
          listData.value = pageNum.value === 1 ? result.data : [...listData.value, ...result.data];
          listTotal.value = result.total;

          // 如果有数据返回，页码加1
          if (result.data.length > 0) {
            pageNum.value++;
            // 因为查询到的记录变为了已读，所以刷新未读数量
            if (onCountChange) {
              setTimeout(() => {
                onCountChange();
              }, 500);
            }
          }
        }
      } catch {
        // ElMessage.error(error.message);
      } finally {
        listLoading.value = false;
      }
    };

    /**
     * 重置列表数据
     */
    const resetList = (): void => {
      listData.value = [];
      pageNum.value = 1;
      listTotal.value = 0;
    };

    // 使用CRUD打包
    const { remove, readAll } = this.useNoticeRcdCrud({
      onRemoved: (id) => {
        // 从本地列表中移除
        listData.value = listData.value.filter((item) => item.id !== id);
        listTotal.value--;

        // 刷新未读数量
        if (onCountChange) {
          onCountChange();
        }

        // 如果删除后列表数据不足且还有更多数据，自动加载下一页补充
        if (listData.value.length < pageSize.value && !noMore.value) {
          loadMore();
        }
      },
      onReadAll: () => {
        // 重置并加载第一页
        resetList();
        loadMore();
      },
    });

    return {
      listData,
      listLoading,
      listTotal,
      noMore,
      disabled,
      loadMore,
      resetList,
      remove,
      readAll,
    };
  },

  /**
   * 用户通知记录CRUD打包
   */
  useNoticeRcdCrud(callbacks?: { onRemoved?: (id: string) => void; onBatchRemoved?: () => void; onReadAll?: () => void }) {
    /**
     * 删除通知记录
     */
    const remove = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定要删除这条通知吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        await NoticeRcdApi.removeNoticeRcd({ ids: [id] });
        ElMessage.success("删除成功");

        // 触发回调，让调用方决定如何处理
        if (callbacks?.onRemoved) {
          callbacks.onRemoved(id);
        }
      } catch (error: any) {
        // 用户取消删除时，error 为 'cancel'，不需要显示错误消息
        if (error !== "cancel") {
          ElMessage.error(error.message || "删除失败");
        }
      }
    };

    /**
     * 批量删除通知记录
     */
    const removeBatch = async (ids: string[]): Promise<void> => {
      if (!ids || ids.length === 0) {
        ElMessage.warning("请选择要删除的消息");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 条消息吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        await NoticeRcdApi.removeNoticeRcd({ ids });
        ElMessage.success("批量删除成功");

        // 触发回调，让调用方决定如何处理
        if (callbacks?.onBatchRemoved) {
          callbacks.onBatchRemoved();
        }
      } catch (error: any) {
        // 用户取消删除时，error 为 'cancel'，不需要显示错误消息
        if (error !== "cancel") {
          ElMessage.error(error.message || "批量删除失败");
        }
      }
    };

    /**
     * 全部已读
     */
    const readAll = async (): Promise<void> => {
      try {
        await NoticeRcdApi.readAllUserNoticeRcd();
        ElMessage.success("已全部标记为已读");

        // 触发回调，让调用方决定如何处理
        if (callbacks?.onReadAll) {
          callbacks.onReadAll();
        }
      } catch (error: any) {
        ElMessage.error(error.message || "操作失败");
      }
    };

    return {
      remove,
      removeBatch,
      readAll,
    };
  },

  /**
   * 用户通知记录模态框打包
   */
  useNoticeRcdModal() {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const detailsData = ref<GetNoticeRcdDetailsVo | null>(null);

    /**
     * 打开详情模态框
     */
    const openModal = async (id: string): Promise<void> => {
      modalVisible.value = true;
      modalLoading.value = true;
      detailsData.value = null;

      try {
        const result = await NoticeRcdApi.getUserNoticeRcdDetails({ id });
        detailsData.value = result;
      } catch (error: any) {
        ElMessage.error(error.message || "加载详情失败");
        modalVisible.value = false;
      } finally {
        modalLoading.value = false;
      }
    };

    /**
     * 关闭详情模态框
     */
    const closeModal = (): void => {
      modalVisible.value = false;
      detailsData.value = null;
    };

    return {
      modalVisible,
      modalLoading,
      detailsData,
      openModal,
      closeModal,
    };
  },
};
