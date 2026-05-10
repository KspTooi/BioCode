import { ref, type Ref } from "vue";
import type { GetUserListDto, GetUserListVo } from "@/views/core/api/UserApi";
import AdminUserApi from "@/views/core/api/UserApi";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElTable } from "element-plus";

/**
 * 用户选择器列表参数
 */
export interface ModalUserSelectorProps {
  //弹窗标题，默认为 "选择用户"
  title?: string;

  //弹窗宽度，默认为 "75%"
  width?: string | number;

  //是否支持多选用户，默认为 false
  checkMultiple?: boolean;
}

export interface ModalUserSelectorEmits {
  (e: "on-submit", data: string[]): void;
  (e: "on-close"): void;
}

export default {
  /**
   * 用户选择模态框打包
   * @param props 用户选择器列表参数
   * @param modalCurrentOrgId 当前选中组织ID
   * @param modalmodalCheckedUserIds 当前已勾选的用户IDS
   */
  useUserSelect(
    props: ModalUserSelectorProps,
    emit: ModalUserSelectorEmits,
    listRef: Ref<InstanceType<typeof ElTable>>,
    modalVisible: Ref<boolean>,
    modalCurrentOrgId: Ref<string | null>,
    modalCheckedUserIds: Ref<string[]>
  ) {
    const listForm = ref<GetUserListDto>({
      pageNum: 1,
      pageSize: 20,
      username: "",
      status: null,
      orgId: null,
    });

    const listData = ref<GetUserListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);
    const displayValue = ref("");

    /**
     * 加载用户列表
     * @param orgId 组织ID
     */
    const loadList = async (orgId?: string | null): Promise<void> => {
      listLoading.value = true;
      listForm.value.orgId = orgId ?? null;

      const result = await AdminUserApi.getUserList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        syncChecked();
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.username = "";
      listForm.value.nickname = "";
      listForm.value.status = null;
      loadList(listForm.value.orgId);
    };

    /**
     * 用户列表勾选
     * @param rows 当前所有已选行
     */
    const onListCheck = (rows: GetUserListVo[]): void => {
      if (props.checkMultiple) {
        modalCheckedUserIds.value = rows.map((row) => row.id);
        return;
      }
      if (rows.length === 0) {
        modalCheckedUserIds.value = [];
        return;
      }
      const last = rows[rows.length - 1];
      rows.slice(0, -1).forEach((row) => listRef.value?.toggleRowSelection(row, false));
      modalCheckedUserIds.value = [last.id];
    };

    /**
     * 模态框打开
     */
    const onModalOpen = (): void => {
      //重置查询表单
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.username = "";
      listForm.value.nickname = "";
      listForm.value.status = null;

      //先根据当前选中组织ID加载用户列表
      loadList(modalCurrentOrgId.value);
    };

    /**
     * 模态框关闭
     */
    const onModalClose = (): void => {
      modalVisible.value = false;
      emit("on-close");
    };

    /**
     * 模态框提交
     */
    const onModalSubmit = (): void => {
      modalVisible.value = false;
      emit("on-submit", modalCheckedUserIds.value);
    };

    /**
     * 同步已勾选用户IDS到表格
     */
    const syncChecked = (): void => {
      //当前已勾选的用户IDS长度
      const length = modalCheckedUserIds.value.length;

      //需要恢复之前的选中
      if (length > 0) {
        //多选模式下 外面传入的已选用户IDS长度大于1 不支持恢复
        if (length > 1 && !props.checkMultiple) {
          console.warn("多选模式下 外部双向绑定传入的已选用户IDS长度大于1 不支持恢复选中状态");
          return;
        }

        //直接恢复选中
        listData.value.forEach((user) => {
          if (modalCheckedUserIds.value.includes(user.id)) {
            listRef.value?.toggleRowSelection(user, true);
          }
        });
      }
    };

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      displayValue,
      loadList,
      resetList,
      onListCheck,
      onModalOpen,
      onModalClose,
      onModalSubmit,
    };
  },
};
