package com.ksptool.bio.biz.qf.commons;

import com.ksptool.assembly.entity.exception.BizException;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
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
}
