<!--
  * 组织机构输入选择器
  * 这是ModalOrgTree的轻量包装，提供带输入框的交互形式。
  * 所有属性都会透传给ModalOrgTree组件 具体参考ModalOrgTree组件的属性
-->
<template>
  <div style="display: inline-flex; width: 100%">
    <el-input :model-value="bindCheckedOrgNames" :placeholder="props.placeholder" readonly style="flex: 1">
      <template #append>
        <el-button type="primary" :disabled="disabled" @click="modalVisible = true">{{
          props.readonly ? "查看" : "选择"
        }}</el-button>
      </template>
    </el-input>
    <ModalOrgTree
      v-model="modalVisible"
      v-model:checked-org-ids="bindCheckedOrgIds"
      :readonly="readonly"
      :exclude-node-method="excludeNodeMethod"
      :check-enable-method="checkEnableMethod"
      v-bind="$attrs"
      @on-submit-entity="(entities) => onSubmitEntity(entities)"
    />
  </div>
</template>

<script setup lang="ts">
import ModalOrgTree from "@/views/core/public/ModalOrgTree.vue";
import InputOrgTreeService, {
  type InputOrgTreeEmits,
  type InputOrgTreeProps,
} from "@/views/core/public/service/InputOrgTreeService";

//组件参数
const props = withDefaults(defineProps<InputOrgTreeProps>(), {
  placeholder: "请选择组织机构",
  readonly: false,
  excludeNodeMethod: undefined,
  checkEnableMethod: undefined,
  disabled: false,
});

//事件发射器
const emit = defineEmits<InputOrgTreeEmits>();

//已勾选组织机构ID数组 外部用v-model绑定
const bindCheckedOrgIds = defineModel<string[]>({ required: true });

//已选组织机构名称 外部用v-model:checked-org-names绑定
const bindCheckedOrgNames = defineModel<string>("checkedOrgNames", { default: "" });

//输入组织机构选择器打包
const { modalVisible, onSubmitEntity } = InputOrgTreeService.useInputOrgTree(emit, bindCheckedOrgNames);
</script>
