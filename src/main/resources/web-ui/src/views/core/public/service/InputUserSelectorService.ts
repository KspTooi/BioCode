import type { GetUserListVo } from "@/views/core/api/UserApi";
import { ref, type Ref } from "vue";

/**
 * 输入用户选择器参数
 * 其他参数全部透传给内部的ModalUserSelector组件 具体参考ModalUserSelector组件的属性说明
 *
 * 双向绑定v-model参数
 * v-model:checked-user-ids 当前已选用户IDS 不管单选多选都是数组
 * v-model:checked-user-names 当前已选用户姓名(回显用，模态框提交后会同步给外面)
 */
export interface InputUserSelectorProps {
  //输入框占位符
  placeholder?: string;

  //是否只读
  readonly?: boolean;
}

/**
 * 输入用户选择器事件发射器
 * 全量透传ModalUserSelector组件的事件发射器 具体参考 @see ModalUserSelectorService
 */
export interface InputUserSelectorEmits {
  (e: "on-submit-entity", data: GetUserListVo[]): void;
}

export default {
  /**
   * 输入用户选择器打包
   * @param emit 输入用户选择器事件发射器
   * @param bindCheckedUserNames bind已选用户姓名
   */
  useInputUserSelector(emit: InputUserSelectorEmits, bindCheckedUserNames: Ref<string>) {
    //模态框显隐控制
    const modalVisible = ref(false);

    //草稿已选用户姓名
    const draftCheckedUserNames = ref<string>("");

    /**
     * 模态框提交用户VO事件处理
     * @param data 用户VO
     */
    const onSubmitEntity = (data: GetUserListVo[]): void => {
      draftCheckedUserNames.value = "";

      if (data || data.length > 0) {
        //拼接已选用户姓名
        const userNames = data.map((v) => v.nickname).join("、");
        draftCheckedUserNames.value = userNames;
      }

      //发射事件到外部
      emit("on-submit-entity", data);

      //把现在已选的draft同步给外部双向绑定
      bindCheckedUserNames.value = draftCheckedUserNames.value;
    };

    return {
      onSubmitEntity,
      modalVisible,
    };
  },
};
