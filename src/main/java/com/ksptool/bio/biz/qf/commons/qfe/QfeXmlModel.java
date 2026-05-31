package com.ksptool.bio.biz.qf.commons.qfe;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QfeXmlFormatter 用于格式化模型XML
 * 该类直接操作DOM树，避免通过 BpmnModel 转回 XML 时丢失连线
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 1.7.1(A).1
 */
public class QfeXmlModel {

    private static final String NS_BPMN2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private String bpmnXml;

    /**
     * 从BPMN XML字符串解析并包装为QfeXmlFormatter
     *
     * @param bpmnXml BPMN XML字符串
     * @return QfeXmlFormatter
     */
    public static QfeXmlModel of(String bpmnXml) {
        var model = new QfeXmlModel();
        model.bpmnXml = bpmnXml;
        return model;
    }

    /**
     * 用 DOM4J 直接从 XML 读取所有 userTask 元素及其 QFE 扩展属性；
     * XML 为空或解析失败时返回空列表
     *
     * @return 用户任务列表
     */
    public List<QfeXmlUserTask> getUserTasks() {

        if (StringUtils.isBlank(bpmnXml)) {
            return Collections.emptyList();
        }

        Document doc;
        try {
            doc = new SAXReader().read(new StringReader(bpmnXml));
        } catch (Exception e) {
            return Collections.emptyList();
        }

        var bpmn2Ns = Namespace.get(NS_BPMN2);

        var processQName = QName.get("process", bpmn2Ns);
        var userTaskQName = QName.get("userTask", bpmn2Ns);

        var process = doc.getRootElement().element(processQName);

        if (process == null) {
            return Collections.emptyList();
        }

        List<QfeXmlUserTask> result = new ArrayList<>();
        for (var node : process.elements(userTaskQName)) {
            var ut = new QfeXmlUserTask();
            ut.setElement((Element) node);
            ut.loadBatch();
            result.add(ut);
        }

        return result;
    }

    /**
     * 归一化XML
     * @return 归一化后的XML
     */
    public String normalize() {

        //是否需要添加发起人
        var needInitiator = false;

        //获取所有用户任务
        var userTasks = getUserTasks();

        //遍历用户任务
        for (var xUt : userTasks) {

            
            

        }
        
        return bpmnXml;
    }



}
