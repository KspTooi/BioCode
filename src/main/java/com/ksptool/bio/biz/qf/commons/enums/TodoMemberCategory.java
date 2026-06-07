package com.ksptool.bio.biz.qf.commons.enums;

import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import lombok.Getter;

/**
 * 待办持久化归类
 * <p>
 * QfTodoPo.memberType 字段使用的持久化值:
 * 0=按单个用户办理、1=按组/部门办理、2=任意人认领
 */
@Getter
public enum TodoMemberCategory {

    /**
     * 按单个用户办理
     */
    USER(0),

    /**
     * 按组/部门办理
     */
    GROUP(1),

    /**
     * 任意人认领
     */
    ANYONE(2);

    private final int value;

    TodoMemberCategory(int value) {
        this.value = value;
    }

    /**
     * 根据审批成员类型解析待办持久化归类
     *
     * @param memberKind 审批成员类型
     * @return 待办持久化归类
     */
    public static TodoMemberCategory fromMemberKind(MemberKind memberKind) {
        if (memberKind == null) {
            throw new IllegalArgumentException("memberKind 不能为空");
        }
        if (memberKind == MemberKind.USER || memberKind == MemberKind.INITIATOR) {
            return USER;
        }
        if (memberKind == MemberKind.GROUP || memberKind == MemberKind.DEPT) {
            return GROUP;
        }
        if (memberKind == MemberKind.ANYONE) {
            return ANYONE;
        }
        throw new IllegalArgumentException("不支持的 memberKind: " + memberKind);
    }

}
