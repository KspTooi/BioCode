import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpCapApi from "@/views/aacp/api/AacpCapApi.ts";
import AacpMicroFuncApi from "@/views/aacp/api/AacpMicroFuncApi.ts";
import type { GetCapListDto, GetCapListVo, GetCapDetailsVo } from "@/views/aacp/api/AacpCapApi.ts";
import type { GetMicroFuncListVo } from "@/views/aacp/api/AacpMicroFuncApi.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

const PERSIST_KEY = "aacp-cap";

type ModalMode = "add" | "edit";

export default {
  useCapList() {
    const listForm = reactive<GetCapListDto>({
      name: null,
      kind: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetCapListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpCapApi.getCapList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
        QueryPersistService.persistQuery(PERSIST_KEY, listForm);
      } catch {
        ElMessage.error("加载能力包列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.name = null;
      listForm.kind = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      loadList();
      QueryPersistService.clearQuery(PERSIST_KEY);
    };

    const removeList = async (row: GetCapListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定删除能力包 [${row.name}] 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await AacpCapApi.removeCap(row.id.toString());
        ElMessage.success("删除能力包成功");
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

  useCapModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetCapDetailsVo>({
      id: null,
      name: null,
      kind: null,
      remark: null,
      funcIds: [],
    });

    // 微函数选择器
    const funcOptions = ref<GetMicroFuncListVo[]>([]);
    const funcLoading = ref(false);

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入能力包名称", trigger: "blur" },
        { max: 40, message: "能力包名称长度不能超过40", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请选择类型", trigger: "change" }],
      funcIds: [
        {
          validator: (_rule, _value, callback) => {
            if (modalForm.funcIds.length > 50) {
              callback(new Error("一个能力包最多绑定50个微函数"));
              return;
            }
            callback();
          },
          trigger: "change",
        },
      ],
      remark: [{ max: 500, message: "备注长度不能超过500", trigger: "blur" }],
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.kind = 0;
      modalForm.remark = null;
      modalForm.funcIds = [];
      funcOptions.value = [];
    };

    /** 一次全量加载微函数选项（pageSize 写死 10000） */
    const loadFuncOptions = async (): Promise<void> => {
      if (funcOptions.value.length > 0) {
        return;
      }
      funcLoading.value = true;
      try {
        const res = await AacpMicroFuncApi.getMicroFuncList({
          name: null,
          code: null,
          description: null,
          pageNum: 1,
          pageSize: 10000,
        });
        funcOptions.value = res.data;
      } catch {
        ElMessage.error("加载微函数列表失败");
      } finally {
        funcLoading.value = false;
      }
    };

    const openModal = async (mode: ModalMode, row: GetCapListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      await loadFuncOptions();

      if (mode === "edit" && row) {
        try {
          const res = await AacpCapApi.getCapDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.kind = res.kind;
          modalForm.remark = res.remark;
          modalForm.funcIds = res.funcIds;
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
          await AacpCapApi.addCap({
            name: modalForm.name,
            kind: modalForm.kind,
            remark: modalForm.remark,
            funcIds: modalForm.funcIds,
          });
          ElMessage.success("新增能力包成功");
        }

        if (modalMode.value === "edit") {
          await AacpCapApi.editCap({
            id: modalForm.id,
            name: modalForm.name,
            kind: modalForm.kind,
            remark: modalForm.remark,
            funcIds: modalForm.funcIds,
          });
          ElMessage.success("编辑能力包成功");
        }

        reloadCallback();
        modalVisible.value = false;
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
      funcOptions,
      funcLoading,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
