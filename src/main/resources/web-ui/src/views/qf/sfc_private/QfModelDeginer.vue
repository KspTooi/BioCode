<template>
  <div v-loading="loading" class="qf-model-deginer-page w-full h-full">
    <FlowableDesigner
      v-if="!loading"
      :initial-xml="initialXml"
      :readonly="readonly"
      :form-id="formId"
      @update:xml="onUpdateXml"
      @save="onSave"
      @exit="onExit"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import FlowableDesigner from "@/views/qf/sfc_private/flowable-designer/FlowableDesigner.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import type { GetQfModelListVo } from "@/views/qf/api/QfModelApi.ts";
import QfModelApi from "@/views/qf/api/QfModelApi.ts";
import { ElMessage } from "element-plus";

//从CDRC获取参数
const { cdrcReturn, getCdrcQuery, cdrcRedirect } = ComDirectRouteContext.useDirectRouteContext();
const initialXml = ref<string>("");
const rows = ref<GetQfModelListVo | null>(null);
const readonly = ref<boolean>(false);
const formId = ref<string | null>(null);

const loading = ref<boolean>(true);

onMounted(async () => {
  //从CDRC获取参数
  const query: GetQfModelListVo = getCdrcQuery();

  if (!query) {
    cdrcReturn();
    return;
  }

  //保存查询参数
  rows.value = query;

  //查询模型详情
  try {
    loading.value = true;
    const details = await QfModelApi.getQfModelDetails({ id: query.id });
    initialXml.value = details.bpmnXml;
    formId.value = details.formId;

    //如果模型不是草稿状态，则只读
    if (query.status !== 0) {
      readonly.value = true;
    }
  } catch {
    ElMessage.error("查询模型详情失败");
    cdrcReturn();
    return;
  } finally {
    loading.value = false;
  }
});

const xml = ref("");

function onUpdateXml(next: string): void {
  xml.value = next;
}

/**
 * 保存BPMN XML
 */
async function onSave(next: string): Promise<void> {
  if (readonly.value) {
    return;
  }

  xml.value = next;

  if (!rows.value) {
    ElMessage.error("无法保存模型设计，传递的模型ID不正确。");
    return;
  }

  try {
    await QfModelApi.designQfModel({ id: rows.value?.id, bpmnXml: next });
    ElMessage.success("保存模型设计成功");
    cdrcRedirect("qfModel");
  } catch (error: any) {
    ElMessage.error(error.message);
  }
}

/**
 * 退出设计
 */
function onExit(): void {
  cdrcRedirect("qfModel");
}
</script>

<style scoped>
.qf-model-deginer-page {
  height: calc(100vh - 96px);
  min-height: 560px;
  box-sizing: border-box;
}
</style>
