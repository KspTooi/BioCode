import { ref, computed, watch, nextTick, type Ref } from "vue";
import { watchDebounced } from "@vueuse/core";
import type { ElTree } from "element-plus";

export interface StdAdvTreeProps {
  //树的数据源
  data: Array<any>;

  //是否可以选中节点 总开关(check在高级树中代表单选+多选)
  check?: boolean;

  //是否支持多选
  checkMultiple?: boolean;

  //子节点是否级联选上级
  checkCascade?: boolean;

  //是否支持行点击选中节点(和expandOnClick互斥 如果两个都打开 只生效checkOnNodeClick)
  checkOnNodeClick?: boolean;

  //禁用选中的节点key
  checkDisableNks?: (string | number)[];

  //是否显示搜索框
  search?: boolean;

  //搜索框占位文字
  searchPlaceholder?: string;

  //搜索字段名，默认使用 nt 字段
  searchFields?: string[];

  //是否级联搜索(级联搜索时会把子节点也包含进来)
  searchCascade?: boolean;

  //是否在搜索旁显示刷新按钮
  searchRefresh?: boolean;

  //是否显示根节点(NodeRoot)
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
   * 高级树基础功能打包
   */
  useStdAdvTree(
    props: StdAdvTreeProps,
    emit: StdAdvTreeEmits,
    treeRef: Ref<InstanceType<typeof ElTree> | undefined>,
    selectedNk: Ref<string | number | null>,
    checkedNks: Ref<(string | number)[]>,
    checkedHalfNks: Ref<(string | number)[]>
  ) {
    const searchText = ref("");
    const rootSelected = computed(() => selectedNk.value === props.nrValue);

    //有效级联选项
    const effectiveCascade = computed(() => {
      let effective = true;

      //如果禁用级联，则级联不可用
      if (!props.checkCascade) {
        effective = false;
      }

      //如果禁用勾选，则级联不可用
      if (!props.check) {
        effective = false;
      }

      //如果禁用多选，则级联不可用
      if (!props.checkMultiple) {
        effective = false;
      }

      return effective;
    });

    //有效点击展开选项
    const effectiveClickExpand = computed(() => {
      //如果禁用点击展开，则点击展开不可用
      if (!props.expandOnClick) {
        return false;
      }

      //如果开启了节点点击选中，则点击展开不可用
      if (props.checkOnNodeClick) {
        return false;
      }
      return true;
    });

    /**
     * 树数据
     */
    const treeData = computed(() => {
      if (!props.checkDisableNks?.length) {
        return props.data;
      }
      const markDisabled = (nodes: any[]): any[] => {
        return nodes.map((node) => {
          const children = node[props.nc];
          if (props.checkDisableNks.includes(node[props.nk])) {
            return { ...node, disabled: true, [props.nc]: children ? markDisabled(children) : undefined };
          }
          if (children && Array.isArray(children)) {
            return { ...node, [props.nc]: markDisabled(children) };
          }
          return node;
        });
      };
      return markDisabled(props.data);
    });

    /**
     * 过滤节点
     */
    const filterNode = (value: string, data: any, node?: any): boolean => {
      if (!value) {
        return true;
      }
      const fields = props.searchFields?.length ? props.searchFields : [props.nt ?? "name"];
      const matchData = (d: any): boolean => fields.some((field) => String(d[field] ?? "").includes(value));

      if (matchData(data)) {
        return true;
      }

      //级联搜索：祖先命中时，后代节点也保留可见
      if (!props.searchCascade || !node) {
        return false;
      }
      let parent = node.parent;
      while (parent && parent.level > 0 && parent.data) {
        if (matchData(parent.data)) {
          return true;
        }
        parent = parent.parent;
      }
      return false;
    };

    watchDebounced(
      searchText,
      (val) => {
        treeRef.value?.filter(val);
        emit("on-search", val);
      },
      { debounce: 300 }
    );

    //同步选中节点高亮：selectedNk 或 data 或 treeRef 变化时同步
    watch(
      [selectedNk, () => props.data, () => treeRef.value],
      async ([nk, , tree]) => {
        if (!tree) {
          return;
        }
        await nextTick();
        if (nk === null || nk === undefined || nk === props.nrValue) {
          tree.setCurrentKey(null);
          return;
        }
        tree.setCurrentKey(nk);
      },
      { flush: "post", immediate: true }
    );

    //同步勾选状态：checkedNks 或 data 或 treeRef 变化时同步
    watch(
      [checkedNks, () => props.data, () => treeRef.value],
      async ([keys, , tree]) => {
        if (!tree) {
          return;
        }
        await nextTick();
        tree.setCheckedKeys(keys ?? []);
      },
      { flush: "post", immediate: true }
    );

    /**
     * 节点勾选事件
     * @param _data 节点数据
     * @param checkInfo 勾选信息
     */
    const onNodeCheck = (
      _data: any,
      checkInfo: {
        checkedKeys: (string | number)[];
        checkedNodes: any[];
        halfCheckedKeys: (string | number)[];
        halfCheckedNodes: any[];
      }
    ): void => {
      let keys = checkInfo.checkedKeys;

      if (!props.checkMultiple && keys.length > 1) {
        const prevKeys = new Set(checkedNks.value);
        const added = keys.find((k) => !prevKeys.has(k));
        keys = added ? [added] : [keys[keys.length - 1]];
        treeRef.value?.setCheckedKeys(keys);
      }

      checkedNks.value = keys;
      checkedHalfNks.value = checkInfo.halfCheckedKeys;
    };

    /**
     * 节点点击事件
     * @param data 节点数据
     */
    const onNodeClick = (data: any): void => {
      const key = data[props.nk];
      if (key === selectedNk.value) {
        return;
      }
      selectedNk.value = key;
      emit("on-select", data);
    };

    /**
     * 根节点点击事件
     */
    const onRootClick = (): void => {
      if (rootSelected.value) {
        return;
      }
      const nrValue = props.nrValue;
      selectedNk.value = nrValue;
      treeRef?.value.setCurrentKey(null);
      emit("on-root-select", nrValue);
    };

    const reset = (): void => {
      selectedNk.value = null;
      checkedNks.value = [];
      searchText.value = "";
      treeRef.value?.filter("");
      treeRef.value?.setCurrentKey(null);
      treeRef.value?.setCheckedKeys([]);
    };

    const filter = (val: string): void => {
      treeRef.value?.filter(val);
    };

    /**
     * 全选所有节点
     */
    const checkAll = (): void => {
      if (!props.check || !props.checkMultiple) {
        return;
      }
      const allKeys = collectAllKeys(treeData.value);
      treeRef.value?.setCheckedKeys(allKeys);
      checkedNks.value = (treeRef.value?.getCheckedKeys() as (string | number)[]) ?? [];
      checkedHalfNks.value = (treeRef.value?.getHalfCheckedKeys() as (string | number)[]) ?? [];
    };

    const checkClear = (): void => {
      treeRef.value?.setCheckedKeys([]);
      checkedNks.value = [];
      checkedHalfNks.value = [];
    };

    /**
     * 递归收集所有节点 key
     */
    const collectAllKeys = (nodes: any[]): (string | number)[] => {
      const keys: (string | number)[] = [];
      for (const node of nodes) {
        keys.push(node[props.nk]);
        const children = node[props.nc];
        if (children && Array.isArray(children) && children.length > 0) {
          keys.push(...collectAllKeys(children));
        }
      }
      return keys;
    };

    return {
      searchText,
      treeData,
      rootSelected,
      effectiveCascade,
      effectiveClickExpand,
      filterNode,
      reset,
      filter,
      checkAll,
      checkClear,
      onNodeCheck,
      onNodeClick,
      onRootClick,
    };
  },
};
