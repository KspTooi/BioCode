import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Result } from "@/commons/model/Result";
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

    /**
     * 加载菜单树及组已绑定的菜单
     */
    const openModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
        const [menuResult, groupDetails] = await Promise.all([
          MenuApi.getMenuTree({}),
          GroupApi.getGroupDetails({ id: props.data.id }),
        ]);
        if (Result.isSuccess(menuResult)) {
          modalTreeData.value = menuResult.data;
        }
        if (Result.isError(menuResult)) {
          ElMessage.error(menuResult.message);
        }
        const ids = groupDetails.menuIds ?? [];
        modalCheckedKeys.value = ids.map((id) => String(id));
      } catch (error: any) {
        ElMessage.error(error.message || "加载菜单数据失败");
      } finally {
        modalLoading.value = false;
      }
    };

    /**
     * 勾选状态变化回调
     */
    const onCheckedKeysChange = (keys: (string | number)[]): void => {
      modalCheckedKeys.value = keys;
    };

    /**
     * 全选所有菜单节点
     */
    const selectAll = (): void => {
      const innerTree = modalTreeRef.value?.getTreeRef();
      if (!innerTree) {
        return;
      }
      const allKeys = collectAllKeys(modalTreeData.value);
      innerTree.setCheckedKeys(allKeys);
      modalCheckedKeys.value = allKeys;
    };

    /**
     * 取消全选
     */
    const deselectAll = (): void => {
      const innerTree = modalTreeRef.value?.getTreeRef();
      if (!innerTree) {
        return;
      }
      innerTree.setCheckedKeys([]);
      modalCheckedKeys.value = [];
    };

    /**
     * 提交菜单绑定
     */
    const submitModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
        const menuIds = modalCheckedKeys.value.map((k) => String(k));
        const result = await GroupApi.updateGroupGm({ groupId: props.data.id, menuIds });
        if (Result.isError(result)) {
          ElMessage.error(result.message || "保存失败");
          return;
        }
        ElMessage.success("菜单绑定已保存");
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
      modalTreeData.value = [];
      modalCheckedKeys.value = [];
      modalCascadeCheck.value = false;
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
      modalTreeRef,
      modalTreeData,
      modalCheckedKeys,
      modalCascadeCheck,
      modalLoading,
      onCheckedKeysChange,
      selectAll,
      deselectAll,
      submitModal,
    };
  },
};
