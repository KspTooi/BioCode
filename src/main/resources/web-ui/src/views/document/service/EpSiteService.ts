import { onMounted, reactive, ref, type Ref } from "vue";
import type {
  GetEpSiteDetailsVo,
  GetEpSiteListDto,
  GetEpSiteListVo,
  AddEpSiteDto,
  EditEpSiteDto,
} from "@/views/document/api/EpSiteApi.ts";
import EpSiteApi from "@/views/document/api/EpSiteApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

export default {
  /**
   * 站点列表打包
   */
  useEpSiteList() {
    const listForm = ref<GetEpSiteListDto>({
      pageNum: 1,
      pageSize: 20,
      id: null,
      name: null,
      address: null,
      username: null,
      remark: null,
      seq: null,
      createTime: null,
    });

    const listData = ref<GetEpSiteListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    // 密码显示状态映射
    const passwordVisibleMap = ref<Record<string, boolean>>({});

    /**
     * 切换密码显示/隐藏
     */
    const togglePasswordVisibility = (id: string): void => {
      passwordVisibleMap.value[id] = !passwordVisibleMap.value[id];
    };

    /**
     * 加载站点列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await EpSiteApi.getEpSiteList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("ep-site-manager", listForm.value);
      }

      if (Result.isError(result)) {
        ElMessage.error(result.message);
      }

      listLoading.value = false;
    };

    /**
     * 重置查询条件
     */
    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.id = null;
      listForm.value.name = null;
      listForm.value.address = null;
      listForm.value.username = null;
      listForm.value.remark = null;
      listForm.value.seq = null;
      listForm.value.createTime = null;
      QueryPersistService.clearQuery("ep-site-manager");
      loadList();
    };

    /**
     * 删除站点
     */
    const removeList = async (id: string): Promise<void> => {
      try {
        await ElMessageBox.confirm("确定删除该站点吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await EpSiteApi.removeEpSite({ id });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    /**
     * 批量删除站点
     */
    const removeListBatch = async (selectedItems: GetEpSiteListVo[]): Promise<void> => {
      if (selectedItems.length === 0) {
        ElMessage.warning("请选择要删除的站点");
        return;
      }

      try {
        await ElMessageBox.confirm(`确定删除选中的${selectedItems.length}个站点吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        const ids = selectedItems.map((item) => item.id);
        await EpSiteApi.removeEpSite({ ids });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    //初始化
    onMounted(async () => {
      QueryPersistService.loadQuery("ep-site-manager", listForm.value);
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
      removeListBatch,
      passwordVisibleMap,
      togglePasswordVisibility,
    };
  },

  /**
   * 复制到剪贴板（支持非HTTPS）
   * @param text 要复制的文本
   * @param label 标签名称（用于提示）
   */
  copyToClipboard(text: string, label: string) {
    if (!text) {
      return;
    }

    const copy = async (): Promise<void> => {
      try {
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(text);
          ElMessage.success(`${label}已复制到剪贴板`);
          return;
        }

        const textArea = document.createElement("textarea");
        textArea.value = text;
        textArea.style.position = "fixed";
        textArea.style.left = "-999999px";
        textArea.style.top = "-999999px";
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
          const successful = document.execCommand("copy");
          if (successful) {
            ElMessage.success(`${label}已复制到剪贴板`);
          }
          if (!successful) {
            ElMessage.error("复制失败，请手动复制");
          }
        } catch {
          ElMessage.error("复制失败，请手动复制");
        } finally {
          document.body.removeChild(textArea);
        }
      } catch {
        ElMessage.error("复制失败，请手动复制");
      }
    };

    copy();
  },

  /**
   * 站点模态框打包
   * @param modalFormRef 模态框表单引用
   * @param loadList 列表加载函数
   */
  useEpSiteModal(modalFormRef: Ref<FormInstance>, loadList: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<"add" | "edit">("add");
    const modalForm = reactive<GetEpSiteDetailsVo>({
      id: "",
      name: "",
      address: null,
      username: null,
      password: null,
      remark: null,
      seq: 0,
    });

    const modalRules = {
      name: [
        { required: true, message: "请输入站点名称", trigger: "blur" },
        { max: 32, message: "站点名称长度不能超过32个字符", trigger: "blur" },
      ],
      address: [{ max: 255, message: "地址长度不能超过255个字符", trigger: "blur" }],
      username: [{ max: 500, message: "账户长度不能超过500个字符", trigger: "blur" }],
      password: [{ max: 500, message: "密码长度不能超过500个字符", trigger: "blur" }],
      remark: [{ max: 1000, message: "备注长度不能超过1000个字符", trigger: "blur" }],
      seq: [{ required: true, message: "请输入排序", trigger: "blur" }],
    };

    /**
     * 打开模态框
     * @param mode 模式
     * @param row 当前行
     */
    const openModal = async (mode: "add" | "edit", row: GetEpSiteListVo | null): Promise<void> => {
      modalMode.value = mode;
      resetModal();

      //如果是编辑模式则需要加载详情数据
      if (mode === "edit" && row) {
        try {
          const ret = await EpSiteApi.getEpSiteDetails({ id: row.id });
          modalForm.id = ret.id;
          modalForm.name = ret.name;
          modalForm.address = ret.address;
          modalForm.username = ret.username;
          modalForm.password = ret.password;
          modalForm.remark = ret.remark;
          modalForm.seq = ret.seq;
        } catch (error: any) {
          ElMessage.error(error.message || "获取站点详情失败");
          return;
        }
      }

      modalVisible.value = true;
    };

    /**
     * 重置模态框表单
     */
    const resetModal = (): void => {
      modalForm.id = "";
      modalForm.name = "";
      modalForm.address = null;
      modalForm.username = null;
      modalForm.password = null;
      modalForm.remark = null;
      modalForm.seq = 0;

      if (modalFormRef.value) {
        modalFormRef.value.resetFields();
      }
    };

    /**
     * 提交模态框表单
     */
    const submitModal = async (): Promise<void> => {
      //先校验表单
      try {
        await modalFormRef?.value?.validate();
      } catch {
        return;
      }

      modalLoading.value = true;

      //提交表单
      try {
        if (modalMode.value === "add") {
          const addDto: AddEpSiteDto = {
            name: modalForm.name,
            address: modalForm.address,
            username: modalForm.username,
            password: modalForm.password,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          await EpSiteApi.addEpSite(addDto);
          ElMessage.success("操作成功");
          resetModal();
        }

        if (modalMode.value === "edit") {
          const editDto: EditEpSiteDto = {
            id: modalForm.id,
            name: modalForm.name,
            address: modalForm.address,
            username: modalForm.username,
            password: modalForm.password,
            remark: modalForm.remark,
            seq: modalForm.seq,
          };
          await EpSiteApi.editEpSite(editDto);
          ElMessage.success("操作成功");
        }
      } catch (error: any) {
        ElMessage.error(error.message);
        return;
      } finally {
        modalLoading.value = false;
      }

      await loadList();
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
