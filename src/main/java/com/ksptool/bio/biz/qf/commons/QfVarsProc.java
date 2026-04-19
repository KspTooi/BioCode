package com.ksptool.bio.biz.qf.commons;

import lombok.Getter;

/**
 * QF域约定的流程变量 Key
 * <p>
 * 发起流程时通过这些 Key 往流程实例注入业务数据与发起人信息,
 * 监听器 (如 QfTaskCreatedListener) 在 TASK_CREATED 回调中按相同 Key 读取,
 * 用于脱离Web会话上下文的情况下还原 rootId/deptId/initiator 等字段。
 * <p>
 * 统一以 "QF_" 前缀避免与业务自定义流程变量冲突。
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Getter
public enum QfVarsProc {

    /**
     * --------------------------------发起方数据--------------------------------
     */
    //所属企业/租户ID(这里是发起人的所属企业/租户ID)
    ROOT_ID("QF_ROOT_ID"),

    //所属部门ID (这里是发起人的所属部门ID)
    DEPT_ID("QF_DEPT_ID"),

    //发起人用户ID
    INITIATOR_ID("QF_INITIATOR_ID"),

    //发起人显示名
    INITIATOR_NAME("QF_INITIATOR_NAME"),

    //发起时间 (这里是发起流程的时间)
    INITIATOR_TIME("QF_INITIATOR_TIME"),

    /**
     * --------------------------------业务数据--------------------------------
     */
    //业务表单ID (qf_biz_form.id)
    BIZ_FORM_ID("QF_BIZ_FORM_ID"),

    //业务数据物理表名
    TABLE_NAME("QF_TABLE_NAME"),

    //业务数据主键ID
    DATA_ID("QF_DATA_ID"),

    //待办摘要
    SUMMARY("QF_SUMMARY");


    private final String value;

    QfVarsProc(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
