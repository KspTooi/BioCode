package com.ksptool.bio.commons.utils;

import org.apache.commons.lang3.StringUtils;

public class RegistryTool {

    /**
     * 判断keyPath是否合法
     *
     * @param keyPath 全路径
     * @return 是否合法
     */
    public static boolean allowKeyPath(String keyPath) {

        // 空值校验
        if (StringUtils.isBlank(keyPath)) {
            return false;
        }

        // 长度校验 (数据库字段限制为1024)
        if (keyPath.length() > 1024) {
            return false;
        }

        // 首尾不能是点号
        if (keyPath.startsWith(".") || keyPath.endsWith(".")) {
            return false;
        }

        // 不能包含连续的点号
        if (keyPath.contains("..")) {
            return false;
        }

        // 分割各层级的key
        String[] keys = keyPath.split("\\.");

        // 至少要有一个key
        if (keys.length == 0) {
            return false;
        }

        // 校验每个层级的key
        for (String key : keys) {

            // 每个key不能为空
            if (StringUtils.isBlank(key)) {
                return false;
            }

            // 每个key长度限制 (nkey字段限制为128)
            if (key.length() > 128) {
                return false;
            }

            // key只能包含字母、数字、下划线、中划线、中文
            // 不允许特殊字符,避免注入或解析问题
            if (!key.matches("^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$")) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取父级keyPath
     *
     * @param keyPath 全路径
     * @return 父级全路径 如果是顶级节点则返回null
     */
    public static String getParentKeyPath(String keyPath) {

        // 空值校验
        if (StringUtils.isBlank(keyPath)) {
            return null;
        }

        // 先校验keyPath是否合法
        if (!allowKeyPath(keyPath)) {
            return null;
        }

        // 查找最后一个点号的位置
        int lastDotIndex = keyPath.lastIndexOf('.');

        // 如果没有点号,说明是顶级节点,没有父级
        if (lastDotIndex == -1) {
            return null;
        }

        // 返回最后一个点号之前的部分
        return keyPath.substring(0, lastDotIndex);
    }


}
