import { ref, computed, watch, onMounted, nextTick } from "vue";
import type { ElTree } from "element-plus";

export interface StdAdvTreeProps {
  //树的数据源
  data: Array<any>;

  //双向绑定的选中节点 key（对应 nk 字段的值）
  modelValue?: string | number | null;

  //初始化选中节点 key（对应 nk 字段的值）
  initValue?: string | number | null;

  //双向绑定的Check节点 keys
  modelValueCheck?: (string | number)[];

  //初始化Check节点 keys
  initValueCheck?: (string | number)[];

  //是否可以选中节点 总开关
  check: boolean;

  //是否支持多选
  checkMultiple?: boolean;

  //子节点是否级联选上级
  checkCascade?: boolean;

  //是否支持行点击选中节点(和expandOnClick互斥 如果两个都打开 只生效checkOnNodeClick)
  checkOnNodeClick?: boolean;

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
  (e: "update:modelValueCheck", keys: (string | number)[]): void;
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
    const checkedKeys = ref<(string | number)[]>([]);

    const defaultProps = computed(() => ({
      children: props.nc ?? "children",
      label: props.nt ?? "name",
    }));

    const showCheckbox = computed(() => props.check ?? false);
    const checkStrictly = computed(() => !(props.checkCascade ?? true));
    const checkOnClickNode = computed(() => props.checkOnNodeClick ?? false);

    const onCheck = (_data: any, checkInfo: { checkedKeys: (string | number)[]; checkedNodes: any[]; halfCheckedKeys: (string | number)[]; halfCheckedNodes: any[] }): void => {
      let keys = checkInfo.checkedKeys;
      const isMultiple = props.checkMultiple ?? true;

      if (!isMultiple && keys.length > 1) {
        const prevKeys = new Set(checkedKeys.value);
        const added = keys.find((k) => !prevKeys.has(k));
        keys = added !== undefined ? [added] : [keys[keys.length - 1]];
        treeRef.value?.setCheckedKeys(keys);
      }

      checkedKeys.value = keys;
      emit("update:modelValueCheck", keys);
    };

    const filterNode = (value: string, data: any): boolean => {
      if (!value) {
        return true;
      }
      const fields = props.searchFields?.length ? props.searchFields : [props.nt ?? "name"];
      return fields.some((field) => String(data[field] ?? "").includes(value));
    };

    let searchTimer: ReturnType<typeof setTimeout> | null = null;

    const onFilterInput = (val: string): void => {
      if (searchTimer !== null) {
        clearTimeout(searchTimer);
      }
      searchTimer = setTimeout(() => {
        searchTimer = null;
        treeRef.value?.filter(val);
        emit("on-search", val);
      }, 300);
    };

    const onNodeClick = (data: any): void => {
      const key = data[props.nk ?? "id"];
      if (key === currentSelectedKey.value) {
        return;
      }
      currentSelectedKey.value = key;
      emit("update:modelValue", key);
      emit("on-select", data);
    };

    const nrKey = computed(() => props.nrValue ?? null);

    const isRootSelected = computed(() => currentSelectedKey.value === nrKey.value);

    watch(
      () => props.modelValueCheck,
      (val) => {
        if (val !== undefined && val !== null) {
          checkedKeys.value = [...val];
          treeRef.value?.setCheckedKeys(val);
        }
      }
    );

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
      checkedKeys.value = [];
      filterText.value = "";
      treeRef.value?.filter("");
      treeRef.value?.setCurrentKey(null);
      treeRef.value?.setCheckedKeys([]);
      emit("update:modelValue", null);
      emit("update:modelValueCheck", []);
    };

    const filter = (val: string): void => {
      treeRef.value?.filter(val);
    };

    const getTreeRef = (): InstanceType<typeof ElTree> | undefined => {
      return treeRef.value;
    };

    onMounted(async () => {
      await nextTick();

      const init = props.initValue ?? props.modelValue ?? null;
      if (init !== null) {
        currentSelectedKey.value = init;
        const isRoot = init === nrKey.value;
        treeRef.value?.setCurrentKey(isRoot ? null : init);
        emit("update:modelValue", init);
      }

      const initCheck = props.initValueCheck ?? props.modelValueCheck ?? null;
      if (initCheck !== null && initCheck.length > 0) {
        checkedKeys.value = [...initCheck];
        treeRef.value?.setCheckedKeys(initCheck);
        emit("update:modelValueCheck", initCheck);
      }
    });

    const onRootClick = (): void => {
      if (isRootSelected.value) {
        return;
      }
      currentSelectedKey.value = nrKey.value;
      treeRef.value?.setCurrentKey(null);
      emit("update:modelValue", nrKey.value);
      emit("on-root-select", nrKey.value);
    };

    return {
      treeRef,
      filterText,
      currentSelectedKey,
      checkedKeys,
      isRootSelected,
      defaultProps,
      showCheckbox,
      checkStrictly,
      checkOnClickNode,
      filterNode,
      onFilterInput,
      onNodeClick,
      onCheck,
      onRootClick,
      reset,
      filter,
      getTreeRef,
    };
  },
};
