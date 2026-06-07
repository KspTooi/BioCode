import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpFuncApi from "@/views/aacp/api/AacpFuncApi.ts";
import type {
  GetAacpFuncListDto,
  GetAacpFuncListVo,
  GetAacpFuncDetailsVo,
} from "@/views/aacp/api/AacpFuncApi.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

const PERSIST_KEY = "aacp-func";

type ModalMode = "add" | "edit";

export default {
  useAacpFuncList() {
    const listForm = reactive<GetAacpFuncListDto>({
      name: null,
      code: null,
      description: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetAacpFuncListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpFuncApi.getAacpFuncList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
        QueryPersistService.persistQuery(PERSIST_KEY, listForm);
      } catch {
        ElMessage.error("加载微函数列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.name = null;
      listForm.code = null;
      listForm.description = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      loadList();
      QueryPersistService.clearQuery(PERSIST_KEY);
    };

    const removeList = async (row: GetAacpFuncListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定删除微函数 [${row.name}] 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AacpFuncApi.removeAacpFunc(row.id.toString());
        ElMessage.success("删除微函数成功");
        loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery(PERSIST_KEY, listForm);
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

  useAacpFuncModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpFuncDetailsVo>({
      id: null,
      name: null,
      code: null,
      description: null,
      schema: null,
      target: null,
      remark: null,
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入微函数名称", trigger: "blur" },
        { max: 40, message: "微函数名称长度不能超过40", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入微函数标识", trigger: "blur" },
        { max: 32, message: "微函数标识长度不能超过32", trigger: "blur" },
      ],
      description: [
        { required: true, message: "请输入意图词", trigger: "blur" },
        { max: 1000, message: "意图词长度不能超过1000", trigger: "blur" },
      ],
      target: [
        { required: true, message: "请输入调用目标Bean", trigger: "blur" },
        { max: 1000, message: "调用目标Bean长度不能超过1000", trigger: "blur" },
      ],
      remark: [
        { max: 500, message: "备注长度不能超过500", trigger: "blur" },
      ],
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.code = null;
      modalForm.description = null;
      modalForm.schema = null;
      modalForm.target = null;
      modalForm.remark = null;
    };

    const openModal = async (mode: ModalMode, row: GetAacpFuncListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "edit" && row) {
        try {
          const res = await AacpFuncApi.getAacpFuncDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.code = res.code;
          modalForm.description = res.description;
          modalForm.schema = res.schema;
          modalForm.target = res.target;
          modalForm.remark = res.remark;
        } catch (error: any) {
          ElMessage.error(error.message);
          return;
        }
      }

      modalVisible.value = true;
    };

    const submitModal = async (): Promise<void> => {
      try {
        await modalFormRef.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      try {
        if (modalMode.value === "add") {
          await AacpFuncApi.addAacpFunc({
            name: modalForm.name,
            code: modalForm.code,
            description: modalForm.description,
            schema: modalForm.schema,
            target: modalForm.target,
            remark: modalForm.remark,
          });
          ElMessage.success("新增微函数成功");
          resetModal();
        }

        if (modalMode.value === "edit") {
          await AacpFuncApi.editAacpFunc({
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            description: modalForm.description,
            schema: modalForm.schema,
            target: modalForm.target,
            remark: modalForm.remark,
          });
          ElMessage.success("编辑微函数成功");
        }

        reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
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
