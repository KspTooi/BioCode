import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetCoreRootListDto,
  GetCoreRootListVo,
  AddCoreRootDto,
  EditCoreRootDto,
} from "@/views/core/api/CoreRootApi.ts";
import CoreRootApi from "@/views/core/api/CoreRootApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

type CoreRootModalForm = {
  id: string;
  name: string;
  expireTime: string;
  adminUsername: string;
  adminPassword: string;
  remark: string;
  status: number;
};

export default {
  /**
   * 租户列表管理
   */
  useCoreRootList() {
    const listForm = ref<GetCoreRootListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      expireTime: "",
      status: null,
    });

    const listData = ref<GetCoreRootListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await CoreRootApi.getCoreRootList(listForm.value);

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
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.name = "";
      listForm.value.expireTime = "";
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetCoreRootListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该条记录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await CoreRootApi.removeCoreRoot({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    // 没有 updateCoreRootStatus 接口，需用 editCoreRoot，只传 id 和 status 字段
    /**
     * 更新状态（需要全量参数，避免接口触发内部错误）
     */
    const updateStatus = async (row: GetCoreRootListVo): Promise<void> => {
      try {
        const details = await CoreRootApi.getCoreRootDetails({ id: row.id });
        // 注意此接口需要传递所有必须字段，否则后端会报错
        // 补充 name, expireTime, remark 字段
        await CoreRootApi.editCoreRoot({
          id: row.id,
          name: row.name,
          expireTime: row.expireTime,
          remark: details.remark ?? "",
          status: row.status,
        });
        ElMessage.success(`${row.name} 状态已更新`);
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message || '状态更新失败');
        // 回滚本地开关状态
        await loadList();
      }
    };

    onMounted(async () => {
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
      updateStatus
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useCoreRootModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<CoreRootModalForm>({
      id: "",
      name: "",
      expireTime: "",
      adminUsername: "",
      adminPassword: "",
      remark: "",
      status: 0,
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入租户名称", trigger: "blur" },
        { max: 40, message: "租户名称长度不能超过40个字符", trigger: "blur" },
      ],
      adminUsername: [
        { required: true, message: "请输入管理员账号", trigger: "blur" },
        { max: 40, message: "管理员账号长度不能超过40个字符", trigger: "blur" },
      ],
      adminPassword: [
        { required: true, message: "请输入管理员密码", trigger: "blur" },
        { max: 40, message: "管理员密码长度不能超过40个字符", trigger: "blur" },
      ],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      status: [{ required: true, message: "请输入状态", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetCoreRootListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.expireTime = "";
        modalForm.adminUsername = "";
        modalForm.adminPassword = "";
        modalForm.remark = "";
        modalForm.status = 0;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        // 除了adminUsername从row取，其他从详情接口获取
        try {
          const details = await CoreRootApi.getCoreRootDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.expireTime = details.expireTime;
          modalForm.adminUsername = row.adminUsername ?? "";
          modalForm.remark = details.remark ?? "";
          modalForm.status = details.status;
        } catch (error: any) {
          ElMessage.error(error.message || "获取详情失败");
          return;
        }

        modalVisible.value = true;
      }
    };

    /**
     * 重置模态框
     */
    const resetModal = (): void => {
      if (!modalFormRef.value) {
        return;
      }
      modalFormRef.value.resetFields();
      modalForm.id = "";
      modalForm.name = "";
      modalForm.expireTime = "";
      modalForm.adminUsername = "";
      modalForm.adminPassword = "";
      modalForm.remark = "";
      modalForm.status = 0;
    };

    /**
     * 提交模态框
     */
    const submitModal = async (): Promise<void> => {
      if (!modalFormRef.value) {
        return;
      }

      try {
        await modalFormRef.value.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      if (modalMode.value === "add") {
        try {
          const addDto: AddCoreRootDto = {
            name: modalForm.name,
            expireTime: modalForm.expireTime,
            adminUsername: modalForm.adminUsername,
            adminPassword: modalForm.adminPassword,
            remark: modalForm.remark,
            status: modalForm.status,
          };
          await CoreRootApi.addCoreRoot(addDto);
          ElMessage.success("新增成功");
          modalVisible.value = false;
          resetModal();
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
        return;
      }

      if (modalMode.value === "edit") {
        if (!modalForm.id) {
          ElMessage.error("缺少ID参数");
          modalLoading.value = false;
          return;
        }

        try {
          const editDto: EditCoreRootDto = {
            id: modalForm.id,
            name: modalForm.name,
            expireTime: modalForm.expireTime,
            // adminUsername: modalForm.adminUsername,
            // adminPassword: modalForm.adminPassword,
            remark: modalForm.remark,
            status: modalForm.status,

          };
          await CoreRootApi.editCoreRoot(editDto);
          ElMessage.success("编辑成功");
          modalVisible.value = false;
          resetModal();
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
      }
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
