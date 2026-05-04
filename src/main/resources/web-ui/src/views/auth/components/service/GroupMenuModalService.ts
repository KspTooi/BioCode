import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import type { GetGroupListVo } from "@/views/auth/api/GroupApi.ts";
import GroupApi from "@/views/auth/api/GroupApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import type { GetMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

export interface GroupMenuModalProps {
  visible: boolean;
  data?: GetGroupListVo | null;
}

export default {
  useGroupMenuModal(props: GroupMenuModalProps, emit: (e: "close" | "success") => void) {
    const modalTreeRef = ref<InstanceType<typeof StdAdvTree>>();
    const modalTreeData = ref<GetMenuTreeVo[]>([]);
    const modalCheckedKeys = ref<(string | number)[]>([]);
    const modalCascadeCheck = ref(false);
    const modalLoading = ref(false);

    const loadAll = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      const [menuResult, groupDetails] = await Promise.all([
        MenuApi.getMenuTree({}),
        GroupApi.getGroupDetails({ id: props.data.id }),
      ]);
      if (menuResult.code == 0) {
        modalTreeData.value = menuResult.data;
      }
      const ids = groupDetails.menuIds ?? [];
      modalCheckedKeys.value = ids.map((id) => String(id));
      modalLoading.value = false;
    };

    const onCheckedKeysChange = (keys: (string | number)[]): void => {
      modalCheckedKeys.value = keys;
    };

    /**
     * 递归收集树中所有节点的 id
     */
    const collectAllKeys = (nodes: GetMenuTreeVo[]): string[] => {
      const keys: string[] = [];
      for (const node of nodes) {
        if (node.id) {
          keys.push(String(node.id));
        }
        if (node.children?.length) {
          keys.push(...collectAllKeys(node.children));
        }
      }
      return keys;
    };

    const selectAll = (): void => {
      const innerTree = modalTreeRef.value?.getTreeRef();
      if (!innerTree) {
        return;
      }
      const allKeys = collectAllKeys(modalTreeData.value);
      innerTree.setCheckedKeys(allKeys);
      modalCheckedKeys.value = allKeys;
    };

    const deselectAll = (): void => {
      const innerTree = modalTreeRef.value?.getTreeRef();
      if (!innerTree) {
        return;
      }
      innerTree.setCheckedKeys([]);
      modalCheckedKeys.value = [];
    };

    const submit = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      const menuIds = modalCheckedKeys.value.map((k) => String(k));
      const result = await GroupApi.updateGroupGm({ groupId: props.data.id, menuIds });
      modalLoading.value = false;
      if (result.code != 0) {
        ElMessage.error(result.message || "保存失败");
        return;
      }
      ElMessage.success("菜单绑定已保存");
      emit("close");
      emit("success");
    };

    const reset = (): void => {
      modalTreeData.value = [];
      modalCheckedKeys.value = [];
      modalCascadeCheck.value = false;
      modalLoading.value = false;
    };

    watch(
      () => props.visible,
      async (val) => {
        if (val) {
          await loadAll();
          return;
        }
        reset();
      }
    );

    return {
      modalTreeRef,
      modalTreeData,
      modalCheckedKeys,
      modalCascadeCheck,
      modalLoading,
      onCheckedKeysChange,
      selectAll,
      deselectAll,
      submit,
    };
  },
};
