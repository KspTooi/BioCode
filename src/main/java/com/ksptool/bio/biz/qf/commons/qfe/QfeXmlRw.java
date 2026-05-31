package com.ksptool.bio.biz.qf.commons.qfe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于读写 XML 元素的工具类；绑定 element 与首选命名空间后，
 * getNs/setNs 系列方法只需传属性局部名，QName 由内部自动构建
 */
public class QfeXmlRw {

    //原始XML元素
    private Element element;

    //首选命名空间，getNs*/setNs* 方法用它自动构建 QName
    private Namespace ns;

    /**
     * 绑定 XML 元素
     *
     * @param element 目标元素
     * @return 当前实例
     */
    public static QfeXmlRw of(Element element, Namespace ns) {
        var rw = new QfeXmlRw();
        rw.element = element;
        rw.ns = ns;
        return rw;
    }

    public static QfeXmlRw of(Element element) {
        return of(element, null);
    }


    // ==============================
    // 无命名空间 读写
    // ==============================

    /**
     * 获取无命名空间属性的字符串值
     *
     * @param name 属性局部名
     * @return 属性值，element 为 null 时返回 null
     */
    public String getAttr(String name) {
        if (element == null) {
            return null;
        }
        return element.attributeValue(name);
    }

    /**
     * 设置无命名空间属性的字符串值；element/value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value 属性值
     */
    public void setAttr(String name, String value) {
        if (element == null || value == null) {
            return;
        }
        element.addAttribute(name, value);
    }

    /**
     * 获取无命名空间属性的 Integer 值；无法解析时返回 null
     *
     * @param name 属性局部名
     * @return Integer 值
     */
    public Integer getAttrInt(String name) {
        return parseIntVal(getAttr(name));
    }

    /**
     * 设置无命名空间属性的 Integer 值；value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value Integer 值
     */
    public void setAttrInt(String name, Integer value) {
        if (value == null) {
            return;
        }
        setAttr(name, String.valueOf(value));
    }

    /**
     * 获取无命名空间属性的 Long 值；无法解析时返回 null
     *
     * @param name 属性局部名
     * @return Long 值
     */
    public Long getAttrLong(String name) {
        return parseLongVal(getAttr(name));
    }

    /**
     * 设置无命名空间属性的 Long 值；value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value Long 值
     */
    public void setAttrLong(String name, Long value) {
        if (value == null) {
            return;
        }
        setAttr(name, String.valueOf(value));
    }

    /**
     * 读取逗号分隔的无命名空间 Integer 数组；element 为 null 或解析失败时返回空列表
     *
     * @param name 属性局部名
     * @return Integer 列表
     */
    public List<Integer> getArrayInt(String name) {
        if (element == null) {
            return Collections.emptyList();
        }
        return parseIntList(element.attributeValue(name));
    }

