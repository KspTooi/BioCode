import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetCoreRootListDto,
  GetCoreRootListVo,
  GetCoreRootDetailsVo,
  AddCoreRootDto,
  EditCoreRootDto,
} from "@/views/core/api/CoreRootApi.ts";
import CoreRootApi from "@/views/core/api/CoreRootApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

type ModalMode = "add" | "edit";

export default {
  /**
   * 租户列表管理
   */
  useCoreRootList() {
    const listForm = ref<GetCoreRootListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      expireTimeRangeStart: "",
      expireTimeRangeEnd: "",
      status: null,
    });

    const listData = ref<GetCoreRootListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

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

    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.name = "";
      listForm.value.expireTimeRangeStart = "";
      listForm.value.expireTimeRangeEnd = "";
      listForm.value.status = null;
      loadList();
    };

    const removeList = async (row: GetCoreRootListVo): Promise<void> => {
      if (row.isSystem === 1) {
        return;
      }
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
    };
  },

  /**
   * 租户模态框管理
   */
  useCoreRootModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetCoreRootDetailsVo>({
      id: "",
      name: "",
      expireTime: "",
      remark: "",
      status: 0,
      isSystem: 0,
      packIds: [],
      _adminUsername: "",
      _adminPassword: "",
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入租户名称", trigger: "blur" },
        { max: 40, message: "租户名称长度不能超过40个字符", trigger: "blur" },
      ],
      _adminUsername: [
        { required: true, message: "请输入管理员账号", trigger: "blur" },
        { max: 40, message: "管理员账号长度不能超过40个字符", trigger: "blur" },
      ],
      _adminPassword: [
        { required: true, message: "请输入管理员密码", trigger: "blur" },
        { max: 40, message: "管理员密码长度不能超过40个字符", trigger: "blur" },
      ],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
      status: [{ required: true, message: "请输入状态", trigger: "blur" }],
    };

    const openModal = async (mode: ModalMode, row: GetCoreRootListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.expireTime = "";
        modalForm.remark = "";
        modalForm.status = 0;
        modalForm.isSystem = 0;
        modalForm.packIds = [];
        modalForm._adminUsername = "";
        modalForm._adminPassword = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await CoreRootApi.getCoreRootDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.expireTime = details.expireTime;
          modalForm.remark = details.remark ?? "";
          modalForm.status = details.status;
          modalForm.isSystem = details.isSystem ?? 0;
          modalForm.packIds = details.packIds ?? [];
          modalForm._adminUsername = row.adminUsername ?? "";
          modalForm._adminPassword = "";
        } catch (error: any) {
          ElMessage.error(error.message || "获取详情失败");
          return;
        }

        modalVisible.value = true;
      }
    };

    const resetModal = (): void => {
      if (!modalFormRef.value) {
        return;
      }
      modalFormRef.value.resetFields();
      modalForm.id = "";
      modalForm.name = "";
      modalForm.expireTime = "";
      modalForm.remark = "";
      modalForm.status = 0;
      modalForm.isSystem = 0;
      modalForm.packIds = [];
      modalForm._adminUsername = "";
      modalForm._adminPassword = "";
    };

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
            adminUsername: modalForm._adminUsername,
            adminPassword: modalForm._adminPassword,
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
