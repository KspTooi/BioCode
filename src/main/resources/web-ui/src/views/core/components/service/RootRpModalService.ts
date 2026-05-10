import { nextTick, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import type { GetCoreRootListVo } from "@/views/core/api/CoreRootApi.ts";
import CoreRootApi from "@/views/core/api/CoreRootApi.ts";
import PackApi, { type GetPackListVo } from "@/views/core/api/PackApi.ts";
import { Result } from "@/commons/model/Result";

export interface RootRpModalProps {
  visible: boolean;
  data?: GetCoreRootListVo | null;
}

export default {
  /**
   * 租户菜单包绑定弹窗管理
   */
  useRootRpModal(props: RootRpModalProps, emit: (e: "close" | "success") => void) {
    const tableRef = ref<any>();
    const packList = ref<GetPackListVo[]>([]);
    const selectedPacks = ref<GetPackListVo[]>([]);
    const boundIds = ref<Set<string>>(new Set());
    const loading = ref(false);

    const searchForm = ref({
      name: "",
      code: "",
    });
    const pageNum = ref(1);
    const pageSize = ref(20);
    const total = ref(0);

    const loadPackList = async (): Promise<void> => {
      loading.value = true;
      try {
        const result = await PackApi.getPackList({
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          name: searchForm.value.name || undefined,
          code: searchForm.value.code || undefined,
        });

        if (Result.isSuccess(result)) {
          packList.value = result.data;
          total.value = result.total;
        }
        if (Result.isError(result)) {
          ElMessage.error(result.message);
        }

        // 回显勾选逻辑：根据boundIds中的包ID回显勾选表格中对应的行
        await nextTick();
        if (tableRef.value) {
          // 复制一份boundIds.value用于回显，避免在toggleRowSelection过程中被onSelectionChange影响
          const echoBoundIds = new Set(boundIds.value);
          packList.value.forEach((row) => {
            // 只用echoBoundIds做回显判断，避免受副作用影响
            tableRef.value.toggleRowSelection(row, echoBoundIds.has(String(row.id)));
          });
        }
      } finally {
        loading.value = false;
      }
    };

    const loadBoundIds = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      try {
        const details = await CoreRootApi.getCoreRootDetails({ id: props.data.id });
        boundIds.value = new Set((details.packIds ?? []).map(String));
      } catch (error: any) {
        ElMessage.error(error.message || "加载绑定数据失败");
      }
    };

    const search = (): void => {
      pageNum.value = 1;
      loadPackList();
    };

    const resetSearch = (): void => {
      searchForm.value.name = "";
      searchForm.value.code = "";
      pageNum.value = 1;
      loadPackList();
    };

    const onSelectionChange = (rows: GetPackListVo[]): void => {
      selectedPacks.value = rows;
      const currentPageIds = new Set(packList.value.map((p) => String(p.id)));
      currentPageIds.forEach((id) => boundIds.value.delete(id));
      rows.forEach((row) => boundIds.value.add(String(row.id)));
    };

    const onRowClick = (row: GetPackListVo, column: any): void => {
      if (!tableRef.value) {
        return;
      }
      if (column.type === "selection") {
        return;
      }
      tableRef.value.toggleRowSelection(row);
    };

    const openModal = async (): Promise<void> => {
      await loadBoundIds();
      await loadPackList();
    };

    const submitModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      loading.value = true;
      try {
        const packIds = Array.from(boundIds.value);
        await CoreRootApi.updateRootRp({ rootId: props.data.id, packIds });
        ElMessage.success("菜单包绑定已保存");
        emit("close");
        emit("success");
      } catch (error: any) {
        ElMessage.error(error.message || "保存失败");
      } finally {
        loading.value = false;
      }
    };

    const resetModal = (): void => {
      packList.value = [];
      selectedPacks.value = [];
      boundIds.value = new Set();
      searchForm.value.name = "";
      searchForm.value.code = "";
      pageNum.value = 1;
      pageSize.value = 20;
      total.value = 0;
      loading.value = false;
    };

    watch(
      () => props.visible,
      async (val) => {
        if (val) {
          await openModal();
          return;
        }
        resetModal();
      }
    );

    return {
      tableRef,
      packList,
      selectedPacks,
      loading,
      searchForm,
      pageNum,
      pageSize,
      total,
      search,
      resetSearch,
      loadPackList,
      onSelectionChange,
      onRowClick,
      submitModal,
    };
  },
};
