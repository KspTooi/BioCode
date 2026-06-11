import { onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import AacpDatasourceApi from "@/views/aacp/api/AacpDatasourceApi.ts";
import type {
  GetAacpDatasourceListDto,
  GetAacpDatasourceListVo,
  GetAacpDatasourceDetailsVo,
  AddAacpDatasourceDto,
  EditAacpDatasourceDto,
} from "@/views/aacp/api/AacpDatasourceApi.ts";

type ModalMode = "add" | "edit";

export default {
  /**
   * AACP数据源列表管理：加载、查询、删除
   */
  useAacpDatasourceList() {
    const listForm = reactive<GetAacpDatasourceListDto>({
      name: null,
      code: null,
      pageNum: 1,
      pageSize: 20,
    });

    const listData = ref<GetAacpDatasourceListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      try {
        const res = await AacpDatasourceApi.getAacpDatasourceList(listForm);
        listData.value = res.data;
        listTotal.value = res.total;
      } catch {
        ElMessage.error("加载数据源列表失败");
      } finally {
        listLoading.value = false;
      }
    };

    const resetList = (): void => {
      listForm.name = null;
      listForm.code = null;
      listForm.pageNum = 1;
      listForm.pageSize = 20;
      loadList();
    };

    const removeList = async (row: GetAacpDatasourceListVo): Promise<void> => {
      try {
        await ElMessageBox.confirm(`确定删除数据源 [${row.name}] 吗？`, "提示", {
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
        loadList();
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
   * AACP数据源模态框管理：新增/编辑表单校验与提交
   * @param modalFormRef 表单实例引用
   * @param reloadCallback 提交成功后刷新列表的回调
   */
  useAacpDatasourceModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetAacpDatasourceDetailsVo>({
      id: null,
      name: null,
      code: null,
      kind: null,
      drive: null,
      url: null,
      username: null,
      password: null,
      defaultDb: null,
      queryMaxRows: null,
      executeBatch: null,
    });

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
      executeBatch: [{ required: true, message: "请输入是否支持批处理", trigger: "blur" }],
    };

    const resetModal = (): void => {
      modalFormRef.value?.resetFields();
      modalForm.id = null;
      modalForm.name = null;
      modalForm.code = null;
      modalForm.kind = null;
      modalForm.drive = null;
      modalForm.url = null;
      modalForm.username = null;
      modalForm.password = null;
      modalForm.defaultDb = null;
      modalForm.queryMaxRows = null;
      modalForm.executeBatch = null;
    };

    const openModal = (mode: ModalMode, row: GetAacpDatasourceListVo | null): void => {
      resetModal();
      modalMode.value = mode;
      if (mode === "add") {
        modalVisible.value = true;
        return;
      }
      modalLoading.value = true;
      AacpDatasourceApi.getAacpDatasourceDetails({ id: row!.id })
        .then((details) => {
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
        })
        .catch((error: any) => {
          ElMessage.error(error.message);
        })
        .finally(() => {
          modalLoading.value = false;
        });
    };

    const submitModal = async (): Promise<void> => {
      try {
        await modalFormRef.value!.validate();
      } catch {
        return;
      }

      modalLoading.value = true;
      if (modalMode.value === "add") {
        const addDto: AddAacpDatasourceDto = {
          name: modalForm.name!,
          code: modalForm.code!,
          kind: modalForm.kind!,
          drive: modalForm.drive!,
          url: modalForm.url!,
          username: modalForm.username!,
          password: modalForm.password!,
          defaultDb: modalForm.defaultDb!,
          queryMaxRows: modalForm.queryMaxRows!,
          executeBatch: modalForm.executeBatch!,
        };
        try {
          await AacpDatasourceApi.addAacpDatasource(addDto);
          ElMessage.success("新增成功");
          modalVisible.value = false;
          reloadCallback();
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
        return;
      }
      const editDto: EditAacpDatasourceDto = {
        id: modalForm.id!,
        name: modalForm.name!,
        code: modalForm.code!,
        kind: modalForm.kind!,
        drive: modalForm.drive!,
        url: modalForm.url!,
        username: modalForm.username!,
        password: modalForm.password!,
        defaultDb: modalForm.defaultDb!,
        queryMaxRows: modalForm.queryMaxRows!,
        executeBatch: modalForm.executeBatch!,
      };
      try {
        await AacpDatasourceApi.editAacpDatasource(editDto);
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
