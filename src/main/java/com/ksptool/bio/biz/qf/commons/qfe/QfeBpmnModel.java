package com.ksptool.bio.biz.qf.commons.qfe;

import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.AprKind;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.util.io.StringStreamSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QfeBpmnModel {

    private static final BpmnXMLConverter BPMN_XML_CONVERTER = new BpmnXMLConverter();

    //多实例 collection 变量前缀(${qfMi_<节点ID>})，与 QfProcService 注入、QfMemberService 解析保持一致
    private static final String MI_VAR_PREFIX = "qfMi_";

    //多实例 elementVariable 约定名，每个实例的办理人写入该变量
    private static final String MI_ELEMENT_VAR = "assignee";

    private BpmnModel bpmnModel;

    public static void main(String[] args) {

        var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:flowable="http://flowable.org/bpmn" xmlns:qfe="quick_flow_extstion" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="Definitions_Process_1777278881577" targetNamespace="http://flowable.org/bpmn">
                  <bpmn2:process id="qf_model_1376837476013969408" name="随手拍" isExecutable="true">
                    <bpmn2:startEvent id="StartEvent_1" name="开始" flowable:initiator="initiator">
                      <bpmn2:outgoing>Flow_0sxufbp</bpmn2:outgoing>
                    </bpmn2:startEvent>
                    <bpmn2:userTask id="Activity_1ilwu8h" name="隐患上报" flowable:assignee="1339890159700807681" flowable:assigneeKind="user" flowable:candidateUserNames="平台管理员" qfe:utAprKind="0" qfe:utAprMemberKind="0" qfe:utAprMi="0" qfe:utAprActions="0,1" qfe:utAprActionNames="驳回1,同意" qfe:utAprComment="1">
                      <bpmn2:incoming>Flow_0sxufbp</bpmn2:incoming>
                      <bpmn2:outgoing>Flow_00pwf0n</bpmn2:outgoing>
                    </bpmn2:userTask>
                    <bpmn2:endEvent id="Event_0129rx6">
                      <bpmn2:incoming>Flow_00pwf0n</bpmn2:incoming>
                    </bpmn2:endEvent>
                    <bpmn2:sequenceFlow id="Flow_00pwf0n" sourceRef="Activity_1ilwu8h" targetRef="Event_0129rx6" />
                    <bpmn2:sequenceFlow id="Flow_0sxufbp" sourceRef="StartEvent_1" targetRef="Activity_1ilwu8h" />
                  </bpmn2:process>
                  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="qf_model_1376837476013969408">
                      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
                        <dc:Bounds x="152" y="-128" width="36" height="36" />
                        <bpmndi:BPMNLabel>
                          <dc:Bounds x="158" y="-152" width="23" height="14" />
                        </bpmndi:BPMNLabel>
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNShape id="Activity_05xj5jc_di" bpmnElement="Activity_1ilwu8h">
                        <dc:Bounds x="120" y="90" width="100" height="80" />
                        <bpmndi:BPMNLabel />
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNShape id="Event_0129rx6_di" bpmnElement="Event_0129rx6">
                        <dc:Bounds x="152" y="262" width="36" height="36" />
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNEdge id="Flow_00pwf0n_di" bpmnElement="Flow_00pwf0n">
                        <di:waypoint x="220" y="130" />
                        <di:waypoint x="300" y="130" />
                        <di:waypoint x="300" y="280" />
                        <di:waypoint x="188" y="280" />
                      </bpmndi:BPMNEdge>
                      <bpmndi:BPMNEdge id="Flow_0sxufbp_di" bpmnElement="Flow_0sxufbp">
                        <di:waypoint x="170" y="-92" />
                        <di:waypoint x="170" y="90" />
                      </bpmndi:BPMNEdge>
                    </bpmndi:BPMNPlane>
                  </bpmndi:BPMNDiagram>
                </bpmn2:definitions>
                """;

        var qbm = new QfeBpmnModel().of(xml);
        var result = qbm.validateUserTasks();

        if (result != null) {
            System.out.println(result);
            return;
        }

        System.out.println("验证已通过，未发现问题。");

    }

    /**
     * 从 BpmnModel 包装为 QfeBpmnModel
     *
     * @param bpmnModel 已解析的 BpmnModel
     * @return 当前实例
     */
    public QfeBpmnModel of(BpmnModel bpmnModel) {
        this.bpmnModel = bpmnModel;
        return this;
    }

    /**
     * 从 BPMN XML 字符串解析并包装为 QfeBpmnModel；XML 为空时 bpmnModel 保持 null
     *
     * @param xml BPMN XML 字符串
     * @return 当前实例
     */
    public QfeBpmnModel of(String xml) {
        if (StringUtils.isBlank(xml)) {
            return this;
        }
        this.bpmnModel = BPMN_XML_CONVERTER.convertToBpmnModel(new StringStreamSource(xml), false, false);
        return this;
    }

    public BpmnModel getBpmnModel() {
        return bpmnModel;
    }

    /**
     * 获取主流程中所有 UserTask，按 BPMN 元素顺序返回；bpmnModel 未初始化时返回空列表
     *
     * @return QfeUserTask 包装列表
     */
    public List<QfeUserTask> getUserTasks() {
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            return Collections.emptyList();
        }
        List<QfeUserTask> result = new ArrayList<>();
        for (var element : bpmnModel.getMainProcess().getFlowElements()) {
            if (!(element instanceof UserTask ut)) {
                continue;
            }
            result.add(new QfeUserTask().of(ut));
        }
        return result;
    }

    /**
     * 按任务定义键获取单个 UserTask 包装；不存在或非 UserTask 时返回 null
     *
     * @param taskDefKey 任务定义键（BPMN UserTask 的 id）
     * @return QfeUserTask 包装，未命中返回 null
     */
    public QfeUserTask getUserTask(String taskDefKey) {
        if (bpmnModel == null || StringUtils.isBlank(taskDefKey)) {
            return null;
        }
        if (!(bpmnModel.getFlowElement(taskDefKey) instanceof UserTask ut)) {
            return null;
        }
        return new QfeUserTask().of(ut);
    }

    /**
     * 按节点名称获取单个 UserTask 包装；存在同名节点时返回首个，未命中返回 null
     *
     * @param name BPMN UserTask 的 name
     * @return QfeUserTask 包装，未命中返回 null
     */
    public QfeUserTask getUserTaskByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (var ut : getUserTasks()) {
            if (name.equals(ut.getName())) {
                return ut;
            }
        }
        return null;
    }

    /**
     * 获取指定节点的全部上游 UserTask（沿 incoming 反向追溯，跳过网关）
     * <p>
     * 用于"驳回到节点"等场景的候选上游节点收集；遇到 UserTask 即收集且不再向上追溯，
     * 遇到网关则继续向上，其余节点（开始事件等）终止该分支。
     *
     * @param nodeName 起始节点名称
     * @return 上游 UserTask 包装列表；模型未初始化或起点不存在时返回空列表
     */
    public List<QfeUserTask> getUpstreamUserTasks(String nodeName) {
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            return Collections.emptyList();
        }
        var start = getUserTaskByName(nodeName);
        if (start == null) {
            return Collections.emptyList();
        }

        //构建 targetRef -> sourceRef 列表的反向邻接表
        Map<String, List<String>> incoming = new HashMap<>();
        for (var element : bpmnModel.getMainProcess().getFlowElements()) {
            if (!(element instanceof SequenceFlow flow)) {
                continue;
            }
            if (StringUtils.isBlank(flow.getSourceRef()) || StringUtils.isBlank(flow.getTargetRef())) {
                continue;
            }
            incoming.computeIfAbsent(flow.getTargetRef(), k -> new ArrayList<>()).add(flow.getSourceRef());
        }

        List<QfeUserTask> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        var stack = new ArrayDeque<String>();
        stack.push(start.getId());
        while (!stack.isEmpty()) {
            var nodeId = stack.pop();
            for (var sourceRef : incoming.getOrDefault(nodeId, Collections.emptyList())) {
                if (!visited.add(sourceRef)) {
                    continue;
                }
                var source = bpmnModel.getFlowElement(sourceRef);
                if (source instanceof UserTask ut) {
                    result.add(new QfeUserTask().of(ut));
                    continue;
                }
                if (source instanceof Gateway) {
                    stack.push(sourceRef);
                }
            }
        }
        return result;
    }

    /**
     * 校验Qfe模型的全部用户任务配置
     *
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public String validateUserTasks() {

        var userTasks = getUserTasks();

        if (userTasks.isEmpty()) {
            return null;
        }

        for (var qfeUserTask : userTasks) {

            var utId = qfeUserTask.getUserTask().getId();
            var utName = qfeUserTask.getUserTask().getName();

            var approveKind = qfeUserTask.getApproveKind();
            var memberKind = qfeUserTask.getMemberKind();
            var miKind = qfeUserTask.getMultiInstanceKind();
            var actions = qfeUserTask.getActions();

            //基础非空与非法值验证
            if (approveKind == null) {
                return "节点[" + utId + "][" + utName + "]的处理人类型不能为空，请配置qfe:utAprKind";
            }

            if (memberKind == null) {
                return "节点[" + utId + "][" + utName + "]的处理人配置不能为空，请配置qfe:utAprMemberKind";
            }

            if (miKind == null) {
                return "节点[" + utId + "][" + utName + "]的多实例类型不能为空，请配置qfe:utAprMi";
            }

            if (actions.isEmpty()) {
                return "节点[" + utId + "][" + utName + "]至少需要有一个允许的审批操作，请配置qfe:utAprActions";
            }

            //多实例(会签/或签/自定义)节点：流程定义必须自带标准多实例特性，且 collection/elementVariable 符合 QF 约定
            //(后端不再在部署期注入多实例，完整 BPMN 由前端设计器产出)
            if (qfeUserTask.isMultiInstance()) {
                MultiInstanceLoopCharacteristics loop = qfeUserTask.getUserTask().getLoopCharacteristics();
                if (loop == null) {
                    return "多实例节点[" + utId + "][" + utName + "]缺少多实例配置(multiInstanceLoopCharacteristics)，请在设计器中重新保存。";
                }
                var expectCollection = "${" + MI_VAR_PREFIX + utId + "}";
                if (!expectCollection.equals(loop.getInputDataItem())) {
                    return "多实例节点[" + utId + "][" + utName + "]的集合表达式必须为" + expectCollection
                            + "，当前为[" + loop.getInputDataItem() + "]。";
                }
                if (!MI_ELEMENT_VAR.equals(loop.getElementVariable())) {
                    return "多实例节点[" + utId + "][" + utName + "]的元素变量(elementVariable)必须为" + MI_ELEMENT_VAR
                            + "，当前为[" + loop.getElementVariable() + "]。";
                }
            }


            //处理人类型为"标准"时的验证
            if (approveKind == AprKind.STANDARD) {

                //处理人配置只能配为 指定用户、用户组、发起人
                if (memberKind != MemberKind.USER && memberKind != MemberKind.GROUP && memberKind != MemberKind.INITIATOR) {
                    return "标准节点[" + utId + "][" + utName + "]的处理人配置只允许为指定用户、用户组、发起人。";
                }

                //多实例为自定义必须配表达式
                if (qfeUserTask.isMultiInstance()) {

                    var exp = qfeUserTask.getMultiInstanceExpress();

                    if (StringUtils.isBlank(exp)) {
                        return "标准节点[" + utId + "][" + utName + "]的多实例为自定义时必须配表达式。";
                    }

                }

                //标准节点若处理人配置选择为 指定用户、用户组 必须至少配置一名处理人
                if (memberKind == MemberKind.USER || memberKind == MemberKind.GROUP) {
                    if (qfeUserTask.getMemberIds().isEmpty()) {
                        return "标准节点[" + utId + "][" + utName + "]的处理人配置选择为指定用户、用户组时必须至少配置一名处理人。";
                    }
                }

            }


            //处理人类型为 "发起时选人" 时的验证
            if (approveKind == AprKind.INIT_SELECTED) {

                //处理人配置只能配为 任意人、指定用户、用户组
                if (memberKind != MemberKind.ANYONE && memberKind != MemberKind.USER && memberKind != MemberKind.GROUP) {
                    return "发起时选人节点[" + utId + "][" + utName + "]的处理人配置只允许为任意人、指定用户、用户组。";
                }

                //如果配了指定用户、用户组，必须至少配置一名处理人(此处配置为选择人员范围)
                if (memberKind == MemberKind.USER || memberKind == MemberKind.GROUP) {
                    if (qfeUserTask.getMemberIds().isEmpty()) {
                        return "发起时选人节点[" + utId + "][" + utName + "]的处理人配置选择为指定用户、用户组时至少需指定一名处理人。";
                    }
                }

            }


        }

        return null;
    }
}
