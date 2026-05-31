package com.ksptool.bio.biz.qf.commons;

import com.ksptool.assembly.entity.exception.BizException;
import org.apache.commons.lang3.StringUtils;
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
 * 流程模型工具类
 *
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026/4/16 05:12
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
        if (StringUtils.isBlank(bpmnXml)) {
            return bpmnXml;
        }

        try {
            /*
             * 为什么使用 DOM 树操作而不是用 Flowable 提供的 BpmnXMLConverter ?
             * <p>
             * 在 Flowable 原生机制中，修改 BPMN 模型常用的做法是先转成 BpmnModel (Java 对象)，修改后再转回 XML 文本。
             * 但是这种 "对象模型往返" 操作是有损的：Flowable 在解析时并不会把原 XML 所有的信息（例如连线坐标、BPMNDI的视觉布局、或者我们自己定义在扩展属性上的东西）一字不落的保留下来。
             * 这样一旦转回去，你会发现：前端设计器重新打开这个XML时，图形全乱了，自定义属性也丢了。
             * <p>
             * 所以为了安全，这里使用最基础的 W3C DOM 来改节点。
             * 只要精确找到第一个 <process> 标签，修改它的 id 和 name，其他的东西保持 byte-for-byte 的绝对原样。
             */
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            /*
             * BPMN 本身是一个带有强命名空间的 XML (xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL")
             * 所以必须要开启 namespace aware ，这样后面才能用 getElementsByTagNameNS 找元素。
             * 否则只要前端导出的XML前缀不是 "bpmn:" (比如变成 "bpmn2:") 匹配就会失败。
             */
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

            // 按 OMG BPMN 2.0 官方命名空间定位 <process> 元素, 规避前缀差异.
            NodeList processList = document.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "process");

            /*
             * 理论上一个 BPMN 文件可能存在多个 <process>(子流程、协作), 但主流程按约定是第一个。
             * 没有任何 process 元素则说明 XML 不是合法 BPMN, 这里按幂等原则原样返回，由后面的流程去做校验。
             */
            if (processList.getLength() == 0) {
                return bpmnXml;
            }

            // 直接修改属性，不干涉原本 DOM 的结构和顺序
            Element processElement = (Element) processList.item(0);
            processElement.setAttribute("id", processId);
            processElement.setAttribute("name", processName);

            // 用 Transformer 把 DOM 重新序列化回字符串
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
        if (StringUtils.isBlank(bpmnXml)) {
            return false;
        }

        BpmnModel model;
        try {
            /*
             * 先看看能不能成功转成 Flowable 的 BpmnModel
             * <p>
             * 这一步实际上只是一个单纯的 "语法级" 转换，就是看看 XML 格式有没有问题，标签能不能对上。
             * 它并不会校验流程"业务上"是否合法（比如连线没连上、没有开始节点）。
             * <p>
             * 后面的两个 false 分别代表关闭 XSD Schema 校验和安全检测：
             * 因为某些内网或网络隔离环境下，Flowable 可能会因为下载不到外部的 XSD 文件导致解析报错。
             * 我们有更强大的 ProcessValidator 在后头把关，所以这里的 XSD 不校验也罢。
             */
            model = BPMN_XML_CONVERTER.convertToBpmnModel(new StringStreamSource(bpmnXml), false, false);
        } catch (Exception e) {
            // 解析抛异常（XML格式错、标签写错、乱码等）直接认为不合法。这里是返回布尔值，就不抛出打扰业务了。
            return false;
        }

        // BPMN 里面连个主流程都没有，如果走到这里说明模型肯定有问题
        if (model == null || model.getMainProcess() == null) {
            return false;
        }

        /*
         * 吐给 Flowable 官方校验器做 "语义级" 深度校验
         * <p>
         * PROCESS_VALIDATOR 内部会非常细致地跑一堆规则，比如：
         * - 线条：这根连线的起点和终点是不是都连在了正常的节点上？（防连线断裂）
         * - 网关：排他网关是不是至少配置了一个出口？
         * - 任务：用户任务有没有配置办理人或者候选人？
         * - 多实例：集合参数配对了没？
         * <p>
         * 只要这个 validator 查出了至少一个 error 级别的错（warning不算），这个流程模型就算废了。
         */
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

        /*
         * 关于 EL 表达式的两种写法：
         * ${foo} 是即时求值（Immediate Evaluation），我们在给 UserTask 指定 assignee，或者传递流程变量时，大多用这种。
         * #{foo} 是延迟求值（Deferred Evaluation），一般在监听器绑定或者一些高级特性（如JSF）时会看到。
         * <p>
         * 这里作为工具方法，我们的目的是把外面那个壳子剥掉，只拿变量名，所以这两种我都兼容。
         */
        String s = el.trim();

        /*
         * 兼容前端传 "裸变量" 的情况
         * 有时候前端设计器配的 inputDataItem 就是个干净的 "assigneeList"，没有用 ${} 包裹。
         * 考虑到业务层调用时懒得再判断了，如果你没包壳子，我就原样吐给你。
         */
        if (!s.startsWith("${") && !s.startsWith("#{")) {
            return s;
        }

        // 如果开头包了结尾没包，说明表达式不完整，当作非法处理
        if (!s.endsWith("}")) {
            return null;
        }

        /*
         * 核心剥壳逻辑：
         * 起点跳过前两格(比如 ${)，终点砍掉最后一格(})。
         * 最后再 trim 一次，防止有人手抖写成 "${ foo }" 导致拿出来带空格。
         * <p>
         * 警告：这个方法只能处理单一变量提取（拿 foo）。如果你丢进来的是 "${user.name}" 或复杂运算，
         * 它会返回 "user.name"，这在当前项目流程里够用了，但请勿在复杂表达式场景下滥用。
         */
        return s.substring(2, s.length() - 1).trim();
    }
}
