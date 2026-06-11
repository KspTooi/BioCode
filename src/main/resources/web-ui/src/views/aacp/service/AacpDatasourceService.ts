import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAacpDatasourceListDto,
  GetAacpDatasourceListVo,
  GetAacpDatasourceDetailsVo,
  AddAacpDatasourceDto,
  EditAacpDatasourceDto,
} from "@/views/aacp/api/AacpDatasourceApi.ts";
import AacpDatasourceApi from "@/views/aacp/api/AacpDatasourceApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 数据源列表管理
   */
  useAacpDatasourceList() {
    const listForm = ref<GetAacpDatasourceListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
    });

    const listData = ref<GetAacpDatasourceListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await AacpDatasourceApi.getAacpDatasourceList(listForm.value);

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
      listForm.value.code = "";
      loadList();
    };

    const testConnection = async (row: GetAacpDatasourceListVo): Promise<void> => {
      try {
        const msg = await AacpDatasourceApi.testAacpDatasourceConnection({ id: row.id });
        ElMessageBox.alert(msg || "连接成功", "测试结果", { type: "success", confirmButtonText: "确定" });
      } catch (error: any) {
        ElMessageBox.alert(error.message, "测试结果", { type: "error", confirmButtonText: "确定" });
      }
    };

    const removeList = async (row: GetAacpDatasourceListVo): Promise<void> => {
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
        await AacpDatasourceApi.removeAacpDatasource({ id: row.id });
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
      testConnection,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   * @param modalFormRef 表单实例引用
   * @param reloadCallback 提交成功后刷新列表的回调
   */
  useAacpDatasourceModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpDatasourceDetailsVo & { username?: string; password?: string }>({
      id: "",
      name: "",
      code: "",
      kind: 0,
      drive: "",
      url: "",
      username: "",
      password: "",
      defaultDb: "",
      queryMaxRows: 0,
      executeBatch: 0,
    });

    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入数据源名称", trigger: "blur" },
        { max: 40, message: "长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入数据源编码", trigger: "blur" },
        { max: 32, message: "长度不能超过32个字符", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请选择数据源类型", trigger: "blur" }],
      drive: [
        { required: true, message: "请输入JDBC驱动", trigger: "blur" },
        { max: 200, message: "长度不能超过200个字符", trigger: "blur" },
      ],
      url: [{ required: true, message: "请输入连接字符串", trigger: "blur" }],
      username: [{ max: 200, message: "长度不能超过200个字符", trigger: "blur" }],
      password: [{ max: 2000, message: "长度不能超过2000个字符", trigger: "blur" }],
      defaultDb: [
        { required: true, message: "请输入默认数据库", trigger: "blur" },
        { max: 200, message: "长度不能超过200个字符", trigger: "blur" },
      ],
      queryMaxRows: [{ required: true, message: "请输入最大查询行数", trigger: "blur" }],
      executeBatch: [{ required: true, message: "请选择是否支持批处理", trigger: "blur" }],
    };

    const openModal = async (mode: ModalMode, row: GetAacpDatasourceListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.kind = 0;
        modalForm.drive = "com.mysql.cj.jdbc.Driver";
        modalForm.url = "jdbc:mysql://localhost:3306/";
        modalForm.username = "";
        modalForm.password = "";
        modalForm.defaultDb = "";
        modalForm.queryMaxRows = 1000;
        modalForm.executeBatch = 1;
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await AacpDatasourceApi.getAacpDatasourceDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.kind = details.kind;
          modalForm.drive = details.drive;
          modalForm.url = details.url;
          modalForm.username = "";
          modalForm.password = "";
          modalForm.defaultDb = details.defaultDb;
          modalForm.queryMaxRows = details.queryMaxRows;
          modalForm.executeBatch = details.executeBatch;
          modalVisible.value = true;
        } catch (error: any) {
          ElMessage.error(error.message);
        }
      }
    };

    const resetModal = (): void => {
      if (!modalFormRef.value) {
        return;
      }
      modalFormRef.value.resetFields();
      modalForm.id = "";
      modalForm.name = "";
      modalForm.code = "";
      modalForm.kind = 0;
      modalForm.drive = "";
      modalForm.url = "";
      modalForm.username = "";
      modalForm.password = "";
      modalForm.defaultDb = "";
      modalForm.queryMaxRows = 0;
      modalForm.executeBatch = 1;
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
          const addDto: AddAacpDatasourceDto = {
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            drive: modalForm.drive,
            url: modalForm.url,
            username: modalForm.username,
            password: modalForm.password,
            defaultDb: modalForm.defaultDb,
            queryMaxRows: modalForm.queryMaxRows,
            executeBatch: modalForm.executeBatch,
          };
          await AacpDatasourceApi.addAacpDatasource(addDto);
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
          const editDto: EditAacpDatasourceDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            drive: modalForm.drive,
            url: modalForm.url,
            username: modalForm.username,
            password: modalForm.password,
            defaultDb: modalForm.defaultDb,
            queryMaxRows: modalForm.queryMaxRows,
            executeBatch: modalForm.executeBatch,
          };
          await AacpDatasourceApi.editAacpDatasource(editDto);
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
