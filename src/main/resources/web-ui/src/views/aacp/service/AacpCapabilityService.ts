import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpCapabilityApi from "@/views/aacp/api/AacpCapabilityApi.ts";
import type {
  GetAacpCapabilityListDto,
  GetAacpCapabilityListVo,
  GetAacpCapabilityDetailsVo,
} from "@/views/aacp/api/AacpCapabilityApi.ts";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

const PERSIST_KEY = "aacp-capability";

type ModalMode = "add" | "edit";

export default {
  useAacpCapabilityList() {
    const listForm = reactive<GetAacpCapabilityListDto>({
      name: null,
      kind: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetAacpCapabilityListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpCapabilityApi.getAacpCapabilityList(listForm);
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

    const removeList = async (row: GetAacpCapabilityListVo): Promise<void> => {
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
        await AacpCapabilityApi.removeAacpCapability(row.id.toString());
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

  useAacpCapabilityModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpCapabilityDetailsVo>({
      id: null,
      name: null,
      kind: null,
      remark: null,
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入能力包名称", trigger: "blur" },
        { max: 40, message: "能力包名称长度不能超过40", trigger: "blur" },
      ],
      kind: [
        { required: true, message: "请选择类型", trigger: "change" },
      ],
      remark: [
        { max: 500, message: "备注长度不能超过500", trigger: "blur" },
      ],
    };

    const resetModal = (): void => {
      modalForm.id = null;
      modalForm.name = null;
      modalForm.kind = 0;
      modalForm.remark = null;
    };

    const openModal = async (mode: ModalMode, row: GetAacpCapabilityListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      if (mode === "edit" && row) {
        try {
          const res = await AacpCapabilityApi.getAacpCapabilityDetails(row.id.toString());
          modalForm.id = res.id;
          modalForm.name = res.name;
          modalForm.kind = res.kind;
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
          await AacpCapabilityApi.addAacpCapability({
            name: modalForm.name,
            kind: modalForm.kind,
            remark: modalForm.remark,
          });
          ElMessage.success("新增能力包成功");
          resetModal();
        }

        if (modalMode.value === "edit") {
          await AacpCapabilityApi.editAacpCapability({
            id: modalForm.id,
            name: modalForm.name,
            kind: modalForm.kind,
            remark: modalForm.remark,
          });
          ElMessage.success("编辑能力包成功");
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
