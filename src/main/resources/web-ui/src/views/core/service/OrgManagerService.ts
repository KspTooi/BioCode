import { computed, onMounted, reactive, ref, watch, type Ref } from "vue";
import type { GetOrgTreeVo, GetOrgDetailsVo, AddOrgDto, EditOrgDto } from "@/views/core/api/OrgApi.ts";
import OrgApi from "@/views/core/api/OrgApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox, type FormInstance, type TableInstance } from "element-plus";

export default {
  /**
   * 组织机构树打包
   * @param listTableRef 列表表格引用
   */
  useOrgTree() {
    const queryForm = reactive({
      name: "",
    });

    const listData = ref<GetOrgTreeVo[]>([]);
    const listLoading = ref(false);

    /**
     * 前端过滤数据
     */
    const filteredData = computed(() => {
      return filterTree(listData.value);
    });

    /**
     * 递归过滤树形数据
     */
    const filterTree = (tree: GetOrgTreeVo[]): GetOrgTreeVo[] => {
      return tree
        .map((node) => {
          const matchesName = !queryForm.name || node.name.includes(queryForm.name);

          const filteredChildren = node.children ? filterTree(node.children) : [];

          if (matchesName) {
            return {
              id: node.id,
              // rootId: node.rootId,
              parentId: node.parentId,
              kind: node.kind,
              name: node.name,
              seq: node.seq,
              level: node.level,
              children: filteredChildren,
            };
          }

          if (filteredChildren.length > 0) {
            return {
              id: node.id,
              // rootId: node.rootId,
              parentId: node.parentId,
              kind: node.kind,
              name: node.name,
              seq: node.seq,
              level: node.level,
              children: filteredChildren,
            };
          }

          return null;
        })
        .filter((node) => node !== null) as GetOrgTreeVo[];
    };

    /**
     * 用于树形选择器的数据
     */
    const treeSelectData = computed(() => {
      return convertToTreeSelect(listData.value);
    });

    /**
     * 转换为树形选择器格式
     */
    const convertToTreeSelect = (tree: GetOrgTreeVo[]): any[] => {
      return tree.map((node) => ({
        value: node.id,
        label: node.name,
        kind: node.kind,
        children: node.children && node.children.length > 0 ? convertToTreeSelect(node.children) : undefined,
      }));
    };

    /**
     * 接口未返回 level 时，按树深度补全（与后端「顶级为 1」约定一致）
     */
    const ensureTreeLevels = (tree: GetOrgTreeVo[], depth = 1): GetOrgTreeVo[] => {
      return tree.map((node) => ({
        ...node,
        level: typeof node.level === "number" && !Number.isNaN(node.level) ? node.level : depth,
        children: node.children?.length ? ensureTreeLevels(node.children, depth + 1) : [],
      }));
    };

    /**
     * 加载组织机构树
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const result = await OrgApi.getOrgTree({ name: queryForm.name });
        listData.value = ensureTreeLevels(result);
      } catch (error: any) {
        ElMessage.error(error.message || "获取组织机构树失败");
      }
      listLoading.value = false;
    };

    /**
     * 前端筛选
     */
    const filterData = (): void => {
      // 前端筛选，不需要重新加载数据
    };

    /**
     * 重置查询条件
     */
    const resetQuery = (): void => {
      queryForm.name = "";
    };

    /**
     * 删除组织机构
     */
    const removeList = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该组织机构吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await OrgApi.removeOrg({ id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    //初始化
    onMounted(async () => {
      await loadList();
    });

    return {
      queryForm,
      listData,
      listLoading,
      filteredData,
      treeSelectData,
      loadList,
      filterData,
      resetQuery,
      removeList,
    };
  },

  /**
   * 组织机构模态框打包
   * @param modalFormRef 模态框表单引用
   * @param loadList 列表加载函数
   * @param treeSelectData 树形选择器数据
   */
  useOrgModal(modalFormRef: Ref<FormInstance>, loadList: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<"add" | "edit" | "add-item">("add");
    const modalForm = reactive<GetOrgDetailsVo>({
      id: "",
      parentId: null as string | null,
      kind: 0, // 0:企业 1:子企业 2:部门
      name: "",
      shortName: "",
      remark: "",
      seq: 0,
    });

    const modalRules = computed(() => ({
      kind: [{ required: true, message: "请选择组织机构类型", trigger: "change" }],
      name: [
        { required: true, message: "请输入组织机构名称", trigger: "blur" },
        { min: 1, max: 80, message: "组织机构名称长度必须在1-80个字符之间", trigger: "blur" },
      ],
      shortName: [{ min: 1, max: 40, message: "组织机构简称长度必须在1-40个字符之间", trigger: "blur" }],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      parentId: [1, 2, 3].includes(modalForm.kind) ? [{ required: true, message: "请选择上级组织", trigger: "change" }] : [],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    }));

    /**
     * 打开模态框
     * @param mode 模式
     * @param row 当前行
     */
    const openModal = async (mode: "add" | "edit" | "add-item", row: GetOrgTreeVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "add") {
        modalForm.parentId = null;
        modalForm.kind = 0; // 顶级默认为企业
      }

      if (mode === "add-item" && row) {
        modalForm.parentId = row.id;
        modalForm.kind = 2; // 子级默认为部门
      }

      //如果是编辑模式则需要加载详情数据
      if (mode === "edit" && row) {
        try {
          const ret = await OrgApi.getOrgDetails({ id: row.id });
          modalForm.id = ret.id;
          modalForm.parentId = ret.parentId;
          modalForm.kind = ret.kind;
          modalForm.name = ret.name;
          modalForm.shortName = ret.shortName;
          modalForm.remark = ret.remark ?? "";
          modalForm.seq = ret.seq;
        } catch (error: any) {
          ElMessage.error(error.message || "获取组织机构详情失败");
          return;
        }
      }

      modalVisible.value = true;
    };

    /**
     * 重置模态框表单
     */
    const resetModal = (): void => {
      modalForm.id = "";
      modalForm.parentId = null;
      modalForm.kind = 0;
      modalForm.name = "";
      modalForm.shortName = "";
      modalForm.remark = "";
      modalForm.seq = 0;

      if (modalFormRef.value) {
        modalFormRef.value.resetFields();
      }
    };

    /**
     * 提交模态框表单
     */
    const submitModal = async (): Promise<void> => {
      //先校验表单
      try {
        await modalFormRef?.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      //提交表单
      try {
        if (modalMode.value === "add" || modalMode.value === "add-item") {
          const addDto: AddOrgDto = {
            parentId: modalForm.parentId,
            kind: modalForm.kind,
            name: modalForm.name,
            shortName: modalForm.shortName,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          const result = await OrgApi.addOrg(addDto);
          if (Result.isSuccess(result)) {
            ElMessage.success("操作成功");
            resetModal();
            modalVisible.value = false;
          }
          if (Result.isError(result)) {
            ElMessage.error(result.message);
            return;
          }
        }

        if (modalMode.value === "edit") {
          const editDto: EditOrgDto = {
            id: modalForm.id,
            parentId: modalForm.parentId,
            name: modalForm.name,
            shortName: modalForm.shortName,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          const result = await OrgApi.editOrg(editDto);
          if (Result.isSuccess(result)) {
            ElMessage.success("操作成功");
            modalVisible.value = false;
          }
          if (Result.isError(result)) {
            ElMessage.error(result.message);
            return;
          }
        }
      } catch (error: any) {
        ElMessage.error(error.message);
        return;
      } finally {
        modalLoading.value = false;
      }
      await loadList();
    };

    /**
     * 组织机构类型名称
     */
    const modalKindName = computed(() => {
      const kindNameMap = {
        0: "企业",
        1: "子企业",
        2: "部门",
      };
      return kindNameMap[modalForm.kind] || "";
    });

    return {
      modalKindName,
      modalKind: computed(() => {
        return modalForm.kind;
      }),
      modalVisible,
      modalLoading,
      modalMode,
      modalForm,
      modalRules,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
