import { ref, type Ref } from "vue";
import type { GetOrgListVo, GetOrgTreeVo } from "@/views/core/api/OrgApi";

/**
 * 输入组织机构选择器参数
 * 其他参数全部透传给内部的ModalOrgTree组件 具体参考ModalOrgTree组件的属性说明 @see ModalOrgTree.vue
 *
 * 双向绑定v-model参数
 * v-model:checked-org-ids 已选组织机构ID数组
 * v-model:checked-org-names 已选组织机构名称
 */
export interface InputOrgTreeProps {
  //输入框占位符
  placeholder?: string;

  //是否只读
  readonly?: boolean;

  //排除节点方法 如果返回false则排除该节点
  excludeNodeMethod?: (node: GetOrgTreeVo) => boolean;

  //禁用节点方法 如果返回false则禁用该节点
  checkEnableMethod?: (node: GetOrgTreeVo) => boolean;

  //是否禁用
  disabled?: boolean;
}

/**
 * 输入组织机构选择器事件发射器
 * 全量透传ModalOrgTree组件的事件发射器 具体参考 @see ModalOrgTreeService
 */
export interface InputOrgTreeEmits {
  (e: "on-submit-entity", checkedOrgEntities: GetOrgListVo[]): void;
}

export default {
  /**
   * 输入组织机构选择器打包
   * @param emit 事件发射器
   */
  useInputOrgTree(emit: InputOrgTreeEmits, bindCheckedOrgNames: Ref<string>) {
    //模态框显隐控制
    const modalVisible = ref(false);

    //草稿已选组织机构名称
    const draftCheckOrgNames = ref<string>("");

    /**
     * 模态框提交事件处理
     * @param ids 已选组织机构ID数组
     */
    const onSubmitEntity = (entities: GetOrgListVo[]): void => {
      draftCheckOrgNames.value = "";

      if (entities || entities.length > 0) {
        //拼接已选组织机构名称
        const orgNames = entities.map((v) => v.name).join("、");
        draftCheckOrgNames.value = orgNames;
      }

      //发射事件到外部
      emit("on-submit-entity", entities);

      //把现在已选的draft同步给外部双向绑定
      bindCheckedOrgNames.value = draftCheckOrgNames.value;
    };

    return {
      modalVisible,
      onSubmitEntity,
    };
  },
};
