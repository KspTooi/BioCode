<!--
  * 组织机构输入选择器
  * 这是InputOrgTree的轻量包装，提供单选的交互形式。
  * 所有属性都会透传给InputOrgTree组件 具体参考InputOrgTree组件的属性
-->
<template>
  <InputOrgTree v-model="draftCheckedOrgId" v-bind="$attrs" mode="single" @on-submit-entity="onSubmitEntity" />
</template>

<script setup lang="ts">
import InputOrgTree from "@/views/core/public/InputOrgTree.vue";
import { computed } from "vue";
import type { GetOrgTreeVo } from "@/views/core/api/OrgApi";

//事件发射器
const emit = defineEmits<{
  (e: "on-submit-entity", vo: GetOrgTreeVo): void;
}>();

//已勾选组织机构ID数组 外部用v-model绑定
const bindCheckedOrgId = defineModel<string>({ required: true });

//草稿已选组织机构ID 使用一个可写的计算把多选包装成单选
const draftCheckedOrgId = computed<string[]>({
  get: () => (bindCheckedOrgId.value ? [bindCheckedOrgId.value] : []),
  set: (vals) => {
    bindCheckedOrgId.value = vals?.[0] ?? "";
  },
});

//同步包装事件 事件也要变成单选
const onSubmitEntity = (vos: GetOrgTreeVo[]): void => {
  emit("on-submit-entity", vos[0]);
};
</script>
