package com.ksptool.bio.biz.auth.common;

import lombok.Getter;


/**
 * 超级权限枚举
 * 用于调试的权限码,这些权限码在系统中是存在的,但一般不开放给用户使用！
 * @author KspTool
 * @since 1.6.24(X).35
 */
@Getter
public enum CheatPermission {

    //超级操作权限(SA)
    SA("超级操作权限(SA)","*:*:*","拥有此权限的用户组不受任何操作权限限制。"),

    //超级数据权限(SR)
    SR("超级数据权限(SR)","*:*:*:*","拥有此权限的用户组不受任何数据权限限制。"),

    //透视权限
    PERSP("透视权限(SR-PS)","*:*:*:*:PS","拥有此权限将可以在用户列表中看到所有内置用户。");

    private final String name;
    private final String remark;
    private final String code;

    CheatPermission(String name, String code, String remark) {
        this.name = name;
        this.code = code;
        this.remark = remark;
    }

}
