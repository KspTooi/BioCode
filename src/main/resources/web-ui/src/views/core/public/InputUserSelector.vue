<!--
  * 表单用户选择器
  * 
-->
<template>
  <div style="display: inline-flex; width: 100%">
    <slot :open="() => (modalVisible = true)" :display-text="bindCheckedUserNames">
      <el-input :model-value="bindCheckedUserNames" :placeholder="props.placeholder" readonly style="flex: 1">
        <template #append>
          <slot name="button" :open="() => (modalVisible = true)">
            <el-button type="primary" @click="modalVisible = true">{{ props.readonly ? "查看" : "选择" }}</el-button>
          </slot>
        </template>
      </el-input>
    </slot>
    <ModalUserSelector
      v-bind="$attrs"
      v-model="modalVisible"
      v-model:checked-user-ids="bindCheckedUserIds"
      :readonly="props.readonly"
      @on-submit-entity="(data) => onSubmitEntity(data)"
    />
  </div>
</template>

<script setup lang="ts">
import ModalUserSelector from "@/views/core/public/ModalUserSelector.vue";
import InputUserSelectorService, {
  type InputUserSelectorEmits,
  type InputUserSelectorProps,
} from "@/views/core/public/service/InputUserSelectorService";

//组件参数
const props = withDefaults(defineProps<InputUserSelectorProps>(), {
  placeholder: "请选择用户",
});

//事件发射器
const emit = defineEmits<InputUserSelectorEmits>();

//已勾选用户IDS 外部用v-model绑定
const bindCheckedUserIds = defineModel<string[]>({ required: true });

//已选用户姓名 外部用v-model:checked-user-names绑定
const bindCheckedUserNames = defineModel<string>("checkedUserNames", { default: "" });

const { modalVisible, onSubmitEntity } = InputUserSelectorService.useInputUserSelector(emit, bindCheckedUserNames);
</script>
