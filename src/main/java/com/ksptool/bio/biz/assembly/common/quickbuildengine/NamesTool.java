package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import org.apache.commons.lang3.StringUtils;

/**
 * 名称转换工具类
 * 提供各种命名格式之间的转换方法
 *
 * @author KspTooi
 * @since 1.6.19(S).32
 */
public class NamesTool {

    /**
     * 将标准名称转换为小驼峰命名
     *
     * @param stdName 标准名称对象
     * @return 小驼峰命名（camelCase），如：userName
     */
    public static String toSmallCamelCase(StdName stdName) {
        if (stdName == null) {
            return null;
        }
        String value = stdName.getValue();
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() == 1) {
            return value.toLowerCase();
        }
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    /**
     * 将标准名称转换为下划线命名
     *
     * @param stdName 标准名称对象
     * @return 下划线命名（snake_case），如：local_user_account
     */
    public static String toUnderLineName(StdName stdName) {
        if (stdName == null) {
            return null;
        }
        String value = stdName.getValue();
        if (StringUtils.isBlank(value)) {
            return value;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    char prevChar = value.charAt(i - 1);
                    if (Character.isLowerCase(prevChar) || (i < value.length() - 1 && Character.isLowerCase(value.charAt(i + 1)))) {
                        result.append('_');
                    }
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 将下划线命名转换为大驼峰命名
     *
     * @param snakeCase 下划线命名（snake_case），如：user_name
     * @return 大驼峰命名（PascalCase），如：UserName
     */
    public static String toPascalCase(String snakeCase) {
        if (StringUtils.isBlank(snakeCase)) {
            return snakeCase;
        }
        String[] parts = snakeCase.toLowerCase().split("_");
        if (parts.length == 0) {
            return snakeCase;
        }
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                result.append(capitalize(part));
            }
        }
        return result.toString();
    }

    /**
     * 将小驼峰命名转换为大驼峰命名
     *
     * @param camelCase 小驼峰命名（camelCase），如：userName
     * @return 大驼峰命名（PascalCase），如：UserName
     */
    public static String camelCaseToPascalCase(String camelCase) {
        if (StringUtils.isBlank(camelCase)) {
            return camelCase;
        }
        return capitalize(camelCase);
    }

    /**
     * 将首字母大写
     *
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 判断是否为有效的标准名称（大驼峰命名）
     *
     * @param name 名称
     * @return 是否为有效的标准名称
     */
    public static boolean isValidStdName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }

        char firstChar = name.charAt(0);
        if (!Character.isUpperCase(firstChar)) {
            return false;
        }

        if (!Character.isLetter(firstChar)) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检测名称的格式类型
     *
     * @param name 名称
     * @return 格式类型：PASCAL_CASE, CAMEL_CASE, SNAKE_CASE, UPPER_CASE, LOWER_CASE, UNKNOWN
     */
    public static NameFormat detectFormat(String name) {
        if (StringUtils.isBlank(name)) {
            return NameFormat.UNKNOWN;
        }

        boolean hasUnderscore = name.contains("_");
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean startsWithUpper = Character.isUpperCase(name.charAt(0));

        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
        }

        if (hasUnderscore) {
            return NameFormat.SNAKE_CASE;
        }
        if (name.equals(name.toUpperCase())) {
            return NameFormat.UPPER_CASE;
        }
        if (name.equals(name.toLowerCase())) {
            return NameFormat.LOWER_CASE;
        }
        if (startsWithUpper && hasUpperCase && hasLowerCase) {
            return NameFormat.PASCAL_CASE;
        }
        if (!startsWithUpper && hasUpperCase && hasLowerCase) {
            return NameFormat.CAMEL_CASE;
        }

        return NameFormat.UNKNOWN;
    }

    /**
     * 名称格式枚举
     */
    public enum NameFormat {
        PASCAL_CASE,    // 大驼峰：UserName
        CAMEL_CASE,     // 小驼峰：userName
        SNAKE_CASE,     // 下划线：user_name
        UPPER_CASE,     // 全大写：USERNAME
        LOWER_CASE,     // 全小写：username
        UNKNOWN         // 未知格式
    }

}
