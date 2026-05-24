import { computed, h, onMounted, ref, watch, type Ref } from "vue";
import type { AddMenuDto, EditMenuDto, GetMenuDetailsVo, GetMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";
import { defineStore, storeToRefs } from "pinia";
import type { GetPermissionDefinitionVo } from "@/views/auth/api/PermissionApi";
import PermissionApi from "@/views/auth/api/PermissionApi";

//面板模式
type PanelMode = "add" | "edit" | "add-item";

//服务内共享存储(Tree+Panel都用这个存储)
const useMenuServiceStore = defineStore("menuManagerTreeStore", {
  state: () => ({
    treeData: [] as GetMenuTreeVo[],
    treeCurrent: "-1" as string,
    panelPermissionCodes: [] as GetPermissionDefinitionVo[],
    panelVisible: false as boolean,
    panelCurrentRow: null as GetMenuTreeVo | null,
    panelMode: "add" as PanelMode,
    panelForm: {
      id: "",
      parentId: "",
      name: "",
      kind: 0,
      path: "",
      icon: "",
      hide: 0,
      permissionCode: [] as string[],
      seq: 0,
      remark: "",
    } as GetMenuDetailsVo,
  }),
  persist: {
    key: "np_menu_manager_tree",
    pick: ["treeCurrent", "panelVisible", "panelCurrentRow", "panelMode", "panelForm"],
  },

  actions: {
    /**
     * 重置选中状态
     */
    resetSelected() {
      //先把树的选中状态重置为根节点
      this.treeCurrent = "-1";
      this.panelCurrentRow = null;

      //然后把面板状态重置回初始
      this.panelVisible = false;
      this.panelMode = "add";
      this.panelCurrentRow = null;
    },
  },
});

export default {
  /**
   * 菜单树功能打包
   */
  useMenuTree() {
    const { loadMenus } = ComMenuService.useMenuService();
    const treeStore = useMenuServiceStore();
    const { treeCurrent, treeData } = storeToRefs(treeStore);
    const treeLoading = ref(true);

    /**
     * 加载菜单树
     */
    const loadTree = async (): Promise<void> => {
      treeLoading.value = true;
      try {
        const result = await MenuApi.getMenuTree({});
        //加载权限代码列表
        const permissionCodes = await PermissionApi.getPermissionDefinition();
        if (Result.isSuccess(result)) {
          treeData.value = result.data;
          treeStore.panelPermissionCodes = permissionCodes;
        }

        if (Result.isError(result)) {
          ElMessage.error(result.message);
        }
      } catch (error: any) {
        ElMessage.error(error.message);
        return;
      } finally {
        treeLoading.value = false;
      }
    };

    const removeNode = async (id: string): Promise<void> => {
      // 查询该菜单被哪些菜单包引用
      let packNames: string[] = [];
      try {
        const result = await MenuApi.getPacksByMenuId({ id });
        if (Result.isSuccess(result) && result.data && result.data.length > 0) {
          packNames = result.data.map((p) => p.name);
        }
      } catch {
        ElMessage.error("获取菜单包引用失败");
        return;
      }

      // 构建确认消息
      let message: ReturnType<typeof h> | string = "确定删除该菜单吗？";
      if (packNames.length > 0) {
        message = h("div", null, [
          h("p", { style: { color: "#f56c6c", marginBottom: "8px" } }, [
            `该菜单正被以下 ${packNames.length} 个菜单包引用，删除后关联将被自动清理：`,
          ]),
          h("p", { style: { color: "#f56c6c", fontWeight: "bold", marginBottom: "12px" } }, packNames.join("、")),
          h("p", null, "确定删除该菜单吗？"),
        ]);
      }

      try {
        await ElMessageBox.confirm(message, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await MenuApi.removeMenu({ id });
        await loadTree();
        loadMenus();

        //如果被删的是当前选的 直接重置选中状态
        if (treeCurrent.value === id) {
          treeStore.resetSelected();
        }
      } catch (error: any) {
        ElMessage.error(error.message);
        return;
      }
    };

    onMounted(async () => {
      await loadTree();
    });

    return {
      treeData,
      treeLoading,
      treeCurrent,
      loadTree,
      removeNode,
    };
  },

  /**
   * 菜单树面板功能打包
   */
  useMenuTreePanel(panelFormRef: Ref<FormInstance>, reloadCallback: () => void) {
    const { loadMenus } = ComMenuService.useMenuService();
    const treeStore = useMenuServiceStore();
    const { treeData } = storeToRefs(treeStore);
    const panelLoading = ref(false);

    const panelFormLabel = computed(() => {
      if (treeStore.panelForm.kind == 0) {
        return "目录";
      }
      if (treeStore.panelForm.kind == 1) {
        return "菜单";
      }
      if (treeStore.panelForm.kind == 2) {
        return "按钮";
      }
      if (treeStore.panelForm.kind == 3) {
        return "外链";
      }
      if (treeStore.panelForm.kind == 4) {
        return "外链";
      }
      return "";
    });

    const panelRules = {
      name: [
        { required: true, message: "请输入菜单名称", trigger: "blur" },
        { min: 2, max: 40, message: "菜单名称长度必须在2-40个字符之间", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请选择菜单类型", trigger: "blur" }],
      path: [
        { required: true, message: "请输入菜单路径", trigger: "blur" },
        { max: 512, message: "菜单路径长度不能超过512个字符", trigger: "blur" },
      ],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      seq: [
        { required: true, message: "请输入排序", trigger: "blur" },
        { type: "number", min: 0, max: 655350, message: "排序只能在0-655350之间", trigger: "blur" },
      ],
      icon: [
        { required: true, message: "请选择菜单图标", trigger: "blur" },
        { max: 80, message: "菜单图标长度不能超过80个字符", trigger: "blur" },
      ],
      hide: [{ required: true, message: "请选择是否隐藏", trigger: "blur" }],
    };

    // kind 变为目录时清空菜单路径
    watch(
      () => treeStore.panelForm.kind,
      (newVal: number | null | undefined) => {
        if (newVal == 0) {
          treeStore.panelForm.path = "";
        }
      },
      { immediate: true }
    );

    // 父级选择树：过滤掉按钮，并根据当前菜单类型禁用不合法的父级
    const panelParentMenuTree = computed(() => {
      const isEditMode = treeStore.panelMode === "edit";

      const filter = (menuTree: GetMenuTreeVo[]): GetMenuTreeVo[] => {
        return menuTree
          .filter((item) => item.kind !== 2)
          .map((item) => {
            let disabled = false;
            if (isEditMode && item.id === treeStore.panelForm.id) {
              disabled = true;
            }
            // 目录：父级只能是目录
            if (treeStore.panelForm.kind === 0 && item.kind !== 0) {
              disabled = true;
            }
            // 菜单(外链嵌套+外链跳转)：父级只能是目录
            if (
              (treeStore.panelForm.kind === 1 || treeStore.panelForm.kind === 3 || treeStore.panelForm.kind === 4) &&
              item.kind !== 0
            ) {
              disabled = true;
            }
            // 按钮：父级只能是菜单
            if (treeStore.panelForm.kind === 2 && item.kind !== 1) {
              disabled = true;
            }
            return {
              id: item.id,
              parentId: item.parentId,
              name: item.name,
              kind: item.kind,
              path: item.path,
              icon: item.icon,
              hide: item.hide,
              permissionCode: item.permissionCode,
              seq: item.seq,
              disabled,
              children: item.children ? filter(item.children) : [],
            };
          });
      };

      //按钮不能直接挂在根节点下 其他类型可以
      const rootDisabled = treeStore.panelForm.kind === 2;
      return [{ id: "", name: "根节点", disabled: rootDisabled, children: filter(treeData.value) }];
    });

    const panelBreadcrumb = computed<string[]>(() => {
      const root = ["全部菜单"];
      if (!treeStore.panelForm.parentId) {
        return root;
      }
      const findPath = (nodes: GetMenuTreeVo[], targetId: string, acc: string[]): string[] | null => {
        for (const node of nodes) {
          const next = [...acc, node.name ?? ""];
          if (node.id === targetId) {
            return next;
          }
          if (node.children && node.children.length > 0) {
            const found = findPath(node.children, targetId, next);
            if (found) {
              return found;
            }
          }
        }
        return null;
      };
      const ancestors = findPath(treeData.value, treeStore.panelForm.parentId, []);
      if (ancestors) {
        return [...root, ...ancestors];
      }
      return root;
    });

    /**
     * 重置面板表单
     * @param full 是否完全重置(包括父级ID和类型)
     */
    const resetPanel = (full: boolean = false): void => {
      treeStore.panelForm.id = "";
      treeStore.panelForm.name = "";
      treeStore.panelForm.path = "";
      treeStore.panelForm.icon = "";
      treeStore.panelForm.hide = 0;
      treeStore.panelForm.permissionCode = [];
      treeStore.panelForm.seq = 0;
      treeStore.panelForm.remark = "";
      if (!full) {
        return;
      }
      treeStore.panelForm.parentId = "";
      treeStore.panelForm.kind = 0;
    };

    /**
     * 打开面板
     * @param mode 模式
     * @param currentRow 当前行
     */
    const openPanel = async (mode: PanelMode, currentRow: GetMenuTreeVo | null): Promise<void> => {
      treeStore.panelMode = mode;
      treeStore.panelCurrentRow = currentRow;

      //编辑模式时加载详情数据
      if (mode === "edit" && currentRow) {
        panelLoading.value = true;

        try {
          const ret = await MenuApi.getMenuDetails({ id: currentRow.id });
          if (Result.isError(ret)) {
            ElMessage.error(ret.message);
            return;
          }
          Object.assign(treeStore.panelForm, {
            id: ret.data.id,
            parentId: ret.data.parentId ?? "",
            name: ret.data.name,
            kind: ret.data.kind,
            path: ret.data.path,
            icon: ret.data.icon,
            hide: ret.data.hide,
            permissionCode: ret.data.permissionCode,
            seq: ret.data.seq,
            remark: ret.data.remark,
          });
        } catch (error: any) {
          ElMessage.error(error.message);

          //加载失败时把tree状态重置为初始状态
          treeStore.resetSelected();
          return;
        } finally {
          panelLoading.value = false;
        }
      }

      //新增模式时重置表单
      if (mode === "add") {
        resetPanel(true);
      }

      //新增子项模式时设置父级ID
      if (mode === "add-item") {
        resetPanel(true);
        treeStore.panelForm.parentId = currentRow.id;

        //父级是目录时，子项默认选菜单
        if (currentRow.kind == 0) {
          treeStore.panelForm.kind = 1;
        }

        //父级是菜单时，子项默认选按钮
        if (currentRow.kind == 1) {
          treeStore.panelForm.kind = 2;
        }
      }

      treeStore.panelVisible = true;
    };

    const closePanel = (): void => {
      treeStore.panelVisible = false;
      resetPanel(true);
      reloadCallback();

      //如果当前是在新增子项模式下，且左侧树还选了东西 直接回退进详情
      if (treeStore.panelMode === "add-item" && treeStore.treeCurrent !== "-1") {
        openPanel("edit", treeStore.panelCurrentRow);
        return;
      }

      //清除左侧树的选中状态
      treeStore.resetSelected();
    };

    /**
     * 提交面板表单
     */
    const submitPanel = async (): Promise<void> => {
      try {
        await panelFormRef?.value?.validate();
      } catch {
        return;
      }

      panelLoading.value = true;

      try {
        //新增模式或新增子项模式
        if (treeStore.panelMode === "add" || treeStore.panelMode === "add-item") {
          const addDto: AddMenuDto = {
            parentId: treeStore.panelForm.parentId,
            name: treeStore.panelForm.name,
            kind: treeStore.panelForm.kind,
            path: treeStore.panelForm.path,
            icon: treeStore.panelForm.icon,
            hide: treeStore.panelForm.hide,
            permissionCode: treeStore.panelForm.permissionCode,
            seq: treeStore.panelForm.seq,
            remark: treeStore.panelForm.remark,
          };
          const ret = await MenuApi.addMenu(addDto);
          if (Result.isError(ret)) {
            ElMessage.error(ret.message);
            return;
          }
          ElMessage.success("操作成功");

          //如果是新增模式提交成功、则清除表单继续新增
          if (treeStore.panelMode === "add") {
            resetPanel(true);
          }

          //如果是新增子项模式提交成功、则清除表单继续新增子项
          if (treeStore.panelMode === "add-item") {
            resetPanel(false);
          }
        }

        //编辑模式
        if (treeStore.panelMode === "edit") {
          const editDto: EditMenuDto = {
            id: treeStore.panelForm.id,
            parentId: treeStore.panelForm.parentId,
            name: treeStore.panelForm.name,
            kind: treeStore.panelForm.kind,
            path: treeStore.panelForm.path,
            icon: treeStore.panelForm.icon,
            hide: treeStore.panelForm.hide,
            permissionCode: treeStore.panelForm.permissionCode,
            seq: treeStore.panelForm.seq,
            remark: treeStore.panelForm.remark,
          };

          const ret = await MenuApi.editMenu(editDto);
          if (Result.isError(ret)) {
            ElMessage.error(ret.message);
            return;
          }
          ElMessage.success("操作成功");
          //treeStore.panelVisible = false;
        }
      } catch (error: any) {
        ElMessage.error(error.message);

        return;
      } finally {
        panelLoading.value = false;
        reloadCallback();
      }

      loadMenus();
    };

    onMounted(async () => {
      // 编辑模式恢复时重新拉取最新数据，避免展示过时缓存
      if (treeStore.panelMode === "edit" && treeStore.panelVisible && treeStore.panelForm.id) {
        try {
          const ret = await MenuApi.getMenuDetails({ id: treeStore.panelForm.id });
          if (Result.isSuccess(ret)) {
            Object.assign(treeStore.panelForm, {
              id: ret.data.id,
              parentId: ret.data.parentId ?? "",
              name: ret.data.name,
              kind: ret.data.kind,
              path: ret.data.path,
              icon: ret.data.icon,
              hide: ret.data.hide,
              permissionCode: ret.data.permissionCode,
              seq: ret.data.seq,
              remark: ret.data.remark,
            });
          }
        } catch {
          // 拉取失败则关闭面板
          treeStore.panelVisible = false;
        }
      }
    });

    return {
      panelVisible: computed(() => treeStore.panelVisible),
      panelLoading,
      panelMode: computed(() => treeStore.panelMode),
      panelCurrentRow: computed(() => treeStore.panelCurrentRow),
      panelForm: computed(() => treeStore.panelForm),
      panelPermissionCodes: computed(() => treeStore.panelPermissionCodes),
      panelFormLabel,
      panelBreadcrumb,
      panelRules,
      panelParentMenuTree,
      openPanel,
      resetPanel,
      closePanel,
      submitPanel,
    };
  },
};
