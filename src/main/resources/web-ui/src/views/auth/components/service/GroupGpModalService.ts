import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
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

export default {
  useGroupGpModal(props: GroupGpModalProps, emit: (e: "close" | "success") => void) {
    const modalLoading = ref(false);
    const modalPermissionList = ref<GpPermissionItem[]>([]);
    const modalSearch = ref("");
    const modalSelectedIds = ref<string[]>([]);

    const modalFilteredPermissions = computed(() => {
      const s = modalSearch.value.toLowerCase().trim();
      if (!s) {
        return modalPermissionList.value;
      }
      return modalPermissionList.value.filter(
        (p) => p.name.toLowerCase().includes(s) || p.code.toLowerCase().includes(s),
      );
    });

    const modalSelectedCount = computed(() => modalSelectedIds.value.length);

    const load = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
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

    const selectAll = (): void => {
      modalSelectedIds.value = modalFilteredPermissions.value.map((p) => p.id);
    };

    const deselectAll = (): void => {
      modalSelectedIds.value = [];
    };

    const submit = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      const result = await GroupApi.updateGroupGp({
        groupId: props.data.id,
        permissionIds: modalSelectedIds.value,
      });
      modalLoading.value = false;
      if (result.code != 0) {
        ElMessage.error(result.message || "保存失败");
        return;
      }
      ElMessage.success("组权限已保存");
      emit("close");
      emit("success");
    };

    const reset = (): void => {
      modalPermissionList.value = [];
      modalSelectedIds.value = [];
      modalSearch.value = "";
      modalLoading.value = false;
    };

    watch(
      () => props.visible,
      async (val) => {
        if (val) {
          await load();
          return;
        }
        reset();
      },
    );

    return {
      modalLoading,
      modalPermissionList,
      modalSearch,
      modalSelectedIds,
      modalFilteredPermissions,
      modalSelectedCount,
      selectAll,
      deselectAll,
      submit,
    };
  },
};
