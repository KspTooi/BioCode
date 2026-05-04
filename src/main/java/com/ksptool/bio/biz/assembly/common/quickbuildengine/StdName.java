package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 标准化名称类
 * <p>
 * 标准名称（Standard Name）是指符合大驼峰命名规范（PascalCase）的名称：
 * - 首字母必须是大写字母
 * - 只能包含字母和数字
 * - 不能包含下划线、空格等特殊字符
 * - 示例：LocalUserAccount, UserName, OrderItem
 * <p>
 * 此类提供了多个静态工厂方法，可以将各种格式的名称转换为标准名称：
 * - {@link #of(String)} - 自动检测格式并转换
 * - {@link #ofSnakeCase(String)} - 从下划线命名转换
 * - {@link #ofCamelCase(String)} - 从小驼峰命名转换
 * - {@link #ofLowerCase(String)} - 从全小写转换
 * - {@link #ofUpperCase(String)} - 从全大写转换
 *
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
public class StdName {

    /**
     * -- GETTER --
     * 获取标准名称的字符串值
     *
     * @return 标准名称字符串，如：UserName
     */
    private final String value;

    private StdName(String value) {
        if (!NamesTool.isValidStdName(value)) {
            throw new IllegalArgumentException("标准化名称格式不正确：" + value + "，应为大驼峰命名（PascalCase），首字母必须大写，只能包含字母和数字");
        }
        this.value = value;
    }

    /**
     * 从各种格式的名称创建标准名称
     * 自动检测输入格式并转换为标准名称
     *
     * @param name 输入名称（可以是下划线、小驼峰、大驼峰、全小写、全大写等格式）
     * @return 标准名称对象
     * @throws IllegalArgumentException 如果无法转换为有效的标准名称
     */
    public static StdName of(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("名称不能为空");
        }

        NamesTool.NameFormat format = NamesTool.detectFormat(name);

        if (format == NamesTool.NameFormat.PASCAL_CASE) {
            return new StdName(name);
        }

        if (format == NamesTool.NameFormat.CAMEL_CASE) {
            String stdName = NamesTool.camelCaseToPascalCase(name);
            return new StdName(stdName);
        }

        if (format == NamesTool.NameFormat.SNAKE_CASE) {
            String stdName = NamesTool.toPascalCase(name);
            return new StdName(stdName);
        }

        if (format == NamesTool.NameFormat.LOWER_CASE) {
            String stdName = NamesTool.capitalize(name);
            return new StdName(stdName);
        }

        if (format == NamesTool.NameFormat.UPPER_CASE) {
            String stdName = NamesTool.capitalize(name.toLowerCase());
            return new StdName(stdName);
        }

        String stdName = NamesTool.toPascalCase(name.replaceAll("[^a-zA-Z0-9]", "_"));
        return new StdName(stdName);
    }

    /**
     * 从下划线命名（snake_case）创建标准名称
     *
     * @param snakeCase 下划线命名，如：user_name, local_user_account
     * @return 标准名称对象，如：UserName, LocalUserAccount
     */
    public static StdName ofSnakeCase(String snakeCase) {
        if (StringUtils.isBlank(snakeCase)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        String stdName = NamesTool.toPascalCase(snakeCase);
        return new StdName(stdName);
    }

    /**
     * 从小驼峰命名（camelCase）创建标准名称
     *
     * @param camelCase 小驼峰命名，如：userName, localUserAccount
     * @return 标准名称对象，如：UserName, LocalUserAccount
     */
    public static StdName ofCamelCase(String camelCase) {
        if (StringUtils.isBlank(camelCase)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        String stdName = NamesTool.camelCaseToPascalCase(camelCase);
        return new StdName(stdName);
    }

    /**
     * 从全小写名称创建标准名称
     *
     * @param lowerCase 全小写名称，如：username, localuseraccount
     * @return 标准名称对象，如：Username, Localuseraccount
     */
    public static StdName ofLowerCase(String lowerCase) {
        if (StringUtils.isBlank(lowerCase)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        String stdName = NamesTool.capitalize(lowerCase);
        return new StdName(stdName);
    }

    /**
     * 从全大写名称创建标准名称
     *
     * @param upperCase 全大写名称，如：USERNAME, LOCALUSERACCOUNT
     * @return 标准名称对象，如：Username, Localuseraccount
     */
    public static StdName ofUpperCase(String upperCase) {
        if (StringUtils.isBlank(upperCase)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        String stdName = NamesTool.capitalize(upperCase.toLowerCase());
        return new StdName(stdName);
    }

    /**
     * 获取小驼峰命名
     *
     * @return 小驼峰命名，如：userName
     */
    public String toSmallCamelCase() {
        return NamesTool.toSmallCamelCase(this);
    }

    /**
     * 获取下划线命名
     *
     * @return 下划线命名，如：user_name
     */
    public String toUnderLineName() {
        return NamesTool.toUnderLineName(this);
    }

    /**
     * 获取全小写名称
     *
     * @return 全小写名称，如：username
     */
    public String toLowerCase() {
        return value.toLowerCase();
    }

    /**
     * 获取全大写名称
     *
     * @return 全大写名称，如：USERNAME
     */
    public String toUpperCase() {
        return value.toUpperCase();
    }

    /**
     * 获取标准名称的字符串表示
     *
     * @return 标准名称字符串，如：UserName
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StdName stdName = (StdName) o;
        return value.equals(stdName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
