package com.ksptool.bio.biz.qf.commons;

/**
 * QF域约定的流程变量 Key
 * <p>
 * 发起流程时通过这些 Key 往流程实例注入业务数据与发起人信息,
 * 监听器 (如 QfTaskCreatedListener) 在 TASK_CREATED 回调中按相同 Key 读取,
 * 用于脱离Web会话上下文的情况下还原 rootId/deptId/initiator 等字段。
 * <p>
 * 统一以 "QF_" 前缀避免与业务自定义流程变量冲突。
 */
public final class QfProcVars {

    /**
     * --------------------------------发起方数据--------------------------------
     */
    //所属企业/租户ID(这里是发起人的所属企业/租户ID)
    public static final String ROOT_ID = "QF_ROOT_ID";

    //所属部门ID (这里是发起人的所属部门ID)
    public static final String DEPT_ID = "QF_DEPT_ID";

    //发起人用户ID
    public static final String INITIATOR_ID = "QF_INITIATOR_ID";

    //发起人显示名
    public static final String INITIATOR_NAME = "QF_INITIATOR_NAME";

    //发起时间 (这里是发起流程的时间)
    public static final String INITIATOR_TIME = "QF_INITIATOR_TIME";

    /**
     * --------------------------------业务数据--------------------------------
     */
    //业务表单ID (qf_biz_form.id)
    public static final String BIZ_FORM_ID = "QF_BIZ_FORM_ID";

    //业务数据物理表名
    public static final String TABLE_NAME = "QF_TABLE_NAME";

    //业务数据主键ID
    public static final String DATA_ID = "QF_DATA_ID";

    //待办摘要
    public static final String SUMMARY = "QF_SUMMARY";


    private QfProcVars() {

    }
}
