import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetTymSchemaListDto,
  GetTymSchemaListVo,
  GetTymSchemaDetailsVo,
  AddTymSchemaDto,
  EditTymSchemaDto,
  CopyTymSchemaDto,
} from "@/views/assembly/api/TymSchemaApi";
import TymSchemaApi from "@/views/assembly/api/TymSchemaApi";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit" | "copy";

export default {
  /**
   * 类型映射方案表列表管理
   */
  useTymSchemaList() {
    const listForm = ref<GetTymSchemaListDto>({
      pageNum: 1,
      pageSize: 20,
      id: "",
      name: "",
      code: "",
    });

    const listData = ref<GetTymSchemaListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await TymSchemaApi.getTymSchemaList(listForm.value);

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
      listForm.value.id = "";
      listForm.value.name = "";
      listForm.value.code = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetTymSchemaListVo): Promise<void> => {
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
        await TymSchemaApi.removeTymSchema({ id: row.id });
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
  useTymSchemaModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetTymSchemaDetailsVo>({
      id: "",
      name: "",
      code: "",
      typeCount: 0,
      defaultType: "",
      seq: 0,
      remark: "",
      createTime: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入方案名称", trigger: "blur" },
        { max: 32, message: "方案名称长度不能超过32个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入方案编码", trigger: "blur" },
        { max: 32, message: "方案编码长度不能超过32个字符", trigger: "blur" },
      ],
      defaultType: [
        { required: true, message: "请输入默认类型", trigger: "blur" },
        { max: 80, message: "默认类型长度不能超过80个字符", trigger: "blur" },
      ],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetTymSchemaListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.typeCount = 0;
        modalForm.defaultType = "";
        modalForm.seq = 0;
        modalForm.remark = "";
        modalForm.createTime = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "copy") {
        if (!row) {
          ElMessage.error("未选择要复制的数据");
          return;
        }
        modalForm.id = row.id;
        modalForm.name = row.name;
        modalForm.code = row.code;
        modalForm.typeCount = 0;
        modalForm.defaultType = "";
        modalForm.seq = 0;
        modalForm.remark = "";
        modalForm.createTime = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await TymSchemaApi.getTymSchemaDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.typeCount = details.typeCount;
          modalForm.defaultType = details.defaultType;
          modalForm.seq = details.seq;
          modalForm.remark = details.remark;
          modalForm.createTime = details.createTime;
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
      modalForm.typeCount = 0;
      modalForm.defaultType = "";
      modalForm.seq = 0;
      modalForm.remark = "";
      modalForm.createTime = "";
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
          const addDto: AddTymSchemaDto = {
            name: modalForm.name,
            code: modalForm.code,
            defaultType: modalForm.defaultType,
            seq: modalForm.seq,
            remark: modalForm.remark,
          };
          await TymSchemaApi.addTymSchema(addDto);
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

      if (modalMode.value === "copy") {
        if (!modalForm.name || !modalForm.code) {
          ElMessage.error("方案名称和编码不能为空");
          modalLoading.value = false;
          return;
        }
        try {
          const copyDto: CopyTymSchemaDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
          };
          await TymSchemaApi.copyTymSchema(copyDto);
          ElMessage.success("复制成功");
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
          const editDto: EditTymSchemaDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            defaultType: modalForm.defaultType,
            seq: modalForm.seq,
            remark: modalForm.remark,
          };
          await TymSchemaApi.editTymSchema(editDto);
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
