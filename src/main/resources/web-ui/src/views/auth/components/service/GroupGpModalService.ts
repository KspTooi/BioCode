import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { Result } from "@/commons/model/Result";
import type { GetGroupListVo } from "@/views/auth/api/GroupApi.ts";
import GroupApi from "@/views/auth/api/GroupApi.ts";
import PermissionApi from "@/views/auth/api/PermissionApi.ts";

export interface GroupGpModalProps {
  visible: boolean;
  data?: GetGroupListVo | null;
}

export interface GpPermissionItem {
  id: string;
  code: string;
  name: string;
}

import { SA_CODE } from "@/views/auth/service/UserAuthService.ts";

export default {
  useGroupGpModal(props: GroupGpModalProps, emit: (e: "close" | "success") => void) {
    const modalLoading = ref(false);
    const modalPermissionList = ref<GpPermissionItem[]>([]);
    const modalSearch = ref("");
    const modalSelectedIds = ref<string[]>([]);
    const isSystemGroup = ref(false);

    const modalFilteredPermissions = computed(() => {
      const s = modalSearch.value.toLowerCase().trim();
      if (!s) {
        return modalPermissionList.value;
      }
      return modalPermissionList.value.filter((p) => p.name.toLowerCase().includes(s) || p.code.toLowerCase().includes(s));
    });

    const modalSelectedCount = computed(() => modalSelectedIds.value.length);

    /**
     * 加载权限定义及组已绑定的权限
     */
    const openModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
        isSystemGroup.value = props.data.isSystem === 1;

        const [definitionResult, detailsResult] = await Promise.all([
          PermissionApi.getPermissionDefinition(),
          GroupApi.getGroupDetails({ id: props.data.id }),
        ]);
        modalPermissionList.value = definitionResult.map((p) => ({
          id: p.id,
          code: p.code,
          name: p.name,
        }));
        modalSelectedIds.value = (detailsResult.permissionIds ?? []).map((id) => String(id));
      } catch (error: any) {
        ElMessage.error(error.message || "加载权限数据失败");
      } finally {
        modalLoading.value = false;
      }
    };

    /**
     * 全选当前过滤结果
     */
    const selectAll = (): void => {
      modalSelectedIds.value = modalFilteredPermissions.value.map((p) => p.id);
    };

    /**
     * 清空选择
     */
    const deselectAll = (): void => {
      modalSelectedIds.value = [];
    };

    /**
     * 提交权限绑定
     */
    const submitModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }

      // 系统内置组不允许去除SA权限
      if (isSystemGroup.value) {
        const saPermission = modalPermissionList.value.find((p) => p.code === SA_CODE);
        if (saPermission && !modalSelectedIds.value.includes(saPermission.id)) {
          ElMessage.error("系统内置组不允许去除超级操作权限(SA)");
          return;
        }
      }

      modalLoading.value = true;
      try {
        const result = await GroupApi.updateGroupGp({
          groupId: props.data.id,
          permissionIds: modalSelectedIds.value,
        });
        if (Result.isError(result)) {
          ElMessage.error(result.message || "保存失败");
          return;
        }
        ElMessage.success("组权限已保存");
        emit("close");
        emit("success");
      } catch (error: any) {
        ElMessage.error(error.message || "保存失败");
      } finally {
        modalLoading.value = false;
      }
    };

    /**
     * 重置状态
     */
    const resetModal = (): void => {
      modalPermissionList.value = [];
      modalSelectedIds.value = [];
      modalSearch.value = "";
      isSystemGroup.value = false;
      modalLoading.value = false;
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
      modalLoading,
      modalPermissionList,
      modalSearch,
      modalSelectedIds,
      modalFilteredPermissions,
      modalSelectedCount,
      isSystemGroup,
      selectAll,
      deselectAll,
      submitModal,
    };
  },
};
