import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetDataSourceListDto,
  GetDataSourceListVo,
  GetDataSourceDetailsVo,
  AddDataSourceDto,
  EditDataSourceDto,
} from "@/views/assembly/api/DataSourceApi.ts";
import DataSourceApi from "@/views/assembly/api/DataSourceApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * 数据源表列表管理
   */
  useDataSourceList() {
    const listForm = ref<GetDataSourceListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      code: "",
    });

    const listData = ref<GetDataSourceListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await DataSourceApi.getDataSourceList(listForm.value);

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
      loadList();
    };

    /**
     * 测试连接
     */
    const testConnection = async (row: GetDataSourceListVo): Promise<void> => {
      try {
        const msg = await DataSourceApi.testDataSourceConnection({ id: row.id });
        ElMessageBox.alert(msg || "连接成功", "测试结果", { type: "success", confirmButtonText: "确定" });
      } catch (error: any) {
        ElMessageBox.alert(error.message, "测试结果", { type: "error", confirmButtonText: "确定" });
      }
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetDataSourceListVo): Promise<void> => {
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
        await DataSourceApi.removeDataSource({ id: row.id });
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
      testConnection,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useDataSourceModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetDataSourceDetailsVo & { username?: string; password?: string }>({
      id: "",
      name: "",
      code: "",
      kind: 0,
      drive: "",
      url: "",
      username: "",
      password: "",
      dbSchema: "",
    });

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入数据源名称", trigger: "blur" },
        { max: 32, message: "长度不能超过32个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入数据源编码", trigger: "blur" },
        { max: 32, message: "长度不能超过32个字符", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请选择数据源类型", trigger: "blur" }],
      drive: [
        { required: true, message: "请输入JDBC驱动", trigger: "blur" },
        { max: 80, message: "长度不能超过80个字符", trigger: "blur" },
      ],
      url: [
        { required: true, message: "请输入连接字符串", trigger: "blur" },
        { max: 1000, message: "长度不能超过1000个字符", trigger: "blur" },
      ],
      username: [{ max: 320, message: "长度不能超过320个字符", trigger: "blur" }],
      password: [{ max: 1280, message: "长度不能超过1280个字符", trigger: "blur" }],
      dbSchema: [
        { required: true, message: "请输入默认模式", trigger: "blur" },
        { max: 80, message: "长度不能超过80个字符", trigger: "blur" },
      ],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetDataSourceListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.code = "";
        modalForm.kind = 0;
        modalForm.drive = "com.mysql.cj.jdbc.Driver";
        modalForm.url = "";
        modalForm.username = "";
        modalForm.password = "";
        modalForm.dbSchema = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await DataSourceApi.getDataSourceDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.code = details.code;
          modalForm.kind = details.kind;
          modalForm.drive = details.drive;
          modalForm.url = details.url;
          modalForm.username = "";
          modalForm.password = "";
          modalForm.dbSchema = details.dbSchema;
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
      modalForm.kind = 0;
      modalForm.drive = "";
      modalForm.url = "";
      modalForm.username = "";
      modalForm.password = "";
      modalForm.dbSchema = "";
    };

    const onCodeBlur = (): void => {
      if (modalMode.value !== "add") {
        return;
      }
      if (!modalForm.code) {
        return;
      }
      if (!modalForm.url) {
        modalForm.url =
          "jdbc:mysql://127.0.0.1:3306/" +
          modalForm.code +
          "?serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowMultiQueries=true&useUnicode=true&characterEncoding=utf-8";
      }
      if (!modalForm.dbSchema) {
        modalForm.dbSchema = modalForm.code;
      }
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
          const addDto: AddDataSourceDto = {
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            drive: modalForm.drive,
            url: modalForm.url,
            username: modalForm.username,
            password: modalForm.password,
            dbSchema: modalForm.dbSchema,
          };
          await DataSourceApi.addDataSource(addDto);
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
          const editDto: EditDataSourceDto = {
            id: modalForm.id,
            name: modalForm.name,
            code: modalForm.code,
            kind: modalForm.kind,
            drive: modalForm.drive,
            url: modalForm.url,
            username: modalForm.username,
            password: modalForm.password,
            dbSchema: modalForm.dbSchema,
          };
          await DataSourceApi.editDataSource(editDto);
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
      onCodeBlur,
    };
  },
};
