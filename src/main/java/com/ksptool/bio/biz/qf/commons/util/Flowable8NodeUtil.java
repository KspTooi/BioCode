package com.ksptool.bio.biz.qf.commons.util;

import com.ksptool.bio.biz.qf.commons.enums.NodeStatusEnum;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ProcessNodeVo;
import jakarta.annotation.Resource;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flowable8流程节点工具类
 * 用于获取流程节点状态、生成带颜色标记的BPMN XML等功能
 */
@Component
public class Flowable8NodeUtil {
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;


    /**
     * 生成带审批状态颜色的BPMN XML（兼容运行中和已结束的流程）
     * <p>
     * 颜色通过 bioc:fill / bioc:stroke 属性注入，前端 bpmn-js 可直接渲染
     *
     * @param processInstanceId 流程实例ID
     * @return 带颜色标记的BPMN XML字符串
     */
    public String generateColorBpmnXml(String processInstanceId) {
        try {
            // 1. 查询流程实例（兼容运行中和已结束的流程）
            HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (historicInstance == null) {
                return "<error>流程不存在</error>";
            }

            String processDefinitionId = historicInstance.getProcessDefinitionId();

            // 2. 获取BPMN模型
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            Process process = bpmnModel.getMainProcess();

            // 3. 查询历史上曾经完成过的节点ID（含 userTask / sequenceFlow 等所有类型）
            Set<String> finishedIds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .finished()
                    .list().stream()
                    .map(HistoricActivityInstance::getActivityId)
                    .collect(Collectors.toSet());

            // 4. 查询当前正在运行的节点ID（仅流程未结束时有值）
            // runtimeService.createActivityInstanceQuery 会包含已结束的历史实例，
            // 因此必须再调用 unfinished() 过滤出真正运行中的节点，否则与 finishedIds 产生交集
            Set<String> currentIds = new HashSet<>();
            if (historicInstance.getEndTime() == null) {
                currentIds = runtimeService.createActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .unfinished()
                        .list().stream()
                        .map(ActivityInstance::getActivityId)
                        .collect(Collectors.toSet());
                // current 优先级最高：从 finishedIds 中剔除当前正在运行的节点，保证三个集合互斥
                finishedIds.removeAll(currentIds);
            }

            // 5. 收集待办节点（UserTask 中既未完成也未在运行中的）
            Set<String> pendingIds = new HashSet<>();
            for (FlowElement element : process.getFlowElements()) {
                if (element instanceof UserTask) {
                    String id = element.getId();
                    if (!finishedIds.contains(id) && !currentIds.contains(id)) {
                        pendingIds.add(id);
                    }
                }
            }

            // 6. 生成原始XML
            BpmnXMLConverter converter = new BpmnXMLConverter();
            byte[] xmlBytes = converter.convertToXML(bpmnModel);
            String xml = new String(xmlBytes, StandardCharsets.UTF_8);

            // 7. 后处理：给BPMNShape添加颜色属性
            xml = addColorToBpmnXml(xml, finishedIds, currentIds, pendingIds);

            return xml;

        } catch (Exception e) {
            return "<error>生成失败：" + e.getMessage() + "</error>";
        }
    }


    /**
     * 给BPMN XML中的 BPMNShape / BPMNEdge 节点添加颜色属性（bpmn-js 使用 bioc:fill / bioc:stroke）
     *
     * @param xml 原始BPMN XML字符串
     * @param finishedIds 已完成节点ID集合
     * @param currentIds 当前活动节点ID集合
     * @param pendingIds 待处理节点ID集合
     * @return 添加颜色属性后的BPMN XML字符串
     */
    private String addColorToBpmnXml(String xml, Set<String> finishedIds,
                                     Set<String> currentIds, Set<String> pendingIds) {
        // 添加 bioc 命名空间声明（bpmn-js 需要此命名空间来识别颜色）
        if (!xml.contains("xmlns:bioc=")) {
            xml = xml.replace("xmlns:bpmndi=",
                    "xmlns:bioc=\"http://bpmn.io/schema/bpmn/bioc\" xmlns:bpmndi=");
        }

        // 着色顺序：pending → finished → current（current 最后写，优先级最高）
        xml = colorBpmnShapes(xml, pendingIds, NodeStatusEnum.PENDING);
        xml = colorBpmnShapes(xml, finishedIds, NodeStatusEnum.FINISHED);
        xml = colorBpmnShapes(xml, currentIds, NodeStatusEnum.CURRENT);

        return xml;
    }


    /**
     * 给 XML 中指定 bpmnElement 对应的 Shape/Edge 添加 bioc 颜色属性。
     * 已经注入过颜色的节点直接跳过，保证属性不重复（XML 重复属性会导致 bpmn-js 解析失败）。
     *
     * @param xml BPMN XML字符串
     * @param nodeIds 需要染色的节点ID集合
     * @param status 节点状态枚举，用于获取对应的颜色值
     * @return 添加颜色属性后的BPMN XML字符串
     */
    private String colorBpmnShapes(String xml, Set<String> nodeIds, NodeStatusEnum status) {
        for (String nodeId : nodeIds) {
            String target = "bpmnElement=\"" + nodeId + "\"";
            // 若该节点已被染色（bioc: 属性紧随其后），跳过，避免产生重复属性
            String alreadyColoredMarker = target + " bioc:";
            if (xml.contains(alreadyColoredMarker)) {
                continue;
            }
            String replacement = target
                    + " bioc:stroke=\"" + status.getStroke() + "\""
                    + " bioc:fill=\"" + status.getFill() + "\"";
            xml = xml.replace(target, replacement);
        }
        return xml;
    }
}
