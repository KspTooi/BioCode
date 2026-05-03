import { computed, onMounted, reactive, ref, watch, type Ref } from "vue";
import type { AddMenuDto, EditMenuDto, GetMenuDetailsVo, GetMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";

type PanelMode = "add" | "edit" | "add-item";

export default {
  useMenuTree() {
    const { loadMenus } = ComMenuService.useMenuService();

    const treeData = ref<GetMenuTreeVo[]>([]);
    const treeLoading = ref(true);
    const treeCurrent = ref<string>("-1");

    const loadTree = async (): Promise<void> => {
      treeLoading.value = true;
      const result = await MenuApi.getMenuTree({});

      if (Result.isSuccess(result)) {
        treeData.value = result.data;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      treeLoading.value = false;
    };

    const removeNode = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该菜单吗？", "提示", {
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

  useMenuTreePanel(panelFormRef: Ref<FormInstance>, reloadCallback: () => void) {
    const { loadMenus } = ComMenuService.useMenuService();

    const panelVisible = ref(false);
    const panelLoading = ref(false);
    const panelMode = ref<PanelMode>("add");
    const panelCurrentRow = ref<GetMenuTreeVo | null>(null);
    const fullMenuTree = ref<GetMenuTreeVo[]>([]);

    const panelForm = reactive<GetMenuDetailsVo>({
      id: "",
      parentId: "",
      name: "",
      kind: 0,
      path: "",
      icon: "",
      hide: 0,
      permissionCode: "",
      seq: 0,
      remark: "",
    });

    const panelFormLabel = computed(() => {
      if (panelForm.kind == 0) {
        return "目录";
      }
      if (panelForm.kind == 1) {
        return "菜单";
      }
      if (panelForm.kind == 2) {
        return "按钮";
      }
      return "";
    });

    const panelRules = {
      name: [
        { required: true, message: "请输入菜单名称", trigger: "blur" },
        { min: 2, max: 128, message: "菜单名称长度必须在2-128个字符之间", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请选择菜单类型", trigger: "blur" }],
      path: [
        { required: true, message: "请输入菜单路径", trigger: "blur" },
        { max: 500, message: "菜单路径长度不能超过500个字符", trigger: "blur" },
      ],
      permissionCode: [{ max: 500, message: "所需权限长度不能超过500个字符", trigger: "blur" }],
      remark: [{ max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }],
      seq: [
        { required: true, message: "请输入排序", trigger: "blur" },
        { type: "number", min: 0, max: 655350, message: "排序只能在0-655350之间", trigger: "blur" },
      ],
      icon: [
        { required: true, message: "请选择菜单图标", trigger: "change" },
        { max: 80, message: "菜单图标长度不能超过80个字符", trigger: "blur" },
      ],
      hide: [{ required: true, message: "请选择是否隐藏", trigger: "blur" }],
    };

    // kind 变为目录时清空菜单路径
    watch(
      () => panelForm.kind,
      (newVal: number | null | undefined) => {
        if (newVal == 0) {
          panelForm.path = "";
        }
      },
      { immediate: true }
    );

    // 父级选择树：过滤掉按钮，并根据当前菜单类型禁用不合法的父级
    const panelParentMenuTree = computed(() => {
      const isEditMode = panelMode.value === "edit";

      const filter = (menuTree: GetMenuTreeVo[]): GetMenuTreeVo[] => {
        return menuTree
          .filter((item) => item.kind !== 2)
          .map((item) => {
            let disabled = false;
            if (isEditMode && item.id === panelForm.id) {
              disabled = true;
            }
            // 目录：父级只能是目录
            if (panelForm.kind === 0 && item.kind !== 0) {
              disabled = true;
            }
            // 菜单：父级只能是目录
            if (panelForm.kind === 1 && item.kind !== 0) {
              disabled = true;
            }
            // 按钮：父级只能是菜单
            if (panelForm.kind === 2 && item.kind !== 1) {
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
              missingPermission: item.missingPermission,
              seq: item.seq,
              disabled,
              children: item.children ? filter(item.children) : [],
            };
          });
      };

      // 菜单和按钮不能直接挂在根节点下
      const rootDisabled = panelForm.kind === 1 || panelForm.kind === 2;
      return [{ id: "", name: "根节点", disabled: rootDisabled, children: filter(fullMenuTree.value) }];
    });

    const panelBreadcrumb = computed<string[]>(() => {
      const root = ["全部菜单"];
      if (!panelForm.parentId) {
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
      const ancestors = findPath(fullMenuTree.value, panelForm.parentId, []);
      if (ancestors) {
        return [...root, ...ancestors];
      }
      return root;
    });

    const loadFullMenuTree = async (): Promise<void> => {
      const result = await MenuApi.getMenuTree({});
      if (Result.isSuccess(result)) {
        fullMenuTree.value = result.data;
      }
    };

    const resetPanel = (full: boolean = false): void => {
      panelForm.id = "";
      panelForm.name = "";
      panelForm.path = "";
      panelForm.icon = "";
      panelForm.hide = 0;
      panelForm.permissionCode = "";
      panelForm.seq = 0;
      panelForm.remark = "";
      if (!full) {
        return;
      }
      panelForm.parentId = "";
      panelForm.kind = 0;
    };

    const openPanel = async (mode: PanelMode, currentRow: GetMenuTreeVo | null): Promise<void> => {
      await loadFullMenuTree();
      panelMode.value = mode;
      panelCurrentRow.value = currentRow;
      resetPanel();

      if (mode === "add") {
        panelForm.parentId = "";
      }

      if (mode === "add-item" && currentRow) {
        panelForm.parentId = currentRow.id;
        // 父节点是菜单时，子项默认选按钮
        if (currentRow.kind == 1) {
          panelForm.kind = 2;
        }
      }

      if (mode === "edit" && currentRow) {
        const ret = await MenuApi.getMenuDetails({ id: currentRow.id });
        if (Result.isError(ret)) {
          ElMessage.error(ret.message);
          return;
        }
        panelForm.id = ret.data.id;
        panelForm.parentId = ret.data.parentId ?? "";
        panelForm.name = ret.data.name;
        panelForm.kind = ret.data.kind;
        panelForm.path = ret.data.path;
        panelForm.icon = ret.data.icon;
        panelForm.hide = ret.data.hide;
        panelForm.permissionCode = ret.data.permissionCode;
        panelForm.seq = ret.data.seq;
        panelForm.remark = ret.data.remark;
      }

      panelVisible.value = true;
    };

    const closePanel = (): void => {
      panelVisible.value = false;
      resetPanel(true);
      reloadCallback();
    };

    const submitPanel = async (): Promise<void> => {
      try {
        await panelFormRef?.value?.validate();
      } catch {
        return;
      }

      panelLoading.value = true;

      try {
        if (panelMode.value === "add" || panelMode.value === "add-item") {
          const addDto: AddMenuDto = {
            parentId: panelForm.parentId,
            name: panelForm.name,
            kind: panelForm.kind,
            path: panelForm.path,
            icon: panelForm.icon,
            hide: panelForm.hide,
            permissionCode: panelForm.permissionCode,
            seq: panelForm.seq,
            remark: panelForm.remark,
          };
          const ret = await MenuApi.addMenu(addDto);
          if (Result.isError(ret)) {
            ElMessage.error(ret.message);
            return;
          }
          ElMessage.success("操作成功");
          panelVisible.value = false;
        }

        if (panelMode.value === "edit") {
          const editDto: EditMenuDto = {
            id: panelForm.id,
            parentId: panelForm.parentId,
            name: panelForm.name,
            kind: panelForm.kind,
            path: panelForm.path,
            icon: panelForm.icon,
            hide: panelForm.hide,
            permissionCode: panelForm.permissionCode,
            seq: panelForm.seq,
            remark: panelForm.remark,
          };
          const ret = await MenuApi.editMenu(editDto);
          if (Result.isError(ret)) {
            ElMessage.error(ret.message);
            return;
          }
          ElMessage.success("操作成功");
          panelVisible.value = false;
        }
      } finally {
        panelLoading.value = false;
        reloadCallback();
      }

      loadMenus();
    };

    return {
      panelVisible,
      panelLoading,
      panelMode,
      panelCurrentRow,
      panelForm,
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
