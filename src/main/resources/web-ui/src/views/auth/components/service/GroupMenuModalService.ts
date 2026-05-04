import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import type { GetGroupListVo } from "@/views/auth/api/GroupApi.ts";
import GroupApi from "@/views/auth/api/GroupApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import type { GetMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

export interface GroupMenuModalProps {
  //是否显示模态框
  visible: boolean;
  //组信息
  data?: GetGroupListVo | null;
}

export default {
  useGroupMenuModal(props: GroupMenuModalProps, emit: (e: "close" | "success") => void) {
    const treeRef = ref<InstanceType<typeof StdAdvTree>>();
    const treeData = ref<GetMenuTreeVo[]>([]);
    const checkedKeys = ref<(string | number)[]>([]);
    const loading = ref(false);
    const submitting = ref(false);

    const loadAll = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      loading.value = true;
      const [menuResult, groupDetails] = await Promise.all([
        MenuApi.getMenuTree({}),
        GroupApi.getGroupDetails({ id: props.data.id }),
      ]);
      if (menuResult.code == 0) {
        treeData.value = menuResult.data;
      }
      const ids = groupDetails.menuIds ?? [];
      checkedKeys.value = ids.map((id) => String(id));
      loading.value = false;
    };

    const onCheckedKeysChange = (keys: (string | number)[]): void => {
      checkedKeys.value = keys;
    };

    const submit = async (): Promise<void> => {
      if (!props.data?.id) {
        return;
      }
      submitting.value = true;
      const innerTree = treeRef.value?.getTreeRef();
      const halfKeys: (string | number)[] = innerTree ? (innerTree.getHalfCheckedKeys() as (string | number)[]) : [];
      const allKeys = Array.from(new Set([...checkedKeys.value, ...halfKeys]));
      const menuIds = allKeys.map((k) => String(k));
      const result = await GroupApi.updateGroupGm({ groupId: props.data.id, menuIds });
      submitting.value = false;
      if (result.code != 0) {
        ElMessage.error(result.message || "保存失败");
        return;
      }
      ElMessage.success("菜单绑定已保存");
      emit("close");
      emit("success");
    };

    const reset = (): void => {
      treeData.value = [];
      checkedKeys.value = [];
      loading.value = false;
      submitting.value = false;
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
      treeRef,
      treeData,
      checkedKeys,
      loading,
      submitting,
      loadAll,
      onCheckedKeysChange,
      submit,
      reset,
    };
  },
};
