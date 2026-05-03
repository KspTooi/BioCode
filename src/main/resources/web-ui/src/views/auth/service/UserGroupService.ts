import { ref, reactive, computed, watch, type Ref, onMounted } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import AdminGroupApi, {
  type GetGroupListDto,
  type GetGroupListVo,
  type GroupPermissionDefinitionVo,
  type GetGroupDetailsVo,
  type AddGroupDto,
  type EditGroupDto,
} from "@/views/auth/api/GroupApi.ts";
import AdminPermissionApi from "@/views/auth/api/PermissionApi.ts";
import { Result } from "@/commons/model/Result.ts";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

export default {
  /**
   * 用户组列表打包
   */
  useUserGroupList() {
    const listForm = reactive<GetGroupListDto>({
      pageNum: 1,
      pageSize: 20,
      keyword: "",
      status: undefined,
    });

    const listData = ref<GetGroupListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      if (listLoading.value) {
        return;
      }

      listLoading.value = true;
      const result = await AdminGroupApi.getGroupList(listForm);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    /**
     * 重置查询
     */
    const resetList = (): void => {
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      listForm.keyword = "";
      listForm.status = undefined;
      loadList();
    };

    /**
     * 删除项
     */
    const removeList = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该用户组吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AdminGroupApi.removeGroup({ id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 批量删除
     */
    const removeListBatch = async (selectedItems: GetGroupListVo[]): Promise<void> => {
      if (selectedItems.length === 0) {
        ElMessage.warning("请选择要删除的用户组");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.length} 个用户组吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        const ids = selectedItems.map((item) => item.id);
        await AdminGroupApi.removeGroup({ ids });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(() => {
      loadList();
    });

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      removeList,
      removeListBatch,
    };
  },

  /**
   * 用户组模态框打包
   */
  useUserGroupModal(modalFormRef: Ref<FormInstance | undefined>, loadList: () => void) {
    const modalVisible = ref(false);
    const modalMode = ref<"add" | "edit">("add");
    const modalLoading = ref(false);
    const isSystemGroup = ref(false);

    // 表单数据
    const modalForm = reactive<GetGroupDetailsVo>({
      id: "",
      code: "",
      name: "",
      remark: "",
      isSystem: 0,
      status: 1,
      seq: 0,
      permissions: [],
      rowScope: 0,
      deptIds: [],
    });

    // 表单校验规则
    const modalRules = {
      code: [
        { required: true, message: "请输入组编码", trigger: "blur" },
        { min: 2, max: 32, message: "组编码长度必须在2-32个字符之间", trigger: "blur" },
        {
          pattern: /^[a-zA-Z][a-zA-Z_]*$/,
          message: "组编码只能包含英文字符和下划线，且必须以字母开头",
          trigger: "blur",
        },
      ],
      name: [
        { required: true, message: "请输入组名称", trigger: "blur" },
        { min: 2, max: 80, message: "组名称长度必须在2-80个字符之间", trigger: "blur" },
      ],
      remark: [{ max: 200, message: "描述不能超过200个字符", trigger: "blur" }],
      seq: [
        { required: true, message: "请输入排序号", trigger: "blur" },
        { type: "number", min: 0, max: 655350, message: "排序号必须在0~655350之间", trigger: "blur" },
      ],
      rowScope: [{ required: true, message: "请选择数据权限范围", trigger: "change" }],
      deptIds: [
        {
          validator: (rule: any, value: any, callback: any) => {
            if (modalForm.rowScope === 60) {
              if (!value || value.length === 0) {
                callback(new Error("请选择至少一个部门"));
                return;
              }
            }
            callback();
          },
          trigger: "change",
        },
      ],
    };

    // 权限相关
    const permissionList = ref<GroupPermissionDefinitionVo[]>([]);
    const permissionSearch = ref("");
    const selectedPermissionIds = ref<string[]>([]);

    const filteredPermissions = computed(() => {
      const search = permissionSearch.value.toLowerCase().trim();
      if (!search) {
        return permissionList.value;
      }
      return permissionList.value.filter(
        (permission) => permission.name.toLowerCase().includes(search) || permission.code.toLowerCase().includes(search)
      );
    });

    // 部门选择相关
    const deptSelectModalVisible = ref(false);

    const openDeptSelect = (): void => {
      deptSelectModalVisible.value = true;
    };

    const onDeptSelectConfirm = (depts: GetOrgTreeVo | GetOrgTreeVo[]): void => {
      if (Array.isArray(depts)) {
        modalForm.deptIds = depts.map((d) => d.id);
        // 触发表单验证，清除错误提示
        if (modalFormRef.value) {
          modalFormRef.value.validateField("deptIds");
        }
      }
    };

    /**
     * 重置模态框
     */
    const resetModal = async (): Promise<void> => {
      modalForm.id = "";
      modalForm.code = "";
      modalForm.name = "";
      modalForm.remark = "";
      modalForm.status = 1;
      modalForm.seq = 0;
      modalForm.permissions = [];
      modalForm.rowScope = 0;
      modalForm.deptIds = [];

      permissionSearch.value = "";
      selectedPermissionIds.value = [];

      if (modalMode.value === "add") {
        try {
          const permissions = await AdminPermissionApi.getPermissionDefinition();
          permissionList.value = permissions.map((p) => ({
            id: p.id,
            code: p.code,
            name: p.name,
            has: 0,
          }));
        } catch {
          // 静默处理
        }
      }

      if (modalMode.value !== "add") {
        permissionList.value = [];
      }

      if (modalFormRef.value) {
        modalFormRef.value.resetFields();
      }
    };

    /**
     * 打开模态框
     */
    const openModal = async (mode: "add" | "edit", row: GetGroupListVo | null): Promise<void> => {
      modalMode.value = mode;
      await resetModal();

      if (mode === "edit" && row) {
        isSystemGroup.value = row.isSystem === 1;
        try {
          const ret = await AdminGroupApi.getGroupDetails({ id: row.id });
          modalForm.id = ret.id;
          modalForm.code = ret.code;
          modalForm.name = ret.name;
          modalForm.remark = ret.remark;
          modalForm.status = ret.status;
          modalForm.seq = ret.seq;
          modalForm.permissions = ret.permissions || [];
          modalForm.rowScope = ret.rowScope ?? 0;
          modalForm.deptIds = ret.deptIds || [];

          permissionList.value = ret.permissions || [];
          selectedPermissionIds.value = ret.permissions ? ret.permissions.filter((p) => p.has === 0).map((p) => p.id) : [];
        } catch (error: any) {
          ElMessage.error(error.message || "获取用户组详情失败");
          return;
        }
      }

      if (mode !== "edit" || !row) {
        isSystemGroup.value = false;
      }

      modalVisible.value = true;
    };

    /**
     * 提交模态框
     */
    const submitModal = async (): Promise<void> => {
      try {
        await modalFormRef?.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      try {
        if (modalMode.value === "add") {
          const addDto: AddGroupDto = {
            code: modalForm.code,
            name: modalForm.name,
            remark: modalForm.remark,
            status: modalForm.status,
            seq: modalForm.seq,
            rowScope: modalForm.rowScope,
            deptIds: modalForm.rowScope === 60 ? modalForm.deptIds : [],
            permissionIds: selectedPermissionIds.value,
          };
          const result = await AdminGroupApi.addGroup(addDto);
          if (Result.isSuccess(result)) {
            ElMessage.success("操作成功");
            modalVisible.value = false;
            await resetModal();
          }
          if (Result.isError(result)) {
            ElMessage.error(result.message);
            return;
          }
        }

        if (modalMode.value === "edit") {
          const editDto: EditGroupDto = {
            id: modalForm.id,
            code: modalForm.code,
            name: modalForm.name,
            remark: modalForm.remark,
            status: modalForm.status,
            seq: modalForm.seq,
            rowScope: modalForm.rowScope,
            deptIds: modalForm.rowScope === 60 ? modalForm.deptIds : [],
            permissionIds: selectedPermissionIds.value,
          };
          const result = await AdminGroupApi.editGroup(editDto);
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

    const selectAllPermissions = (): void => {
      selectedPermissionIds.value = filteredPermissions.value.map((p) => p.id);
    };

    const deselectAllPermissions = (): void => {
      selectedPermissionIds.value = [];
    };
    const rsSimulationModalVisible = ref(false);
    const openRsSimulationModal = (): void => {
      rsSimulationModalVisible.value = true;
    };

    // 监听 rowScope 变化，触发 deptIds 验证
    watch(
      () => modalForm.rowScope,
      (newVal): void => {
        // 如果不是指定部门，清空部门选择
        if (newVal !== 60) {
          modalForm.deptIds = [];
        }
        // 触发验证
        if (modalFormRef.value) {
          modalFormRef.value.validateField("deptIds");
        }
      }
    );

    return {
      modalVisible,
      modalMode,
      modalLoading,
      isSystemGroup,
      modalForm,
      modalRules,
      permissionList,
      permissionSearch,
      selectedPermissionIds,
      filteredPermissions,
      deptSelectModalVisible,
      openDeptSelect,
      onDeptSelectConfirm,
      openModal,
      resetModal,
      submitModal,
      selectAllPermissions,
      deselectAllPermissions,
      openRsSimulationModal,
    };
  },

  /**
   * 权限管理模态框打包
   */
  useUserGroupPermissionModal() {
    const modalPermissionEditVisible = ref(false);
    const modalPermissionEditRow = ref<GetGroupListVo | null>(null);

    const openPermissionEditModal = (row: GetGroupListVo): void => {
      modalPermissionEditRow.value = row;
      modalPermissionEditVisible.value = true;
    };

    return {
      modalPermissionEditVisible,
      modalPermissionEditRow,
      openPermissionEditModal,
    };
  },
};
