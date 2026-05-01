import flowableDescriptor from "@/views/qf/sfc_private/flowable-designer/flowableDescriptor.json";
import biocDescriptor from "@/views/qf/sfc_private/flowable-designer/biocDescriptor.json";

export function getFlowableModdleExtensions(): Record<string, Record<string, unknown>> {
  return {
    flowable: flowableDescriptor as unknown as Record<string, unknown>,
    // 注册 bioc 命名空间描述符，使 moddle 能正确解析 bioc:fill / bioc:stroke 属性
    bioc: biocDescriptor as unknown as Record<string, unknown>,
  };
}

const FLOWABLE_NS = "http://flowable.org/bpmn";

function escapeXml(text: string): string {
  if (!text) {
    return "";
  }
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/** 生成含开始事件的空白可执行流程（与 moddle 中 flow 命名空间一致） */
export function buildEmptyFlowableDiagram(processId: string, processName: string): string {
  const safeName = escapeXml(processName);
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns:flowable="${FLOWABLE_NS}"
  id="Definitions_${processId}"
  targetNamespace="${FLOWABLE_NS}">
  <bpmn2:process id="${processId}" name="${safeName}" isExecutable="true">
    <bpmn2:startEvent id="StartEvent_1" name="开始" />
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${processId}">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="99" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`;
}
