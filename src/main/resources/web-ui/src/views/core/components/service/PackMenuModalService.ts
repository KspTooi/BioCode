import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Result } from "@/commons/model/Result";
import type { GetPackListVo } from "@/views/core/api/PackApi.ts";
import PackApi from "@/views/core/api/PackApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import type { GetMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

export interface PackMenuModalProps {
  visible: boolean;
  data?: GetPackListVo | null;
}

export default {
  usePackMenuModal(props: PackMenuModalProps, emit: (e: "close" | "success") => void) {
    const modalTreeRef = ref<InstanceType<typeof StdAdvTree>>();
    const modalTreeData = ref<GetMenuTreeVo[]>([]);
    const modalCheckedKeys = ref<(string | number)[]>([]);
    const modalCascadeCheck = ref(false);
    const modalLoading = ref(false);

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

    const openModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
        const [menuResult, packDetails] = await Promise.all([
          MenuApi.getMenuTree({}),
          PackApi.getPackDetails({ id: props.data.id }),
        ]);
        if (Result.isSuccess(menuResult)) {
          modalTreeData.value = menuResult.data;
        }
        if (Result.isError(menuResult)) {
          ElMessage.error(menuResult.message);
        }
        const ids = packDetails.menuIds ?? [];
        modalCheckedKeys.value = ids.map((id) => String(id));
      } catch (error: any) {
        ElMessage.error(error.message || "加载菜单数据失败");
      } finally {
        modalLoading.value = false;
      }
    };

    const onCheckedKeysChange = (keys: (string | number)[]): void => {
      modalCheckedKeys.value = keys;
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

    const submitModal = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      modalLoading.value = true;
      try {
        const menuIds = modalCheckedKeys.value.map((k) => String(k));
        await PackApi.updatePackMenu({ packId: props.data.id, menuIds });
        ElMessage.success("菜单绑定已保存");
        emit("close");
        emit("success");
      } catch (error: any) {
        ElMessage.error(error.message || "保存失败");
      } finally {
        modalLoading.value = false;
      }
    };

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
