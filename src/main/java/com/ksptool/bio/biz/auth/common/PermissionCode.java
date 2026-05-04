package com.ksptool.bio.biz.auth.common;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 权限码
 * 权限码是最细粒度的权限控制单位，可以精确到每个按钮，甚至每个字段。
 * <p>
 * 权限码必须遵循严格的格式：域:模块:操作
 * <p>
 * 域定义: 系统中的业务域 例如CORE(核心业务域)、AUTH(权限业务域)
 * </p>
 * 模块定义: 系统中的业务模块 例如USER(用户模块)、ROLE(角色模块)、PERMISSION(权限模块)
 * </p>
 * 操作定义: 系统中的业务操作 例如VIEW(查看)、ADD(新增)、EDIT(编辑)、REMOVE(删除)
 * <p>
 * 通配符: 任意一段可以使用 * 表示匹配该段的任意值
 * <p>
 * 示例:
 * core:user:view       精确匹配
 * core:user:*          匹配 core:user 下的所有操作
 * core:*:view          匹配 core 域下所有模块的 view 操作
 * *:*:*                匹配所有权限
 *
 * @author KspTool
 * @since 1.6.21(U).90
 */
@Getter
public class PermissionCode {

    // 合法段名：只允许小写字母、数字、下划线，且必须以字母开头
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    // 通配符
    private static final String WILDCARD = "*";

    // 分隔符
    private static final String SEPARATOR = ":";

    // 完整权限码字符串
    private String code;

    // 域
    private String domain;

    // 模块
    private String module;

    // 操作
    private String operation;


    /**
     * 通过三段构造权限码，自动校验每一段的合法性
     *
     * @param domain    域
     * @param module    模块
     * @param operation 操作
     */
    public PermissionCode(String domain, String module, String operation) {
        resolve(domain + SEPARATOR + module + SEPARATOR + operation);
    }

    /**
     * 通过完整权限码字符串构造，自动解析并校验
     *
     * @param code 权限码字符串，格式：域:模块:操作
     */
    public PermissionCode(String code) {
        resolve(code);
    }

    public static PermissionCode of(String domain, String module, String operation) {
        return new PermissionCode(domain, module, operation);
    }

    public static PermissionCode of(String code) {
        return new PermissionCode(code);
    }

    /**
     * 判断权限码是否匹配
     *
     * @param pattern 权限码模式，格式：域:模块:操作
     * @param value   权限码值
     * @return 是否匹配
     */
    public static boolean matches(String pattern, String value) {

        if (pattern == null || value == null) {
            return false;
        }

        return new PermissionCode(pattern).matches(new PermissionCode(value));
    }

    /**
     * 解析并严格校验权限码
     *
     * @param code 权限码字符串
     * @throws IllegalArgumentException 格式或内容不合法时抛出
     */
    private void resolve(String code) throws IllegalArgumentException {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("权限码不能为空");
        }

        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("权限码不能为纯空白字符");
        }

        // 不允许首尾或中间出现空白字符
        if (!trimmed.equals(code)) {
            throw new IllegalArgumentException("权限码不能包含首尾空白字符: [" + code + "]");
        }

        String[] parts = code.split(":", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "权限码必须由3段组成（域:模块:操作），实际段数: " + parts.length + "，权限码: [" + code + "]");
        }

        String parsedDomain = parts[0];
        String parsedModule = parts[1];
        String parsedOperation = parts[2];

        validateSegment(parsedDomain, "域", code);
        validateSegment(parsedModule, "模块", code);
        validateSegment(parsedOperation, "操作", code);

        this.code = code;
        this.domain = parsedDomain;
        this.module = parsedModule;
        this.operation = parsedOperation;
    }

    /**
     * 校验单个段是否合法：不为空、只含小写字母/数字/下划线、以字母开头，或为通配符 *
     */
    private void validateSegment(String segment, String segmentName, String fullCode) {
        if (segment == null || segment.isEmpty()) {
            throw new IllegalArgumentException(
                    "权限码[" + segmentName + "]段不能为空，权限码: [" + fullCode + "]");
        }
        if (WILDCARD.equals(segment)) {
            return;
        }
        if (!SEGMENT_PATTERN.matcher(segment).matches()) {
            throw new IllegalArgumentException(
                    "权限码[" + segmentName + "]段格式不合法（只允许小写字母开头，含小写字母/数字/下划线，或使用通配符 *）: ["
                            + segment + "]，权限码: [" + fullCode + "]");
        }
    }

    /**
     * 判断当前权限码（可含通配符）是否覆盖目标权限码（必须是精确码）
     * <p>
     * 规则：逐段比对，当前段为 * 时匹配目标段的任意值，否则必须完全相等。
     * <p>
     * 示例：
     * <pre>
     *   PermissionCode.of("core:user:*").matches(PermissionCode.of("core:user:view"))  → true
     *   PermissionCode.of("core:*:*").matches(PermissionCode.of("core:user:view"))     → true
     *   PermissionCode.of("*:*:*").matches(PermissionCode.of("core:user:view"))        → true
     *   PermissionCode.of("auth:user:*").matches(PermissionCode.of("core:user:view"))  → false
     * </pre>
     *
     * @param target 目标精确权限码，不应包含通配符
     * @return 当前权限码覆盖目标时返回 true
     */
    public boolean matches(PermissionCode target) {
        if (target == null) {
            return false;
        }
        return segmentMatches(this.domain, target.domain)
                && segmentMatches(this.module, target.module)
                && segmentMatches(this.operation, target.operation);
    }

    /**
     * matches 的字符串重载
     */
    public boolean matches(String targetCode) {
        return matches(new PermissionCode(targetCode));
    }

    /**
     * 当前权限码是否含有通配符段
     */
    public boolean isWildcard() {
        return WILDCARD.equals(domain)
                || WILDCARD.equals(module)
                || WILDCARD.equals(operation);
    }

    private boolean segmentMatches(String pattern, String value) {
        return WILDCARD.equals(pattern) || pattern.equals(value);
    }

    @Override
    public String toString() {
        return code;
    }

}
