import type { GetOrgListVo, GetOrgTreeVo } from "@/views/core/api/OrgApi";
import OrgApi from "@/views/core/api/OrgApi";
import { ElMessage } from "element-plus";
import { computed, ref, watch, type Ref } from "vue";

/**
 * 模态组织机构选择器参数
 * 其他未声明props全量透传给底层的OrgTree组件 具体参考OrgTree组件的属性 @see OrgTree.vue
 *
 * 双向绑定v-model参数
 * v-model 模态框显隐控制
 * v-model:checked-org-ids 当前已勾选组织机构ID数组 不管单选多选都是数组
 */
export interface ModalOrgTreeProps {
  //模态框标题
  title?: string;

  //模态框宽度
  width?: string | number;

  //模式: 单选、多选
  mode?: "single" | "multiple";

  //是否只读
  readonly?: boolean;

  //限制选择组织机构数量
  max?: number | null;

  //组织树裁剪根ID 将会以该ID为根节点进行裁剪 只显示该组织及下级组织
  cropOrgId?: string | null;

  //是否级联选择
  checkCascade?: boolean;

  //排除节点方法 如果返回false则排除该节点
  excludeNodeMethod?: (node: GetOrgTreeVo) => boolean;

  //禁用节点方法 如果返回false则禁用该节点
  checkEnableMethod?: (node: GetOrgTreeVo) => boolean;
}

/**
 * 模态组织机构选择器事件发射器
 */
export interface ModalOrgTreeEmits {
  (e: "on-submit", checkedOrgIds: string[]): void;
  (e: "on-submit-entity", checkedOrgEntities: GetOrgListVo[]): void;
  (e: "on-close"): void;
}

export default {
  /**
   * 模态组织机构选择器打包
   * @param props 模态组织机构选择器参数
   * @param emit 模态组织机构选择器事件发射器
   * @param bindModalVisible 模态框显隐控制
   * @param bindCheckedOrgIds 已勾选组织机构ID数组
   */
  useModalOrgTree(
    props: ModalOrgTreeProps,
    emit: ModalOrgTreeEmits,
    bindModalVisible: Ref<boolean>,
    bindCheckedOrgIds: Ref<string[]>
  ) {
    //草稿已勾选组织机构ID数组
    const draftCheckedOrgIds = ref<string[]>([]);

    //按钮加载状态
    const btnLoading = ref(false);

    /**
     * 是否超过最大选择数量
     */
    const isOverMax = computed(() => {
      if (!props.max) {
        return false;
      }

      return draftCheckedOrgIds.value.length > props.max;
    });

    const onModalSubmit = async (): Promise<void> => {
      btnLoading.value = true;

      try {
        //提交时根据Draft查询出组织机构完整Vo 外部要使用
        const data = await OrgApi.getOrgList({
          orgIds: draftCheckedOrgIds.value,
          pageNum: 1,
          pageSize: draftCheckedOrgIds.value.length + 100,
        });

        //根据后端返回的结果获取组织机构完整Vo和组织机构IDS(防止Draft中选了后端不存在的组织机构ID)
        const orgVos = data;
        const orgIds = orgVos.map((vo) => vo.id);

        //提交Draft到外部
        bindCheckedOrgIds.value = [...orgIds];

        emit("on-submit", orgIds);
        emit("on-submit-entity", orgVos);
        bindModalVisible.value = false;
      } catch (error: any) {
        ElMessage.error("获取组织机构列表失败，请稍后重试。");
        return;
      } finally {
        btnLoading.value = false;
      }
    };

    /**
     * 模态框打开
     */
    const onModalOpen = (): void => {
      //把外部的bind同步给draft
      draftCheckedOrgIds.value = [...bindCheckedOrgIds.value];
    };

    /**
     * 模态框关闭
     */
    const onModalClose = (): void => {
      //清空内部draft
      draftCheckedOrgIds.value = [];
      emit("on-close");
      bindModalVisible.value = false;
    };

    //监听模态框显隐控制
    watch(bindModalVisible, (visible) => (visible ? onModalOpen() : onModalClose()));

    return {
      draftCheckedOrgIds,
      isOverMax,
      btnLoading,
      onModalSubmit,
      onModalOpen,
      onModalClose,
    };
  },
};
