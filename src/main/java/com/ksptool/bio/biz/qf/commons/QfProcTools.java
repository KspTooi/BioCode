package com.ksptool.bio.biz.qf.commons;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.task.api.Task;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程变量工具类
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 */
public class QfProcTools {


    /**
     * 节点名优先取BPMN里的 name, 无名称时回落到 taskDefinitionKey, 再兜底 "审批"
     */
    public static String nodeName(Task task) {
        if (StringUtils.isNotBlank(task.getName())) {
            return task.getName();
        }
        if (StringUtils.isNotBlank(task.getTaskDefinitionKey())) {
            return task.getTaskDefinitionKey();
        }
        return "审批";
    }

    /**
     * 从流程变量读Long, 兼容 Number/字符串数字, 解析失败返回默认值
     */
    public static long varLong(Map<String, Object> vars, QfVarsProc key, long defaultVal) {
        Object v = vars.get(key.toString());
        if (v == null) {
            return defaultVal;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        return NumberUtils.toLong(String.valueOf(v), defaultVal);
    }

    public static String varString(Map<String, Object> vars, QfVarsProc key, String defaultVal) {
        Object v = vars.get(key.toString());
        if (v == null) {
            return defaultVal;
        }
        return String.valueOf(v);
    }

    public static LocalDateTime varDateTime(Map<String, Object> vars, QfVarsProc key, LocalDateTime defaultVal) {
        Object v = vars.get(key.toString());
        if (v == null) {
            return defaultVal;
        }
        if (v instanceof LocalDateTime ldt) {
            return ldt;
        }
        return defaultVal;
    }

    /**
     * 按数据库列长度截断, 避免超长值写入报错
     */
    public static String trunc(String s, int max) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }


}
