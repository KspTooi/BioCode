import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetPackageListDto,
  GetPackageListVo,
  GetPackageDetailsVo,
  AddPackageDto,
  EditPackageDto,
} from "../api/PackApi.ts";
import PackageApi from "@/views/core/api/PackApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 菜单包列表管理
   */
  usePackageList() {
    const listForm = ref<GetPackageListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
      status: null,
    });

    const listData = ref<GetPackageListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await PackageApi.getPackageList(listForm.value);

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
      listForm.value.code = "";
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetPackageListVo): Promise<void> => {
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
        await PackageApi.removePackage({ id: row.id });
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
   * 模态框管理（统一处理新增和编辑）
   */
  usePackageModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetPackageDetailsVo>({
      id: "",
      name: "",
      code: "",
      status: 0,
      seq: 0,
      remark: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入菜单包名", trigger: "blur" },
        { max: 40, message: "菜单包名长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入菜单包编码", trigger: "blur" },
        { max: 16, message: "菜单包编码长度不能超过16个字符", trigger: "blur" },
      ],
      status: [{ required: true, message: "请输入状态 0:禁用 1:启用", trigger: "blur" }],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
      remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetPackageListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.status = 0;
        modalForm.seq = 0;
        modalForm.remark = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await PackageApi.getPackageDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.status = details.status;
          modalForm.seq = details.seq;
          modalForm.remark = details.remark;
          modalVisible.value = true;
        } catch (error: any) {
          ElMessage.error(error.message);
        }
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
      modalForm.code = "";
      modalForm.status = 0;
      modalForm.seq = 0;
      modalForm.remark = "";
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
          const addDto: AddPackageDto = {
            name: modalForm.name,
            code: modalForm.code,
            status: modalForm.status,
            seq: modalForm.seq,
            remark: modalForm.remark,
          };
          await PackageApi.addPackage(addDto);
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
          const editDto: EditPackageDto = {
            id: modalForm.id,
            name: modalForm.name,
            status: modalForm.status,
            seq: modalForm.seq,
            remark: modalForm.remark,
          };
          await PackageApi.editPackage(editDto);
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
