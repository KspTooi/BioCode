package com.ksptool.bio.biz.auth.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * RowScopes 枚举类(RS 7级数据权限-2ID方案)
 * 用于表示数据权限RS的7个等级
 *
 * @author KspTool
 * @since 2026-04-29
 */
@Getter
public enum RowScopes {

    ALL(0, "全部"),
    COMPANY_AND_SUBS(10, "本公司+下级公司"),
    COMPANY_ONLY(20, "仅本公司"),
    DEPT_AND_SUBS(30, "本部门+下级部门"),
    DEPT_ONLY(40, "仅本部门"),
    SELF_ONLY(50, "仅本人"),
    SPECIFIED_ORG(60, "指定组织"),
    DENY_ALL(100, "未配置/拒绝所有");

    private final int code;
    private final String label;

    RowScopes(int code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonCreator
    public static RowScopes of(int code) {
        return Stream.of(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的数据范围等级: " + code));
    }

    public static RowScopes of(Integer code) {
        if (code == null) {
            return null;
        }
        return of(code.intValue());
    }

    @JsonValue
    public int getCode() {
        return code;
    }
    
}
