import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetAacpDatasourceListDto,
  GetAacpDatasourceListVo,
  GetAacpDatasourceDetailsVo,
  AddAacpDatasourceDto,
  EditAacpDatasourceDto,
} from "@/views/aacpDatasource/api/AacpDatasourceApi.ts";
import AacpDatasourceApi from "@/views/aacpDatasource/api/AacpDatasourceApi.ts";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit";

export default {
  /**
   * AACP数据源列表管理
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

    /**
     * 加载列表
     */
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
     * 删除记录
     */
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
  useAacpDatasourceModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpDatasourceDetailsVo>({
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

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      name: [
        { required: true, message: "请输入数据源名称", trigger: "blur" },
        { max: 40, message: "数据源名称长度不能超过40个字符", trigger: "blur" },
      ],
      code: [
        { required: true, message: "请输入数据源编码", trigger: "blur" },
        { max: 32, message: "数据源编码长度不能超过32个字符", trigger: "blur" },
      ],
      kind: [{ required: true, message: "请输入数据源类型 0:MYSQL", trigger: "blur" }],
      drive: [
        { required: true, message: "请输入JDBC驱动", trigger: "blur" },
        { max: 200, message: "JDBC驱动长度不能超过200个字符", trigger: "blur" },
      ],
      url: [{ required: true, message: "请输入连接字符串", trigger: "blur" }],
      username: [{ max: 200, message: "连接用户名长度不能超过200个字符", trigger: "blur" }],
      password: [{ max: 2000, message: "连接密码长度不能超过2000个字符", trigger: "blur" }],
      defaultDb: [
        { required: true, message: "请输入默认数据库", trigger: "blur" },
        { max: 200, message: "默认数据库长度不能超过200个字符", trigger: "blur" },
      ],
      queryMaxRows: [{ required: true, message: "请输入最大查询行数", trigger: "blur" }],
      executeBatch: [{ required: true, message: "请输入是否支持批处理 0:不支持 1:支持", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetAacpDatasourceListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
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
        modalForm.executeBatch = 0;
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
          modalForm.username = details.username;
          modalForm.password = details.password;
          modalForm.defaultDb = details.defaultDb;
          modalForm.queryMaxRows = details.queryMaxRows;
          modalForm.executeBatch = details.executeBatch;
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
      modalForm.defaultDb = "";
      modalForm.queryMaxRows = 0;
      modalForm.executeBatch = 0;
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
