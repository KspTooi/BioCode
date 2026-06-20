import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance } from "element-plus";
import type {
  GetOpRcdListDto,
  GetOpRcdListVo,
  GetOpRcdDetailsVo,
} from "@/views/assembly/api/OpRcdApi.ts";
import OpRcdApi from "@/views/assembly/api/OpRcdApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  /**
   * 输出方案执行记录列表管理
   */
  useOpRcdList() {
    const listForm = ref<GetOpRcdListDto>({
      pageNum: 1,
      pageSize: 20,
      opName: "",
      dsName: "",
      dsTableName: "",
      modelName: "",
      bizDomain: "",
      creatorUsername: "",
    });

    const listData = ref<GetOpRcdListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await OpRcdApi.getOpRcdList(listForm.value);

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
      listForm.value.opName = "";
      listForm.value.dsName = "";
      listForm.value.dsTableName = "";
      listForm.value.modelName = "";
      listForm.value.bizDomain = "";
      listForm.value.creatorUsername = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetOpRcdListVo): Promise<void> => {
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
        await OpRcdApi.removeOpRcd({ id: row.id });
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
   * 查看详情模态框管理（只读）
   */
  useOpRcdViewModal(modalFormRef: Ref<FormInstance | undefined>) {
    const modalVisible = ref(false);
    const modalForm = reactive<GetOpRcdDetailsVo>({
      id: "",
      opName: "",
      dsName: "",
      dsTableName: "",
      dsUrl: "",
      scmInputUrl: "",
      scmOutputUrl: "",
      modelName: "",
      modelRemark: "",
      bizDomain: "",
      qbeParams: "",
      startTime: "",
      endTime: "",
      durationMs: 0,
      creatorUsername: "",
    });

    /**
     * 打开查看详情模态框，加载详情数据
     */
    const openViewModal = async (row: GetOpRcdListVo): Promise<void> => {
      try {
        const details = await OpRcdApi.getOpRcdDetails({ id: row.id });
        modalForm.id = details.id;
        modalForm.opName = details.opName;
        modalForm.dsName = details.dsName;
        modalForm.dsTableName = details.dsTableName;
        modalForm.dsUrl = details.dsUrl;
        modalForm.scmInputUrl = details.scmInputUrl;
        modalForm.scmOutputUrl = details.scmOutputUrl;
        modalForm.modelName = details.modelName;
        modalForm.modelRemark = details.modelRemark;
        modalForm.bizDomain = details.bizDomain;
        modalForm.qbeParams = details.qbeParams;
        modalForm.startTime = details.startTime;
        modalForm.endTime = details.endTime;
        modalForm.durationMs = details.durationMs;
        modalForm.creatorUsername = details.creatorUsername;
        modalVisible.value = true;
      } catch (error: any) {
        ElMessage.error(error.message);
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
    };

    return {
      modalVisible,
      modalForm,
      openViewModal,
      resetModal,
    };
  },
};