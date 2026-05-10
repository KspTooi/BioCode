import { ref } from "vue";
import type { GetUserListDto, GetUserListVo } from "@/views/core/api/UserApi";
import AdminUserApi from "@/views/core/api/UserApi";
import { Result } from "@/commons/model/Result";
import { ElMessage } from "element-plus";

export default {
  /**
   * 用户选择器列表逻辑
   */
  useUserSelect(initialUserId?: string | string[] | null, multiple?: boolean) {
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
    const selectedUser = ref<GetUserListVo | null>(null);
    const displayValue = ref("");

    const loadList = async (orgId?: string | null): Promise<void> => {
      listForm.value.orgId = orgId ?? null;
      listLoading.value = true;

      const result = await AdminUserApi.getUserList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
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
      listForm.value.status = null;
      loadList(listForm.value.orgId);
    };

    const loadUserById = async (userId: string): Promise<GetUserListVo | null> => {
      if (!userId) {
        return null;
      }

      const result = await AdminUserApi.getUserList({
        pageNum: 1,
        pageSize: 1000,
        username: "",
        status: null,
        orgId: null,
      });

      if (Result.isSuccess(result)) {
        const user = result.data.find((u) => u.id === userId);
        return user || null;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      return null;
    };

    const loadUsersByIds = async (userIds: string[]): Promise<GetUserListVo[]> => {
      if (!userIds || userIds.length === 0) {
        return [];
      }

      const result = await AdminUserApi.getUserList({
        pageNum: 1,
        pageSize: 1000,
        username: "",
        status: null,
        orgId: null,
      });

      if (Result.isSuccess(result)) {
        return result.data.filter((u) => userIds.includes(u.id));
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      return [];
    };

    if (initialUserId) {
      if (multiple && Array.isArray(initialUserId)) {
        loadUsersByIds(initialUserId).then((users) => {
          if (users.length > 0) {
            displayValue.value = users.map((u) => `${u.username} (${u.nickname || "-"})`).join(", ");
          }
        });
      }

      if (!multiple && typeof initialUserId === "string") {
        loadUserById(initialUserId).then((user) => {
          if (user) {
            selectedUser.value = user;
            displayValue.value = `${user.username} (${user.nickname || "-"})`;
          }
        });
      }
    }

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      selectedUser,
      displayValue,
    };
  },
};
