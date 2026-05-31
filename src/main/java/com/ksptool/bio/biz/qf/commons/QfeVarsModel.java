package com.ksptool.bio.biz.qf.commons;


import lombok.Getter;

/**
 * 前端QFD设计器自定义命名空间的属性
 * <p>
 * 这些自定义属性不会被注入到Proc变量中，如果需要获取这些值，应该通过静态的Bpmn模型来获取
 * <p>
 * 这些自定义属性全部使用命名空间qfe (quick_flow_extstion)
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 1.7.1(A).1
 */
@Getter
public enum QfeVarsModel {

    //审批节点类型 0:固定人 1:发起时选人
    UT_APR_KIND("utAprKind"),

    //审批成员类型 0:指定人 1:组 2:组织机构 3:发起人 10:任意人
    UT_APR_MEMBER_KIND("utAprMemberKind"),

    //人员IDS
    UT_APR_MEMBER_IDS("utAprMemberIds"),
    
    //人员名称S
    UT_APR_MEMBER_NAMES("utAprMemberNames"),

    //多实例实现 0:无 1:会签 2:或签 3:自定义
    UT_APR_MI("utAprMi"),

    //多实例表达式
    UT_APR_MI_EXPRESS("utAprMiExpress"),

    //允许的审批操作 0:同意 1:驳回 2:转交 3:驳回节点
    UT_APR_ACTIONS("utAprActions"),

    //允许的审批操作名称
    UT_APR_ACTION_NAMES("utAprActionNames"),

    //是否允许填写审批意见 0:不写 1:要写
    UT_APR_COMMENT("utAprComment"),

    //绑定表单可编辑字段
    UT_FORM_ALLOW_EDIT_FIELDS("utFormAllowEditFields");

    /** BPMN 扩展命名空间 URI，与前端 qfeDescriptor.json 一致 */
    public static final String NS_URI = "quick_flow_extstion";

    /** BPMN 扩展命名空间前缀 */
    public static final String NS_PREFIX = "qfe";

    private final String value;

    QfeVarsModel(String value) {
        this.value = value;
    }
}