    /**
     * 将 Integer 列表以逗号分隔写入无命名空间属性；element 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values Integer 列表
     */
    public void setArrayInt(String name, List<Integer> values) {
        if (element == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(name, joinInt(values));
    }

    /**
     * 读取逗号分隔的无命名空间 Long 数组；element 为 null 或解析失败时返回空列表
     *
     * @param name 属性局部名
     * @return Long 列表
     */
    public List<Long> getArrayLong(String name) {
        if (element == null) {
            return Collections.emptyList();
        }
        return parseLongList(element.attributeValue(name));
    }

    /**
     * 将 Long 列表以逗号分隔写入无命名空间属性；element 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values Long 列表
     */
    public void setArrayLong(String name, List<Long> values) {
        if (element == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(name, joinLong(values));
    }

    /**
     * 读取逗号分隔的无命名空间 String 数组；element 为 null 时返回空列表
     *
     * @param name 属性局部名
     * @return String 列表
     */
    public List<String> getArrayString(String name) {
        if (element == null) {
            return Collections.emptyList();
        }
        return parseStringList(element.attributeValue(name));
    }

    /**
     * 将 String 列表以逗号分隔写入无命名空间属性；element 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values String 列表
     */
    public void setArrayString(String name, List<String> values) {
        if (element == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(name, String.join(",", values));
    }

    // ==============================
    // 带命名空间 读写（传局部名，内部用 ns 自动构建 QName）
    // ==============================

    /**
     * 获取带命名空间属性的字符串值；element/ns 为 null 时返回 null
     *
     * @param name 属性局部名
     * @return 属性值
     */
    public String getNsAttr(String name) {
        if (element == null || ns == null) {
            return null;
        }
        return element.attributeValue(QName.get(name, ns));
    }

    /**
     * 设置带命名空间属性的字符串值；element/ns/value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value 属性值
     */
    public void setNsAttr(String name, String value) {
        if (element == null || ns == null || value == null) {
            return;
        }
        element.addAttribute(QName.get(name, ns), value);
    }

    /**
     * 获取带命名空间属性的 Integer 值；无法解析时返回 null
     *
     * @param name 属性局部名
     * @return Integer 值
     */
    public Integer getNsAttrInt(String name) {
        return parseIntVal(getNsAttr(name));
    }

    /**
     * 设置带命名空间属性的 Integer 值；value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value Integer 值
     */
    public void setNsAttrInt(String name, Integer value) {
        if (value == null) {
            return;
        }
        setNsAttr(name, String.valueOf(value));
    }

    /**
     * 获取带命名空间属性的 Long 值；无法解析时返回 null
     *
     * @param name 属性局部名
     * @return Long 值
     */
    public Long getNsAttrLong(String name) {
        return parseLongVal(getNsAttr(name));
    }

    /**
     * 设置带命名空间属性的 Long 值；value 为 null 时跳过
     *
     * @param name  属性局部名
     * @param value Long 值
     */
    public void setNsAttrLong(String name, Long value) {
        if (value == null) {
            return;
        }
        setNsAttr(name, String.valueOf(value));
    }

    /**
     * 读取带命名空间的逗号分隔 Integer 数组；element/ns 为 null 时返回空列表
     *
     * @param name 属性局部名
     * @return Integer 列表
     */
    public List<Integer> getNsArrayInt(String name) {
        if (element == null || ns == null) {
            return Collections.emptyList();
        }
        return parseIntList(element.attributeValue(QName.get(name, ns)));
    }

    /**
     * 将 Integer 列表以逗号分隔写入带命名空间属性；element/ns 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values Integer 列表
     */
    public void setNsArrayInt(String name, List<Integer> values) {
        if (element == null || ns == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(QName.get(name, ns), joinInt(values));
    }

    /**
     * 读取带命名空间的逗号分隔 Long 数组；element/ns 为 null 时返回空列表
     *
     * @param name 属性局部名
     * @return Long 列表
     */
    public List<Long> getNsArrayLong(String name) {
        if (element == null || ns == null) {
            return Collections.emptyList();
        }
        return parseLongList(element.attributeValue(QName.get(name, ns)));
    }

    /**
     * 将 Long 列表以逗号分隔写入带命名空间属性；element/ns 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values Long 列表
     */
    public void setNsArrayLong(String name, List<Long> values) {
        if (element == null || ns == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(QName.get(name, ns), joinLong(values));
    }

    /**
     * 读取带命名空间的逗号分隔 String 数组；element/ns 为 null 时返回空列表
     *
     * @param name 属性局部名
     * @return String 列表
     */
    public List<String> getNsArrayString(String name) {
        if (element == null || ns == null) {
            return Collections.emptyList();
        }
        return parseStringList(element.attributeValue(QName.get(name, ns)));
    }

    /**
     * 将 String 列表以逗号分隔写入带命名空间属性；element/ns 为 null 或列表为空时跳过
     *
     * @param name   属性局部名
     * @param values String 列表
     */
    public void setNsArrayString(String name, List<String> values) {
        if (element == null || ns == null || values == null || values.isEmpty()) {
            return;
        }
        element.addAttribute(QName.get(name, ns), String.join(",", values));
    }

    // ==============================
    // 解析辅助
    // ==============================

    private Integer parseIntVal(String s) {
        if (StringUtils.isBlank(s) || !NumberUtils.isCreatable(s.trim())) {
            return null;
        }
        return Integer.parseInt(s.trim());
    }

    private Long parseLongVal(String s) {
        if (StringUtils.isBlank(s) || !NumberUtils.isCreatable(s.trim())) {
            return null;
        }
        return Long.parseLong(s.trim());
    }

    private List<Integer> parseIntList(String s) {
        if (StringUtils.isBlank(s)) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .filter(NumberUtils::isCreatable)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<Long> parseLongList(String s) {
        if (StringUtils.isBlank(s)) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .filter(NumberUtils::isCreatable)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private List<String> parseStringList(String s) {
        if (StringUtils.isBlank(s)) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private String joinInt(List<Integer> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String joinLong(List<Long> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

}
