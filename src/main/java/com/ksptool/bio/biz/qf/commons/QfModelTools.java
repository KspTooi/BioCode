package com.ksptool.bio.biz.qf.commons;

import com.ksptool.assembly.entity.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.util.io.StringStreamSource;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author KspTool
 * @createTime 2026/4/16 10:00
 */
public class QfModelTools {

    private static final BpmnXMLConverter BPMN_XML_CONVERTER = new BpmnXMLConverter();
    private static final ProcessValidator PROCESS_VALIDATOR = new ProcessValidatorFactory().createDefaultProcessValidator();


    /**
     * 格式化BPMN XML，设置主流程的ID和名称
     * 使用 DOM 直接修改 XML 属性，避免通过 BpmnModel 转回 XML 时丢失连线
     *
     * @param bpmnXml     原始 BPMN XML
     * @param processId   流程ID
     * @param processName 流程名称
     * @return 修改后的 BPMN XML，未找到 process 元素时返回原始 XML
     * @throws BizException XML 解析失败时抛出
     */
    public static String formatBpmnXml(String bpmnXml, String processId, String processName) throws BizException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));
            NodeList processList = document.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "process");
            if (processList.getLength() == 0) {
                return bpmnXml;
            }
            Element processElement = (Element) processList.item(0);
            processElement.setAttribute("id", processId);
            processElement.setAttribute("name", processName);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new BizException("BPMN XML 格式化失败：" + e.getMessage());
        }
    }

    /**
     * 校验BPMN XML是否有效，严格校验，这同时也会校验模型有效性和连线是否正确
     *
     * @param bpmnXml BPMN XML
     * @return 是否有效
     */
    public static boolean isBpmnXmlValid(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.isBlank()) {
            return false;
        }

        BpmnModel model;
        try {
            model = BPMN_XML_CONVERTER.convertToBpmnModel(new StringStreamSource(bpmnXml), false, false);
        } catch (Exception e) {
            return false;
        }

        if (model == null || model.getMainProcess() == null) {
            return false;
        }

        List<ValidationError> errors = PROCESS_VALIDATOR.validate(model);
        return errors.isEmpty();
    }

    /**
     * 提取变量名
     * 把 "${foo}" / "#{foo}" 剥成 "foo"
     *
     * @param el 变量表达式
     * @return 变量名
     */
    public static String extractElVarName(String el) {
        if (StringUtils.isBlank(el)) {
            return null;
        }
        String s = el.trim();
        if (!s.startsWith("${") && !s.startsWith("#{")) {
            return s;
        }
        if (!s.endsWith("}")) {
            return null;
        }
        return s.substring(2, s.length() - 1).trim();
    }

    /**
     * 解析用户任务的候选人
     *
     * @param ut 用户任务
     * @return 候选人ID列表
     * 如果是用户 则返回用户ID列表
     * 如果是用户组 则返回用户组编码列表
     * 如果是组织机构 则返回组织机构ID列表
     */
    public static List<String> resolveCandidates(UserTask ut) {

        if (ut == null) {
            return Collections.emptyList();
        }

        List<String> src = new ArrayList<>();
        if (ut.getCandidateUsers() != null) {
            src.addAll(ut.getCandidateUsers());
        }
        if (ut.getCandidateGroups() != null) {
            src.addAll(ut.getCandidateGroups());
        }
        List<String> out = new ArrayList<>();
        for (String s : src) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            for (String x : s.split(",")) {
                String t = x.trim();
                if (StringUtils.isBlank(t)) {
                    continue;
                }
                out.add(t);
            }
        }
        return out;
    }

    /**
     * 解析办理人类型
     * 根据任务定义键获取办理人类型
     * 这里获取的是前端设计器 (flowable-designer) 扩展的 BPMN 自定义属性 assigneeKind
     *
     * @param model      流程模型
     * @param taskDefKey 任务定义键
     * @return 办理人类型 获取失败返回null
     */
    public static QfMemberKinds resolveMemberKind(BpmnModel model, String taskDefKey) {

        if (model == null || StringUtils.isBlank(taskDefKey)) {
            return null;
        }

        var flowElement = model.getFlowElement(taskDefKey);

        if (!(flowElement instanceof UserTask)) {
            return null;
        }

        var userTask = (UserTask) flowElement;
        String kindStr = userTask.getAttributeValue("http://flowable.org/bpmn", QfVarsModel.ASSIGNEE_KIND.getValue());

        if (kindStr == null || kindStr.isBlank()) {
            return null;
        }

        //用户和发起人都是用户类型
        if(kindStr.equals("user") || kindStr.equals("initiator")){
            return QfMemberKinds.USER;
        }

        //用户组是用户组类型
        if(kindStr.equals("group")){
            return QfMemberKinds.GROUP;
        }

        //组织机构是组织机构类型 但一期我不打算支持这个功能 如果前端设计器非要传这个值，就报错吧
        if(kindStr.equals("dept")){

            //如果能走到这里来 说明前端设计的模型有问题
            throw new RuntimeException("无法解析办理人类型: 组织机构类型不支持, 任务定义键: " + taskDefKey);
        }

        //什么模型会既不是用户类型又不是用户组类型呢？ 这说明前端设计的模型有问题
        return null;
    }
}
