import { computed, ref, watch, type DefineComponent, type EmitFn, type Ref } from "vue";
import type { GetUserListDto, GetUserListVo } from "@/views/core/api/UserApi";
import AdminUserApi from "@/views/core/api/UserApi";
import { Result } from "@/commons/model/Result";
import { ElMessage, ElTable, type DialogProps } from "element-plus";
import { useDebounceFn, useThrottleFn, watchDebounced } from "@vueuse/core";
import UserApi from "@/views/core/api/UserApi";
import ModalUserSelectorVue from "@/views/core/public/ModalUserSelector.vue";

/**
 * 模态用户选择器参数
 * 其他参数透传给el-dialog组件 具体参考el-dialog组件的属性 @see DialogProps
 *
 * 双向绑定v-model:参数
 * v-model 模态框显隐控制
 * v-model:current-org-id 当前选中组织ID
 * v-model:checked-user-ids 当前已勾选用户IDS 不管单选多选都是数组
 */
export interface ModalUserSelectorProps {
  //模态框标题
  title?: string;

  //模态框宽度
  width?: string | number;

  //模式: 单选、多选
  mode?: "single" | "multiple";

  //是否只读
  readonly?: boolean;

  //限制选择用户数量
  max?: number | null;

  //左侧组织树裁剪根ID 将会以该ID为根节点进行裁剪 只显示该组织及下级组织
  cropOrgId?: string | null;
}

/**
 * 模态用户选择器事件发射器
 */
export interface ModalUserSelectorEmits {
  (e: "on-submit", data: string[]): void;
  (e: "on-submit-entity", data: GetUserListVo[]): void;
  (e: "on-close"): void;
}

export default {
  /**
   * 用户选择模态框打包
   * @param emit 用户选择器事件发射器
   * @param modalVisible 模态框显隐控制
   * @param bindCheckedOrgId 当前选中组织ID
   * @param bindCheckedUids 当前已勾选的用户IDS
   */
  useUserSelect(
    props: ModalUserSelectorProps,
    emit: ModalUserSelectorEmits,
    modalVisible: Ref<boolean>,
    bindCheckedOrgId: Ref<string | null>,
    bindCheckedUids: Ref<string[]>
  ) {
    const listForm = ref<GetUserListDto>({
      pageNum: 1,
      pageSize: 20,
      username: "",
      phone: "",
      status: null,
      orgId: null,
    });

    const listData = ref<GetUserListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);
    const modalLoading = ref(false);

    //草稿当前选中组织ID
    const draftCheckedOrgId = ref<string | null>(null);

    //草稿已勾选用户IDS
    const draftCheckUids = ref<string[]>([]);

    /**
     * 是否超过最大选择数量
     */
    const isOverMax = computed(() => {
      if (!props.max) {
        return false;
      }

      return draftCheckUids.value.length > props.max;
    });

    /**
     * 加载用户列表
     */
    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      listForm.value.orgId = draftCheckedOrgId.value ?? null;

      const result = await AdminUserApi.getUserList(listForm.value);

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
     * 重置用户列表
     */
    const resetList = (load: boolean = true): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.username = "";
      listForm.value.nickname = "";
      listForm.value.phone = "";
      listForm.value.status = null;
      if (load) {
        loadList();
      }
    };

    /**
     * 模态框提交
     */
    const onModalSubmit = async (): Promise<void> => {
      modalLoading.value = true;

      //提交时根据Draft查询出用户的完整Vo 外部要使用
      try {
        const result = await UserApi.getUserList({
          pageNum: 1,
          pageSize: draftCheckUids.value.length + 100,
          userIds: draftCheckUids.value,
        });

        //根据后端返回的结果获取用户完整Vo和用户IDS(防止Draft中选了后端不存在的用户ID)
        const userVos = result.data;
        const userIds = userVos.map((vo) => vo.id);

        //提交Draft到外部
        bindCheckedUids.value = [...userIds];

        emit("on-submit", userIds);
        emit("on-submit-entity", userVos);
        modalVisible.value = false;
      } catch (error: any) {
        ElMessage.error("获取用户信息失败，请稍后重试。");
        return;
      } finally {
        modalLoading.value = false;
      }
    };

    /**
     * 模态框打开
     */
    const onModalOpen = (): void => {
      //把外部的bind同步给draft
      draftCheckUids.value = [...bindCheckedUids.value];
      draftCheckedOrgId.value = bindCheckedOrgId.value;

      //如果有剪裁根ID 则选中剪裁根ID
      if (props.cropOrgId) {
        draftCheckedOrgId.value = props.cropOrgId;
      }

      //重置用户列表
      resetList();
    };

    /**
     * 模态框关闭
     */
    const onModalClose = (): void => {
      //清空内部draft
      draftCheckUids.value = [];
      emit("on-close");
    };

    //监听模态框显隐控制
    watch(modalVisible, (visible) => (visible ? onModalOpen() : onModalClose()));

    return {
      listForm,
      listData,
      listTotal,
      listLoading,
      draftCheckUids,
      isOverMax,
      draftCheckedOrgId,
      loadList,
      resetList,
      onModalSubmit,
    };
  },
};
