import { computed, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetPolyModelListDto,
  GetPolyModelListVo,
  EditPolyModelDto,
  AddPolyModelDto,
} from "@/views/assembly/api/PolyModelApi";
import PolyModelApi from "@/views/assembly/api/PolyModelApi";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";
import RawModelApi, { type GetRawModelDto, type GetRawModelListVo } from "@/views/assembly/api/RawModelApi";

export default {
  /**
   * 原始模型列表管理
   */
  useRawModelList(outputSchemaId: Ref<string>) {
    const listForm = ref<GetRawModelDto>({
      outputSchemaId: outputSchemaId.value,
    });

    const listData = ref<GetRawModelListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await RawModelApi.getRawModelList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    const syncRawModelFromDataSource = async (): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定从数据源重新生成原始模型吗？已有字段将被覆盖。", "生成原始模型", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await RawModelApi.syncRawModelFromDataSource({ id: outputSchemaId.value });
        ElMessage.success("同步操作已完成。");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    return {
      listData,
      listTotal,
      listLoading,
      loadList,
      syncRawModelFromDataSource,
    };
  },

  /**
   * 聚合模型列表管理
   */
  usePolyModelList(outputSchemaId: Ref<string>) {
    const listForm = ref<GetPolyModelListDto>({
      outputSchemaId: outputSchemaId.value,
    });

    const listData = ref<GetPolyModelListVo[]>([]);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = false;
      listForm.value.outputSchemaId = outputSchemaId.value;
      const result = await PolyModelApi.getPolyModelList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    const importFromRaw = async (polyTemplateId?: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定从原始模型同步聚合模型吗？已有字段将被覆盖。", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await PolyModelApi.importFromRaw({ outputSchemaId: outputSchemaId.value, polyTemplateId: polyTemplateId || undefined });
        ElMessage.success("导入成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    const removeList = async (row: GetPolyModelListVo): Promise<void> => {
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
        await PolyModelApi.removePolyModel({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    return {
      listData,
      listLoading,
      loadList,
      removeList,
      importFromRaw,
    };
  },

  /**
   * 聚合模型表格行内编辑
   */
  usePolyModelCellEdit() {
    const buildEditDto = (row: GetPolyModelListVo): EditPolyModelDto => ({
      id: row.id,
      outputSchemaId: row.outputSchemaId,
      name: row.name,
      dataType: row.dataType,
      length: row.length,
      require: row.require,
      policyCrudJson: row.policyCrudJson,
      policyQuery: row.policyQuery,
      policyView: row.policyView,
      pk: row.pk,
      remark: row.remark,
      seq: row.seq,
    });

    const submitRow = async (row: GetPolyModelListVo): Promise<boolean> => {
      const editDto = buildEditDto(row);

      try {
        await PolyModelApi.editPolyModel(editDto);
        return true;
      } catch (error: any) {
        ElMessage.error(error.message);
        return false;
      }
    };

    const commitField = async (row: GetPolyModelListVo, field: string, newValue: any): Promise<boolean> => {
      const oldValue = (row as any)[field];
      if (JSON.stringify(newValue) === JSON.stringify(oldValue)) {
        return false;
      }

      (row as any)[field] = newValue;
      const editDto = buildEditDto(row);

      try {
        await PolyModelApi.editPolyModel(editDto);
        return true;
      } catch (error: any) {
        (row as any)[field] = oldValue;
        ElMessage.error(error.message);
        return false;
      }
    };

    return {
      submitRow,
      commitField,
    };
  },

  /**
   * 新增聚合模型模态框
   */
  usePolyModelAddModal(
    outputSchemaId: Ref<string>,
    modalFormRef: Ref<FormInstance | undefined>,
    reloadCallback: () => Promise<void>
  ) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);

    const modalForm = reactive<AddPolyModelDto>({
      outputSchemaId: outputSchemaId.value,
      name: "",
      dataType: "",
      length: 0,
      require: 0,
      policyCrudJson: [],
      policyQuery: 0,
      policyView: 0,
      remark: "",
      pk: 0,
      seq: 0,
    });

    const modalRequireChecked = computed({
      get: () => modalForm.require === 1,
      set: (val: boolean) => {
        modalForm.require = val ? 1 : 0;
      },
    });

    const modalRules: FormRules = {
      name: [{ required: true, message: "请输入字段名", trigger: "blur" }],
      dataType: [{ required: true, message: "请输入数据类型", trigger: "blur" }],
      length: [
        { required: false, message: "请输入长度", trigger: "blur" },
        { type: "number", min: 0, max: 65535, message: "长度只能在0-65535之间", trigger: "blur" },
      ],
      require: [{ required: true, message: "请选择必填", trigger: "change" }],
      policyCrudJson: [{ required: true, message: "请选择可见性策略", trigger: "change" }],
      policyQuery: [{ required: true, message: "请选择查询策略", trigger: "change" }],
      policyView: [{ required: true, message: "请选择显示策略", trigger: "change" }],
      remark: [{ required: true, message: "请输入字段备注", trigger: "blur" }],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    };

    const openModal = (): void => {
      modalForm.outputSchemaId = outputSchemaId.value;
      modalForm.name = "";
      modalForm.dataType = "";
      modalForm.length = 0;
      modalForm.require = 0;
      modalForm.policyCrudJson = [];
      modalForm.policyQuery = 0;
      modalForm.policyView = 0;
      modalForm.remark = "";
      modalForm.pk = 0;
      modalForm.seq = 0;
      modalVisible.value = true;
    };

    const resetModal = (): void => {
      modalFormRef.value?.resetFields();
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

      try {
        await PolyModelApi.addPolyModel(modalForm);
        ElMessage.success("新增成功");
        modalVisible.value = false;
        await reloadCallback();
      } catch (error: any) {
        ElMessage.error(error.message);
      }

      modalLoading.value = false;
    };

    return {
      modalVisible,
      modalLoading,
      modalForm,
      modalRequireChecked,
      modalRules,
      openModal,
      resetModal,
      submitModal,
    };
  },
};
