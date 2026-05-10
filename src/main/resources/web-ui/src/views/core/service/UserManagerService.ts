import { computed, onMounted, reactive, ref, type Ref } from "vue";
import type {
  GetUserDetailsVo,
  GetUserListDto,
  GetUserListVo,
  AddUserDto,
  EditUserDto,
  UserGroupVo,
  BatchEditUserDto,
} from "@/views/core/api/UserApi.ts";
import UserAuthService from "@/views/auth/service/UserAuthService";
const { AuthStore } = UserAuthService;
import AdminUserApi from "@/views/core/api/UserApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";
import GroupApi from "@/views/auth/api/GroupApi.ts";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi.ts";

export default {
  /**
   * 用户列表打包
   */
  useUserList() {
    const listForm = ref<GetUserListDto>({
      pageNum: 1,
      pageSize: 20,
      username: "",
      status: null,
      orgId: null,
      rootName: "",
    });

    const listData = ref<GetUserListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载用户列表
     */
    const loadList = async (orgId?: string | null): Promise<void> => {
      listForm.value.orgId = orgId ?? null;
      listLoading.value = true;
      const result = await AdminUserApi.getUserList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("user-manager", listForm.value);
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    /**
     * 重置查询条件
     */
    const resetList = (orgId?: string | null): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.username = "";
      listForm.value.status = null;
      listForm.value.nickname = "";
      listForm.value.rootName = "";
      QueryPersistService.clearQuery("user-manager");
      loadList(orgId ?? null);
    };

    /**
     * 删除用户
     */
    const removeList = async (user: GetUserListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该用户吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AdminUserApi.removeUser({ id: user.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    //初始化
    onMounted(async () => {
      QueryPersistService.loadQuery("user-manager", listForm.value);
      await loadList();
    });

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      loadList,
      resetList,
      removeList,
    };
  },

  /**
   * 用户模态框打包
   * @param modalFormRef 模态框表单引用
   * @param loadList 列表加载函数
   * @param orgId 组织ID
   */
  useUserModal(modalFormRef: Ref<FormInstance>, loadList: () => void, orgId: Ref<string | null>) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<"add" | "edit">("add");
    const modalCurrentRow = ref<GetUserListVo | null>(null);
    const modalForm = reactive<GetUserDetailsVo>({
      id: "",
      orgId: "",
      username: "",
      nickname: "",
      gender: 0,
      phone: "",
      email: "",
      status: 1,
      createTime: "",
      lastLoginTime: "",
      isSystem: 0,
      groups: [],
      permissions: [],
    });
    const modalFormPassword = ref("");
    const selectedGroupIds = ref<string[]>([]);
    const groupOptions = ref<UserGroupVo[]>([]);
    const orgTreeOptions = ref<any[]>([]);

    /**
     * 处理组织架构树数据，禁用企业节点
     */
    const processOrgTreeData = (treeData: GetOrgTreeVo[]): any[] => {
      return treeData.map((node) => {
        const processedNode: any = {
          id: node.id,
          topId: node.topId,
          parentId: node.parentId,
          kind: node.kind,
          name: node.name,
          seq: node.seq,
          disabled: node.kind === 1,
          children: node.children,
        };
        if (node.children && node.children.length > 0) {
          processedNode.children = processOrgTreeData(node.children);
        }
        return processedNode;
      });
    };

    const modalRules = {
      username: [
        {
          trigger: "blur",
          validator: (rule: any, value: string, callback: (error?: string | Error) => void) => {
            if (modalMode.value === "edit") {
              callback();
              return;
            }
            if (!value) {
              callback(new Error("请输入用户名"));
              return;
            }
            if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) {
              callback(new Error("用户名只能包含4-20位字母、数字和下划线"));
              return;
            }
            callback();
          },
        },
      ],
      nickname: [{ max: 50, message: "昵称长度不能超过50个字符", trigger: "blur" }],
      password: [
        {
          trigger: "blur",
          validator: (rule: any, value: string, callback: (error?: string | Error) => void) => {
            const password = modalFormPassword.value;
            if (modalMode.value === "add" && !password) {
              callback(new Error("请输入密码"));
              return;
            }
            if (password && password.length > 128) {
              callback(new Error("密码长度不能超过128个字符"));
              return;
            }
            if (password && password.length < 6) {
              callback(new Error("密码长度不能少于6位"));
              return;
            }
            callback();
          },
        },
      ],
      email: [
        { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
        { max: 64, message: "邮箱长度不能超过64个字符", trigger: "blur" },
      ],
      gender: [{ required: true, message: "请选择性别", trigger: "change" }],
      phone: [{ max: 64, message: "手机号长度不能超过64个字符", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式
     * @param currentRow 当前行
     */
    const openModal = async (mode: "add" | "edit", currentRow: GetUserListVo | null): Promise<void> => {
      modalMode.value = mode;
      modalCurrentRow.value = currentRow;
      resetModal();

      // 获取组织架构树并处理，禁用企业节点
      const treeData = await OrgApi.getOrgTree({});
      orgTreeOptions.value = treeData;

      //如果是编辑模式则需要加载详情数据
      if (mode === "edit" && currentRow) {
        try {
          const ret = await AdminUserApi.getUserDetails({ id: currentRow.id });

          modalForm.id = ret.id;
          modalForm.username = ret.username;
          modalForm.nickname = ret.nickname || "";
          modalForm.gender = ret.gender ?? 0;
          modalForm.phone = ret.phone || "";
          modalForm.email = ret.email || "";
          modalForm.status = ret.status;
          modalForm.isSystem = ret.isSystem ?? 0;
          modalForm.groups = ret.groups || [];
          modalForm.orgId = ret.orgId;

          groupOptions.value = ret.groups || [];
          selectedGroupIds.value = ret.groups ? ret.groups.filter((group) => group.hasGroup).map((group) => group.id) : [];

          //编辑时滤除当前用户不拥有 且被禁用的组
          groupOptions.value = ret.groups.filter((group) => {
            //当前用户拥有该组
            if (group.hasGroup) {
              return true;
            }

            //组被禁用且未拥有该组
            if (group.status === 0 && !group.hasGroup) {
              return false;
            }

            return true;
          });
        } catch (error: any) {
          ElMessage.error(error.message);
          return;
        }
      }

      if (mode == "add") {
        // 新增模式，获取用户组列表
        const groups = await GroupApi.getGroupList({ pageNum: 1, pageSize: 100000, status: 1 });
        groupOptions.value = [];
        //侧边栏如果选中组织，则新增默认选中组织
        if (orgTreeOptions.value.length > 0) {
          modalForm.orgId = orgId.value;
        }

        groups.data.forEach((group) => {
          //禁用组不显示
          if (group.status === 0) {
            return;
          }

          groupOptions.value.push({
            id: group.id,
            name: group.name,
            remark: "",
            seq: 0,
            isSystem: group.isSystem === 1 ? true : false, // 1:是 0:否
            hasGroup: false,
            status: group.status,
          });
        });
      }

      modalVisible.value = true;
    };

    /**
     * 重置模态框表单
     */
    const resetModal = (): void => {
      modalForm.id = "";
      modalForm.username = "";
      modalForm.nickname = "";
      modalForm.gender = 0;
      modalForm.phone = "";
      modalForm.email = "";
      modalForm.status = 1;
      modalForm.isSystem = 0;
      modalForm.groups = [];
      modalForm.orgId = "";
      modalFormPassword.value = "";
      selectedGroupIds.value = [];

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
        if (modalMode.value === "add") {
          const addDto: AddUserDto = {
            username: modalForm.username,
            password: modalFormPassword.value,
            nickname: modalForm.nickname,
            gender: modalForm.gender,
            phone: modalForm.phone,
            email: modalForm.email,
            status: modalForm.status,
            orgId: modalForm.orgId,
            groupIds: selectedGroupIds.value,
          };
          const result = await AdminUserApi.addUser(addDto);
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
          const editDto: EditUserDto = {
            id: modalForm.id,
            username: modalForm.username,
            nickname: modalForm.nickname,
            gender: modalForm.gender,
            phone: modalForm.phone,
            email: modalForm.email,
            status: modalForm.status,
            orgId: modalForm.orgId,
            groupIds: selectedGroupIds.value,
          };
          if (modalFormPassword.value) {
            editDto.password = modalFormPassword.value;
          }
          const result = await AdminUserApi.editUser(editDto);
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

    return {
      modalVisible,
      modalLoading,
      modalMode,
      modalCurrentRow,
      modalForm,
      modalFormPassword,
      selectedGroupIds,
      groupOptions,
      modalRules,
      openModal,
      resetModal,
      submitModal,
      orgTreeOptions,
    };
  },

  /**
   * 批量操作打包
   * @param loadList 列表加载函数
   * @param deptSelectModalRef 部门选择器 ref
   */
  useBatchAction(loadList: () => void) {
    //组织机构选择器
    const modalOrgTreeVisible = ref(false);
    const modalOrgTreeValues = ref<GetOrgTreeVo[]>([]);

    const selectedRows = ref<GetUserListVo[]>([]);
    const batchCount = ref(0);

    const onSelectionChange = (rows: GetUserListVo[]): void => {
      selectedRows.value = rows;
      batchCount.value = rows.length;
    };

    /**
     * 提交组织机构选择器
     * @param checkedOrgIds 已勾选的组织机构ID列表
     */
    const onSubmitChangeOrg = async (checkedOrgIds: string[]): Promise<void> => {
      //获取选中的用户ID列表
      const ids = selectedRows.value.map((row) => row.id);

      if (ids.length < 1 || checkedOrgIds.length < 1) {
        ElMessage.error("未选择用户或组织机构");
        return;
      }
      try {
        const res = await AdminUserApi.batchEditUser({ ids, kind: 3, orgId: checkedOrgIds[0] });
        if (Result.isError(res)) {
          ElMessage.error(res.message);
          return;
        }
        ElMessage.success("批量操作成功");
        loadList();
      } catch (error) {
        ElMessage.error(error.message);
        return;
      }
    };

    /**
     * 批量操作调度器
     * @param command 操作指令: enable, disable, remove, changeDept
     */
    const onBatchAction = async (command: string): Promise<void> => {
      const ids = selectedRows.value.map((row) => row.id);
      if (ids.length === 0) {
        return;
      }

      let kind = 0;
      const orgId = null; // 将deptId转成了orgId

      // 处理变更部门：需要先选择部门
      if (command === "changeDept") {
        modalOrgTreeVisible.value = true;
        return;
      }

      // 处理批量启用：需要确认
      if (command === "enable") {
        kind = 0;
        try {
          await ElMessageBox.confirm(`确定要批量启用选中的 ${ids.length} 个用户吗？`, "提示", {
            type: "info",
            confirmButtonText: "确定",
            cancelButtonText: "取消",
          });
        } catch {
          return;
        }
      }

      // 处理批量封禁：需要确认
      if (command === "disable") {
        kind = 1;
        try {
          await ElMessageBox.confirm(`确定要批量封禁选中的 ${ids.length} 个用户吗？`, "警告", {
            type: "warning",
            confirmButtonText: "确定",
            cancelButtonText: "取消",
          });
        } catch {
          return;
        }
      }

      // 处理批量删除：需要确认
      if (command === "remove") {
        kind = 2;
        try {
          await ElMessageBox.confirm(`确定要批量删除选中的 ${ids.length} 个用户吗？`, "警告", {
            type: "warning",
            confirmButtonText: "确定",
            cancelButtonText: "取消",
          });
        } catch {
          return;
        }
      }

      const dto: BatchEditUserDto = { ids, kind, orgId: orgId };

      // 执行批量操作
      try {
        const res = await AdminUserApi.batchEditUser(dto);
        if (Result.isError(res)) {
          ElMessage.error(res.message);
          return;
        }

        ElMessage.success("批量操作成功");
        loadList();
      } catch (err: any) {
        ElMessage.error(err.message || "操作失败");
      }
    };

    return {
      onBatchAction,
      onSelectionChange,
      onSubmitChangeOrg,
      modalOrgTreeVisible,
      modalOrgTreeValues,
      canBatchAction: computed(() => batchCount.value > 0),
      batchCount,
    };
  },
};
