import { ref, computed, reactive, nextTick, type Ref } from "vue";
import type { TableInstance } from "element-plus";
import type {
  GetGroupListVo,
  GetGroupPermissionMenuViewVo,
  GetGroupPermissionNodeVo,
  GetGroupPermissionNodeDto,
  GetGroupDetailsVo,
} from "@/views/auth/api/GroupApi.ts";
import GroupApi from "@/views/auth/api/GroupApi.ts";
import { Result } from "@/commons/model/Result.ts";

export default {
  /**
   * 用户组权限模态框tab打包
   */
  useGpTab(_loadListByTab: () => Promise<void>) {
    const tab = ref<"node" | "menu" | "rowScope">("menu");
    return {
      tab,
    };
  },

  /**
   * 用户组权限菜单视图列表打包
   */
  useGpMenuViewList(props: { visible: boolean; row?: GetGroupListVo | null }, listTableRef: Ref<TableInstance | undefined>) {
    const listData = ref<GetGroupPermissionMenuViewVo[]>([]);
    const listSelected = ref<GetGroupPermissionMenuViewVo[]>([]);
    const listLoading = ref(true);
    const menuFilterKeyword = ref("");
    const menuFilterHasPermission = ref<number | null>(null);
    const groupDetails = ref<GetGroupDetailsVo | null>(null);

    /**
     * 递归计算菜单树中的所有节点数量
     */
    const countMenuNodes = (menus: GetGroupPermissionMenuViewVo[]): number => {
      if (!menus || menus.length === 0) {
        return 0;
      }
      let count = 0;
      for (const menu of menus) {
        count++;
        if (menu.children && menu.children.length > 0) {
          count += countMenuNodes(menu.children);
        }
      }
      return count;
    };

    /**
     * 菜单视图总数量（递归计算）
     */
    const menuTotalCount = computed(() => {
      return countMenuNodes(listData.value);
    });

    /**
     * 加载菜单列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await GroupApi.getGroupPermissionMenuView({
        groupId: props.row?.id || "",
        keyword: menuFilterKeyword.value || undefined,
        hasPermission: menuFilterHasPermission.value,
      });
      listData.value = result;
      listLoading.value = false;
    };

    /**
     * 重置菜单列表
     */
    const resetList = (): void => {
      menuFilterKeyword.value = "";
      menuFilterHasPermission.value = null;
      loadList();
    };

    /**
     * 判断菜单行是否可选择
     * kind 为 0（目录）或 permissionCode 为空的行不可选择
     */
    const isMenuRowSelectable = (row: GetGroupPermissionMenuViewVo): boolean => {
      if (row.kind === 0) {
        return false;
      }
      if (!row.permissionCode) {
        return false;
      }
      return true;
    };

    /**
     * 获取菜单行的类名
     * 不可选择的行添加 disabled-row 类名
     */
    const getMenuRowClassName = ({ row }: { row: GetGroupPermissionMenuViewVo }): string => {
      if (!isMenuRowSelectable(row)) {
        return "disabled-row";
      }
      return "";
    };

    /**
     * 处理菜单行点击事件
     */
    const onRowClick = (row: GetGroupPermissionMenuViewVo, column: any, event: Event): void => {
      const target = event.target as HTMLElement;
      if (target.closest(".el-checkbox") || target.closest(".el-table-column--selection")) {
        return;
      }

      // 如果行不可选择，直接返回
      if (!isMenuRowSelectable(row)) {
        return;
      }

      const isSelected = listSelected.value.some((item) => item.id === row.id);
      listTableRef.value?.toggleRowSelection(row, !isSelected);
    };

    /**
     * 清空选择
     */
    const clearSelection = (): void => {
      listSelected.value = [];
      listTableRef.value?.clearSelection();
    };

    /**
     * 监听筛选关键字变化，清空选择
     */
    const onFilterKeywordChange = (): void => {
      if (groupDetails.value) {
        nextTick(() => {
          listTableRef.value?.clearSelection();
        });
      }
    };

    return {
      listData,
      listSelected,
      listLoading,
      menuFilterKeyword,
      menuFilterHasPermission,
      groupDetails,
      menuTotalCount,
      loadList,
      resetList,
      isMenuRowSelectable,
      getMenuRowClassName,
      onRowClick,
      clearSelection,
      countMenuNodes,
      onFilterKeywordChange,
    };
  },

  /**
   * 用户组权限节点视图列表打包
   */
  useGpNodeViewList(
    props: { visible: boolean; row?: GetGroupListVo | null },
    listNodeTableRef: Ref<TableInstance | undefined>
  ) {
    const listNodeData = ref<GetGroupPermissionNodeVo[]>([]);
    const listNodeSelected = ref<GetGroupPermissionNodeVo[]>([]);
    const listNodeSelectedGlobal = ref<Set<string>>(new Set());
    const listNodeLoading = ref(false);
    const listNodeTotal = ref(0);
    const listNodeForm = reactive<GetGroupPermissionNodeDto>({
      groupId: props.row?.id || "",
      keyword: null,
      hasPermission: null,
      pageNum: 1,
      pageSize: 20,
    });

    /**
     * 加载节点列表
     */
    const loadNodeList = async (): Promise<void> => {
      listNodeLoading.value = true;
      const result = await GroupApi.getGroupPermissionNodeView(listNodeForm);
      listNodeData.value = result.data;
      listNodeTotal.value = result.total;
      listNodeLoading.value = false;
    };

    /**
     * 重置节点列表
     */
    const resetNodeList = (): void => {
      listNodeForm.keyword = null;
      listNodeForm.hasPermission = null;
      listNodeForm.pageNum = 1;
      listNodeForm.pageSize = 20;
      loadNodeList();
    };

    /**
     * 处理节点行点击事件
     */
    const onNodeRowClick = (row: GetGroupPermissionNodeVo, column: any, event: Event): void => {
      const target = event.target as HTMLElement;
      if (target.closest(".el-checkbox") || target.closest(".el-table-column--selection")) {
        return;
      }

      const isSelected = listNodeSelected.value.some((item) => item.id === row.id);
      listNodeTableRef.value?.toggleRowSelection(row, !isSelected);
    };

    /**
     * 清空选择
     */
    const clearSelection = (): void => {
      listNodeSelected.value = [];
      listNodeTableRef.value?.clearSelection();
      listNodeSelectedGlobal.value.clear();
    };

    /**
     * 更新分组ID
     */
    const updateGroupId = (groupId: string): void => {
      listNodeForm.groupId = groupId;
    };

    return {
      listNodeData,
      listNodeSelected,
      listNodeSelectedGlobal,
      listNodeLoading,
      listNodeTotal,
      listNodeForm,
      loadNodeList,
      resetNodeList,
      onNodeRowClick,
      clearSelection,
      updateGroupId,
    };
  },

  /**
   * 用户组权限操作打包（授权/取消授权）
   */
  useGpPermissionOperations(
    props: { visible: boolean; row?: GetGroupListVo | null },
    tab: Ref<"node" | "menu" | "rowScope">,
    menuListSelected: Ref<GetGroupPermissionMenuViewVo[]>,
    nodeListSelected: Ref<GetGroupPermissionNodeVo[]>,
    loadListByTab: () => Promise<void>
  ) {
    /**
     * 获取选中的权限代码
     */
    const getSelectedPermissionCodes = (row?: GetGroupPermissionMenuViewVo | GetGroupPermissionNodeVo): string[] => {
      if (!row) {
        const permissionCodes: string[] = [];
        if (tab.value === "menu") {
          for (const item of menuListSelected.value) {
            if (!item.permissionCode) {
              continue;
            }

            if (item.permissionCode.includes(";")) {
              permissionCodes.push(...item.permissionCode.split(";").filter((p) => p.trim() !== ""));
            }

            if (!item.permissionCode.includes(";")) {
              permissionCodes.push(item.permissionCode);
            }
          }
        }
        if (tab.value === "node") {
          for (const item of nodeListSelected.value) {
            permissionCodes.push(item.code);
          }
        }
        return permissionCodes;
      }

      // 根据row的类型获取权限代码
      if ("code" in row) {
        // GetGroupPermissionNodeVo类型，直接返回code
        return [row.code];
      }

      // GetGroupPermissionMenuViewVo类型，处理permissionCode字段
      if ("permissionCode" in row) {
        const permissionCode = row.permissionCode;
        if (!permissionCode) {
          return [];
        }

        if (permissionCode.includes(";")) {
          return permissionCode.split(";").filter((p) => p.trim() !== "");
        }

        return [permissionCode];
      }

      return [];
    };

    /**
     * 授权或取消授权
     * @param row 菜单或节点对象
     * @param type 0:授权 1:取消授权
     */
    const grandAndRevoke = async (
      row: GetGroupPermissionMenuViewVo | GetGroupPermissionNodeVo,
      type: number,
      ElMessage: any
    ): Promise<void> => {
      const permissionCodes = getSelectedPermissionCodes(row);
      if (permissionCodes.length === 0) {
        ElMessage.warning("没有可操作的权限");
        return;
      }
      const result = await GroupApi.grantAndRevoke({
        groupId: props.row?.id || "",
        permissionCodes: permissionCodes,
        type: type,
      });
      if (Result.isSuccess(result)) {
        ElMessage.success(type === 0 ? "授权成功" : "取消授权成功");
        loadListByTab();
      }
    };

    /**
     * 批量授权
     */
    const batchGrant = async (ElMessage: any): Promise<void> => {
      const permissionCodes = getSelectedPermissionCodes();
      const result = await GroupApi.grantAndRevoke({
        groupId: props.row?.id || "",
        permissionCodes: permissionCodes,
        type: 0,
      });
      if (Result.isSuccess(result)) {
        ElMessage.success("批量授权成功");
        loadListByTab();
      }
    };

    /**
     * 批量取消授权
     */
    const batchRevoke = async (ElMessage: any): Promise<void> => {
      const permissionCodes = getSelectedPermissionCodes();
      const result = await GroupApi.grantAndRevoke({
        groupId: props.row?.id || "",
        permissionCodes: permissionCodes,
        type: 1,
      });
      if (Result.isSuccess(result)) {
        ElMessage.success("批量取消授权成功");
        loadListByTab();
      }
    };

    return {
      getSelectedPermissionCodes,
      grandAndRevoke,
      batchGrant,
      batchRevoke,
    };
  },
};
