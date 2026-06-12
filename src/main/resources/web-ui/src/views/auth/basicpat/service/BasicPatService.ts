import { ref, reactive, onMounted, type Ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import type {
  GetBasicPatListDto,
  GetBasicPatListVo,
  GetBasicPatDetailsVo,
  AddBasicPatDto,
  EditBasicPatDto,
} from "@/views/auth/basicpat/api/BasicPatApi.ts";
import BasicPatApi from "@/views/auth/basicpat/api/BasicPatApi.ts";
import { Result } from "@/commons/model/Result";

type ModalMode = "add" | "edit";

export default {
  /**
   * 基本PAT列表管理
   */
  useBasicPatList() {
    const listForm = ref<GetBasicPatListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      status: null,
    });

    const listData = ref<GetBasicPatListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载基本PAT列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await BasicPatApi.getBasicPatList(listForm.value);
      listLoading.value = false;
      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        return;
      }
      ElMessage.error(result.message || "加载列表失败");
    };

    /**
     * 重置查询条件并刷新列表
     */
    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.name = "";
      listForm.value.status = null;
      loadList();
    };

    /**
     * 删除单条基本PAT，含二次确认
     */
    const removeList = async (row: GetBasicPatListVo): Promise<void> => {
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
        await BasicPatApi.removeBasicPat({ id: String(row.id) });
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
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useBasicPatModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");

    const modalForm = reactive<GetBasicPatDetailsVo>({
      id: "",
      name: "",
      patPt: "",
      expire: "",
      status: 1,
      createTime: "",
    });

    const modalRules = reactive<FormRules>({
      name: [
        { required: true, message: "请输入PAT名称", trigger: "blur" },
        { max: 40, message: "PAT名称长度不能超过40个字符", trigger: "blur" },
      ],
    });

    /**
     * 重置模态框表单状态
     */
    const resetModal = (): void => {
      modalFormRef.value?.resetFields();
      modalMode.value = "add";
      modalForm.id = "";
      modalForm.name = "";
      modalForm.patPt = "";
      modalForm.expire = "";
      modalForm.status = 1;
      modalForm.createTime = "";
    };

    /**
     * 打开模态框，add 模式直接打开，edit 模式先加载详情
     */
    const openModal = async (mode: ModalMode, row: GetBasicPatListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.patPt = "";
        modalForm.expire = "";
        modalForm.status = 1;
        modalForm.createTime = "";
        modalVisible.value = true;
        return;
      }

      if (!row) {
        ElMessage.error("未选择要编辑的数据");
        return;
      }

      try {
        const details = await BasicPatApi.getBasicPatDetails({ id: String(row.id) });
        modalForm.id = details.id;
        modalForm.name = details.name;
        modalForm.patPt = details.patPt;
        modalForm.expire = details.expire;
        modalForm.status = details.status;
        modalForm.createTime = details.createTime;
        modalVisible.value = true;
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 校验表单并提交新增或编辑
     */
    const submitModal = async (): Promise<void> => {
      try {
        await modalFormRef?.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;
      if (modalMode.value === "add") {
        const addDto: AddBasicPatDto = {
          name: modalForm.name,
        };
        try {
          const token = await BasicPatApi.addBasicPat(addDto);
          ElMessage.success({ message: `创建成功，令牌仅展示一次: ${token}`, duration: 12000, showClose: true });
          modalVisible.value = false;
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
        return;
      }
      const editDto: EditBasicPatDto = {
        id: modalForm.id,
        name: modalForm.name,
        status: modalForm.status,
      };
      try {
        await BasicPatApi.editBasicPat(editDto);
        ElMessage.success("编辑成功");
        modalVisible.value = false;
        reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
      modalLoading.value = false;
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
