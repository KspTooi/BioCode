import { ref, computed, watch, onMounted, nextTick } from "vue";
import type { ElTree } from "element-plus";

export interface StdAdvTreeProps {
  //双向绑定的选中节点 key（对应 nk 字段的值）
  modelValue?: string | number | null;

  //初始化选中节点 key（对应 nk 字段的值）
  initValue?: string | number | null;

  //树的数据源
  data: Array<any>;

  //是否显示搜索框
  search?: boolean;

  //搜索框占位文字
  searchPlaceholder?: string;

  //搜索字段名，默认使用 nt 字段
  searchFields?: string[];

  //是否在搜索旁显示刷新按钮
  searchRefresh?: boolean;

  //是否显示根节点
  nr?: boolean;

  //根节点标题
  nrTitle?: string;

  //根节点图标
  nrIcon?: string;

  //根节点值
  nrValue?: string;

  //节点的图标字段名（读取节点数据中的图标字符串，兼容 ep:/iconify 格式）
  ni?: string;

  //节点的唯一标识字段名
  nk?: string;

  //节点的显示字段名
  nt?: string;

  //节点的子节点字段名
  nc?: string;

  //是否显示 loading
  loading?: boolean;

  //是否默认展开全部
  expandOnDefault?: boolean;

  //点击节点本身是否展开/折叠
  expandOnClick?: boolean;

  //是否显示节点操作按钮区（总开关）
  action?: boolean;

  //显示哪些操作按钮，不传则全部显示；可选值：'add' | 'edit' | 'remove'
  actionMode?: Array<"add" | "edit" | "remove">;
}

export interface StdAdvTreeEmits {
  (e: "update:modelValue", key: string | number | null): void;
  (e: "on-select", node: any): void;
  (e: "on-root-select", value: string): void;
  (e: "on-add", node: any): void;
  (e: "on-edit", node: any): void;
  (e: "on-remove", node: any): void;
  (e: "on-search", value: string): void;
  (e: "on-refresh", value: string): void;
}

export default {
  /**
   * 使用StdAdvTree
   * @param props 组件属性
   * @param emit 组件事件
   * @returns 组件实例
   */
  useStdAdvTree(props: StdAdvTreeProps, emit: StdAdvTreeEmits) {
    const treeRef = ref<InstanceType<typeof ElTree>>();
    const filterText = ref("");
    const currentSelectedKey = ref<string | number | null>(null);

    const defaultProps = computed(() => ({
      children: props.nc ?? "children",
      label: props.nt ?? "name",
    }));

    const filterNode = (value: string, data: any): boolean => {
      if (!value) {
        return true;
      }
      const fields = props.searchFields?.length ? props.searchFields : [props.nt ?? "name"];
      return fields.some((field) => String(data[field] ?? "").includes(value));
    };

    const onFilterInput = (val: string): void => {
      treeRef.value?.filter(val);
      emit("on-search", val);
    };

    const onNodeClick = (data: any): void => {
      const key = data[props.nk ?? "id"];
      currentSelectedKey.value = key;
      emit("update:modelValue", key);
      emit("on-select", data);
    };

    const nrKey = computed(() => props.nrValue ?? null);

    const isRootSelected = computed(() => currentSelectedKey.value === nrKey.value);

    watch(
      () => props.modelValue,
      (val) => {
        currentSelectedKey.value = val ?? null;
        const isRoot = val === nrKey.value;
        treeRef.value?.setCurrentKey(isRoot ? null : (val ?? null));
      }
    );

    const reset = (): void => {
      currentSelectedKey.value = null;
      filterText.value = "";
      treeRef.value?.filter("");
      treeRef.value?.setCurrentKey(null);
      emit("update:modelValue", null);
    };

    const filter = (val: string): void => {
      treeRef.value?.filter(val);
    };

    const getTreeRef = (): InstanceType<typeof ElTree> | undefined => {
      return treeRef.value;
    };

    onMounted(async () => {
      const init = props.initValue ?? props.modelValue ?? null;
      if (init === null) {
        return;
      }
      await nextTick();
      currentSelectedKey.value = init;
      const isRoot = init === nrKey.value;
      treeRef.value?.setCurrentKey(isRoot ? null : init);
      emit("update:modelValue", init);
    });

    const onRootClick = (): void => {
      currentSelectedKey.value = nrKey.value;
      treeRef.value?.setCurrentKey(null);
      emit("update:modelValue", nrKey.value);
      emit("on-root-select", nrKey.value);
    };

    return {
      treeRef,
      filterText,
      currentSelectedKey,
      isRootSelected,
      defaultProps,
      filterNode,
      onFilterInput,
      onNodeClick,
      onRootClick,
      reset,
      filter,
      getTreeRef,
    };
  },
};
