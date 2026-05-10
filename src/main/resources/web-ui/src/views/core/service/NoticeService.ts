import { computed, onMounted, reactive, ref, watch, type Ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type {
  GetNoticeListDto,
  GetNoticeListVo,
  GetNoticeDetailsVo,
  AddNoticeDto,
  EditNoticeDto,
} from "@/views/core/api/NoticeApi.ts";
import NoticeApi from "@/views/core/api/NoticeApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage, ElMessageBox } from "element-plus";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

/**
 * 模态框模式类型
 */
type ModalMode = "add" | "edit" | "view";

export default {
  /**
   * 消息表列表管理
   */
  useNoticeList() {
    const listForm = ref<GetNoticeListDto>({
      pageNum: 1,
      pageSize: 20,
      title: "",
      kind: undefined,
      content: "",
      priority: undefined,
      category: "",
      senderName: "",
    });

    const listData = ref<GetNoticeListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    /**
     * 加载列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await NoticeApi.getNoticeList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        QueryPersistService.persistQuery("notice-list", listForm.value);
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
      listForm.value.title = "";
      listForm.value.kind = undefined;
      listForm.value.content = "";
      listForm.value.priority = undefined;
      listForm.value.category = "";
      listForm.value.senderName = "";
      QueryPersistService.clearQuery("notice-list");
      loadList();
    };

    /**
     * 删除记录
     */
    const removeList = async (row: GetNoticeListVo): Promise<void> => {
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
        await NoticeApi.removeNotice({ id: String(row.id) });
        ElMessage.success("删除成功");
        await loadList();
      } catch (error: any) {
        ElMessage.error(error.message);
      }
    };

    onMounted(async () => {
      QueryPersistService.loadQuery("notice-list", listForm.value);
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
  useNoticeModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const modalMode = ref<ModalMode>("add");
    const modalFormDisabled = computed(() => modalMode.value === "view");
    const modalForm = reactive<GetNoticeDetailsVo>({
      id: "", // 主键ID
      title: "", // 标题
      kind: 0, // 种类: 0公告, 1业务提醒, 2私信
      content: "", // 通知内容
      priority: 0, // 优先级: 0:低 1:中 2:高
      category: "", // 业务类型/分类
      targetKind: 0, // 接收对象类型 0:全员 1:指定部门 2:指定用户
      senderId: 0, // 发送人ID (NULL为系统)
      senderName: "", // 发送人姓名
      forward: "", // 跳转URL/路由地址
      params: "", // 动态参数 (JSON格式)
      createTime: "", // 创建时间
      _targetIds: [], // 接收对象ID列表
    });

    //当前已勾选的用户/部门IDS
    const modalTargetIds = ref<string[]>([]);

    //模态框部门选择器可见性
    const modalTargetDeptVisible = ref(false);

    //模态框用户选择器可见性
    const modalTargetUserVisible = ref(false);

    /**
     * 表单验证规则
     */
    const modalRules: FormRules = {
      title: [
        { required: true, message: "标题不能为空", trigger: "blur" },
        { max: 32, message: "标题长度不能超过32个字符", trigger: "blur" },
      ],
      kind: [
        { required: true, message: "种类不能为空", trigger: "blur" },
        { type: "number", min: 0, max: 2, message: "种类只能在0-2之间", trigger: "blur" },
      ],
      priority: [
        { required: true, message: "优先级不能为空", trigger: "blur" },
        { type: "number", min: 0, max: 2, message: "优先级只能在0-2之间", trigger: "blur" },
      ],
      category: [{ max: 32, message: "业务类型长度不能超过32个字符", trigger: "blur" }],
      targetKind: [
        { required: true, message: "接收对象类型不能为空", trigger: "blur" },
        { type: "number", min: 0, max: 2, message: "接收对象类型只能在0-2之间", trigger: "blur" },
      ],
      _targetIds: [
        {
          validator: (rule: any, value: any, callback: any) => {
            if (modalForm.targetKind === 0) {
              callback();
              return;
            }
            if (!value || value.length === 0) {
              callback(new Error(`${modalForm.targetKind === 1 ? "接收部门" : "接收用户"}不能为空`));
              return;
            }
            callback();
          },
          trigger: "blur",
        },
      ],
    };

    /**
     * 打开模态框
     * @param mode 模式: 'add' | 'edit'
     * @param row 编辑时传入的行数据
     */
    const openModal = async (mode: ModalMode, row: GetNoticeListVo | null): Promise<void> => {
      modalMode.value = mode;

      if (mode === "add") {
        modalForm.id = undefined;
        modalForm.title = "";
        modalForm.kind = 0;
        modalForm.content = "";
        modalForm.priority = 0;
        modalForm.category = "";
        modalForm.targetKind = 0;
        modalForm._targetIds = [];
        modalVisible.value = true;
        return;
      }

      if (mode === "edit" || mode === "view") {
        if (!row) {
          ElMessage.error("未选择要操作的数据");
          return;
        }

        try {
          const details = await NoticeApi.getNoticeDetails({ id: String(row.id) });
          modalForm.id = details.id;
          modalForm.title = details.title;
          modalForm.kind = details.kind;
          modalForm.content = details.content || "";
          modalForm.priority = details.priority;
          modalForm.category = details.category || "";
          modalForm.targetKind = details.targetKind;
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
      modalForm.id = undefined;
      modalForm.title = "";
      modalForm.kind = 0;
      modalForm.content = "";
      modalForm.priority = 0;
      modalForm.category = "";
      modalForm.targetKind = 0;
      modalForm._targetIds = [];
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
          const addDto: AddNoticeDto = {
            title: modalForm.title,
            kind: modalForm.kind,
            content: modalForm.content || undefined,
            priority: modalForm.priority,
            category: modalForm.category || undefined,
            targetKind: modalForm.targetKind,
            targetIds: modalForm._targetIds,
          };
          await NoticeApi.addNotice(addDto);
          ElMessage.success("新增成功");

          resetModal();
          reloadCallback();
          modalVisible.value = false;
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
          const editDto: EditNoticeDto = {
            id: modalForm.id,
            title: modalForm.title,
            kind: modalForm.kind,
            content: modalForm.content || undefined,
            priority: modalForm.priority,
            category: modalForm.category || undefined,
          };
          await NoticeApi.editNotice(editDto);
          ElMessage.success("修改成功");

          //resetModal();
          reloadCallback();
          modalVisible.value = false;
        } catch (error: any) {
          ElMessage.error(error.message);
        }
        modalLoading.value = false;
      }
    };

    /**
     * 选择接收对象模态框打开
     */
    const onModalTargetOpen = (): void => {
      if (modalForm.targetKind === 1) {
        modalTargetDeptVisible.value = true;
      }
      if (modalForm.targetKind === 2) {
        modalTargetUserVisible.value = true;
      }
      //已选数据填入选择器
      modalTargetIds.value = modalForm._targetIds;
    };

    /**
     * 选择接收对象模态框提交
     */
    const onModalTargetSubmit = (checkedIds: string[]): void => {
      //当前选择的是部门
      if (modalForm.targetKind === 1) {
        modalForm._targetIds = checkedIds;
      }

      //当前选择的是用户
      if (modalForm.targetKind === 2) {
        modalForm._targetIds = checkedIds;
      }
    };

    //处理接收对象类型变化时清空接收对象ID列表
    watch(
      () => modalForm.targetKind,
      (): void => {
        modalForm._targetIds = [];
        modalTargetIds.value = [];
      }
    );

    return {
      modalVisible,
      modalLoading,
      modalMode,
      modalFormDisabled,
      modalForm,
      modalTargetIds,
      modalRules,
      modalTargetDeptVisible,
      modalTargetUserVisible,
      openModal,
      resetModal,
      submitModal,
      onModalTargetOpen,
      onModalTargetSubmit,
    };
  },
};
