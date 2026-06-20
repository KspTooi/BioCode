import { computed, onMounted, reactive, ref, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { GetScmListDto, GetScmListVo, GetScmDetailsVo, AddScmDto, EditScmDto, CopyScmDto } from "@/views/assembly/api/ScmApi";
import ScmApi from "@/views/assembly/api/ScmApi";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit" | "copy";

export default {
  /**
   * SCM列表管理
   */
  useScmList() {
    const listForm = ref<GetScmListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      projectName: "",
    });

    const listData = ref<GetScmListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await ScmApi.getScmList(listForm.value);

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
      listForm.value.projectName = "";
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetScmListVo): Promise<void> => {
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
        await ScmApi.removeScm({ id: row.id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 测试SCM连接
     */
    const testScmConnection = async (row: GetScmListVo): Promise<void> => {
      listLoading.value = true;

      try {
        const msg = await ScmApi.testScmConnection({ id: row.id });
        ElMessageBox.alert(msg || "连接成功", "测试结果", { type: "success", confirmButtonText: "确定" });
      } catch (error: any) {
        ElMessageBox.alert(error.message, "测试结果", { type: "error", confirmButtonText: "确定" });
      }

      listLoading.value = false;
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
      testScmConnection,
    };
  },

  /**
   * 模态框管理（统一处理新增和编辑）
   */
  useScmModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalForm = reactive<GetScmDetailsVo>({
      id: "",
      name: "",
      projectName: "",
      scmUrl: "",
      scmAuthKind: 0,
      scmUsername: "",
      scmPassword: "",
      scmPk: "",
      scmBranch: "main",
      remark: "",
    });

    /**
     * 表单验证规则（根据 scmAuthKind 动态生成）
     */
    const modalRules = computed<FormRules>(() => {
      const needUsernamePassword = modalForm.scmAuthKind === 1 || modalForm.scmAuthKind === 3;
      const needPk = modalForm.scmAuthKind === 2;
      return {
        name: [{ required: true, message: "请输入SCM名称", trigger: "blur", max: 32 }],
        projectName: [{ required: false, trigger: "blur", max: 80 }],
        scmUrl: [{ required: true, message: "请输入SCM仓库地址", trigger: "blur", max: 1000 }],
        scmAuthKind: [{ required: true, message: "请选择SCM认证方式", trigger: "change", type: "number" }],
        scmUsername: [
          ...(needUsernamePassword ? [{ required: true, message: "请输入SCM用户名", trigger: "blur" as const }] : []),
          { max: 10000, message: "SCM用户名长度不能超过10000个字符", trigger: "blur" },
        ],
        scmPassword: [
          ...(needUsernamePassword ? [{ required: true, message: "请输入SCM密码", trigger: "blur" as const }] : []),
          { max: 10000, message: "SCM密码长度不能超过10000个字符", trigger: "blur" },
        ],
        scmPk: [
          ...(needPk ? [{ required: true, message: "请输入SSH KEY", trigger: "blur" as const }] : []),
          { max: 10000, message: "SSH KEY长度不能超过10000个字符", trigger: "blur" },
        ],
        scmBranch: [{ required: true, message: "请输入SCM分支", trigger: "blur", max: 80 }],
        remark: [{ required: false, trigger: "blur", max: 500 }],
      };
    });

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetScmListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = "";
        modalForm.name = "";
        modalForm.projectName = "";
        modalForm.scmUrl = "";
        modalForm.scmAuthKind = 0;
        modalForm.scmUsername = "";
        modalForm.scmPassword = "";
        modalForm.scmPk = "";
        modalForm.scmBranch = "main";
        modalForm.remark = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "copy") {
        if (!row) {
          ElMessage.error("未选择要复制的数据");
          return;
        }
        modalForm.id = row.id;
        modalForm.name = row.name;
        modalForm.projectName = "";
        modalForm.scmUrl = "";
        modalForm.scmAuthKind = 0;
        modalForm.scmUsername = "";
        modalForm.scmPassword = "";
        modalForm.scmPk = "";
        modalForm.scmBranch = "main";
        modalForm.remark = "";
        modalVisible.value = true;
        return;
      }

      if (mode === "edit") {
        if (!row) {
          ElMessage.error("未选择要编辑的数据");
          return;
        }

        try {
          const details = await ScmApi.getScmDetails({ id: row.id });
          modalForm.id = details.id;
          modalForm.name = details.name;
          modalForm.projectName = details.projectName;
          modalForm.scmUrl = details.scmUrl;
          modalForm.scmAuthKind = details.scmAuthKind;
          modalForm.scmUsername = details.scmUsername;
          modalForm.scmPassword = details.scmPassword;
          modalForm.scmPk = details.scmPk;
          modalForm.scmBranch = details.scmBranch;
          modalForm.remark = details.remark;
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
      modalForm.projectName = "";
      modalForm.scmUrl = "";
      modalForm.scmAuthKind = 0;
      modalForm.scmUsername = "";
      modalForm.scmPassword = "";
      modalForm.scmPk = "";
      modalForm.scmBranch = "main";
      modalForm.remark = "";
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
          const addDto: AddScmDto = {
            name: modalForm.name,
            projectName: modalForm.projectName,
            scmUrl: modalForm.scmUrl,
            scmAuthKind: modalForm.scmAuthKind,
            scmUsername: modalForm.scmUsername,
            scmPassword: modalForm.scmPassword,
            scmPk: modalForm.scmPk,
            scmBranch: modalForm.scmBranch,
            remark: modalForm.remark,
          };
          await ScmApi.addScm(addDto);
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

      if (modalMode.value === "copy") {
        if (!modalForm.name) {
          ElMessage.error("SCM名称不能为空");
          modalLoading.value = false;
          return;
        }
        try {
          const copyDto: CopyScmDto = {
            id: modalForm.id,
            name: modalForm.name,
          };
          await ScmApi.copyScm(copyDto);
          ElMessage.success("复制成功");
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
          const editDto: EditScmDto = {
            id: modalForm.id,
            name: modalForm.name,
            projectName: modalForm.projectName,
            scmUrl: modalForm.scmUrl,
            scmAuthKind: modalForm.scmAuthKind,
            scmUsername: modalForm.scmUsername,
            scmPassword: modalForm.scmPassword,
            scmPk: modalForm.scmPk,
            scmBranch: modalForm.scmBranch,
            remark: modalForm.remark,
          };
          await ScmApi.editScm(editDto);
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
