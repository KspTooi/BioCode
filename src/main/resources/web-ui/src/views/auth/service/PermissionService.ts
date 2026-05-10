import { ref, reactive, onMounted, type Ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import AdminPermissionApi, {
  type GetPermissionDetailsVo,
  type GetPermissionListDto,
  type GetPermissionListVo,
  type AddPermissionDto,
  type EditPermissionDto,
} from "@/views/auth/api/PermissionApi.ts";
import { Result } from "@/commons/model/Result.ts";

export default {
  /**
   * 权限列表打包
   */
  usePermissionList() {
    const listForm = reactive<GetPermissionListDto>({
      code: null,
      name: null,
      pageNum: 1,
      pageSize: 20,
    });
    const listData = ref<GetPermissionListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AdminPermissionApi.getPermissionList(listForm);

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
      listForm.code = null;
      listForm.name = null;
      loadList();
    };

    /**
     * 删除项
     */
    const removeList = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该权限吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AdminPermissionApi.removePermission({ id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 批量删除
     */
    const removeListBatch = async (selectedItems: GetPermissionListVo[]): Promise<void> => {
      if (selectedItems.length === 0) {
        ElMessage.warning("请选择要删除的权限节点");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.length} 个权限节点吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        const ids = selectedItems.map((item) => item.id);
        await AdminPermissionApi.removePermission({ ids });
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
   * 权限模态框打包
   */
  usePermissionModal(modalFormRef: Ref<FormInstance | undefined>, loadList: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<"add" | "edit">("add");
    const modalForm = reactive<GetPermissionDetailsVo>({
      id: "",
      name: "",
      code: "",
      remark: "",
      seq: 0,
      isSystem: 0,
      createTime: "",
      creatorId: "",
      updateTime: "",
      updaterId: "",
    });

    const modalRules = {
      code: [
        { required: true, message: "请输入权限代码", trigger: "blur" },
        {
          pattern: /^[a-z]([a-z0-9-]*[a-z0-9])*(:[a-z]([a-z0-9-]*[a-z0-9])*)*$/,
          message: "权限标识格式错误，只允许小写字母、数字、连字符，以及冒号作为分隔符",
          trigger: "blur",
        },
      ],
      name: [
        { required: true, message: "请输入权限名称", trigger: "blur" },
        { min: 1, max: 32, message: "长度在 1 到 32 个字符", trigger: "blur" },
      ],
      remark: [],
      seq: [{ required: true, message: "请输入排序号", trigger: "blur" }],
    };

    /**
     * 重置模态框
     */
    const resetModal = (): void => {
      modalForm.id = "";
      modalForm.name = "";
      modalForm.code = "";
      modalForm.remark = "";
      modalForm.seq = 0;
      modalForm.isSystem = 0;
      modalForm.createTime = "";
      modalForm.creatorId = "";
      modalForm.updateTime = "";
      modalForm.updaterId = "";

      if (modalFormRef.value) {
        modalFormRef.value.resetFields();
      }
    };

    /**
     * 打开模态框
     */
    const openModal = async (mode: "add" | "edit", row: GetPermissionListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "edit" && row) {
        try {
          const ret = await AdminPermissionApi.getPermissionDetails({ id: row.id });
          modalForm.id = ret.id;
          modalForm.name = ret.name;
          modalForm.code = ret.code;
          modalForm.remark = ret.remark;
          modalForm.seq = ret.seq;
          modalForm.isSystem = ret.isSystem;
          modalForm.createTime = ret.createTime;
          modalForm.creatorId = ret.creatorId;
          modalForm.updateTime = ret.updateTime;
          modalForm.updaterId = ret.updaterId;
        } catch (error: any) {
          ElMessage.error(error.message || "获取权限详情失败");
          return;
        }
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
          const addDto: AddPermissionDto = {
            name: modalForm.name,
            code: modalForm.code,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          const result = await AdminPermissionApi.addPermission(addDto);
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
          const editDto: EditPermissionDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          const result = await AdminPermissionApi.editPermission(editDto);
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
      modalForm,
      modalRules,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
