package com.ksptool.bio.biz.qf.commons.qfe;

import com.ksptool.bio.biz.qf.commons.QfeVarsModel;
import lombok.Getter;
import lombok.Setter;
import org.dom4j.Element;
import org.dom4j.Namespace;

import java.util.List;

/**
 * 从 BPMN XML 中直接读取的用户任务原始属性，不经过 BpmnModel 转换；
 * 持有原始 Element 引用，支持 loadBatch/flush 双向同步
 */
@Getter
@Setter
public class QfeXmlUserTask {

    private static final Namespace NS_FLOWABLE = Namespace.get("http://flowable.org/bpmn");
    private static final Namespace NS_QFE = Namespace.get(QfeVarsModel.NS_URI);

    //原始XML元素
    private Element element;

    //任务 ID
    private String id;

    //任务名称
    private String name;

    //flowable:assignee 办理人ID
    private Long assignee;

    //flowable:assigneeKind 办理成员类型 0:指定用户 1:用户组 2:组织机构 3:发起人 10:任意人
    private Integer assigneeKind;

    //qfe:utAprKind 审批节点类型 0:固定人 1:发起时选人
    private Integer utAprKind;

    //qfe:utAprMemberKind 审批成员类型 0:指定用户 1:用户组 2:组织机构 3:发起人 10:任意人
    private Integer utAprMemberKind;

    //qfe:utAprMemberIds 人员IDS
    private List<Long> utAprMemberIds;

    //qfe:utAprMemberNames 人员名称S
    private List<String> utAprMemberNames;

    //qfe:utAprMi 多实例实现 0:无 1:会签 2:或签 3:自定义
    private Integer utAprMi;

    //qfe:utAprMiExpress 多实例自定义表达式
    private String utAprMiExpress;

    //qfe:utAprActions 允许的审批操作
    private List<Integer> utAprActions;

    //qfe:utAprActionNames 允许的审批操作名称
    private List<String> utAprActionNames;

    //qfe:utAprComment 是否允许填写审批意见 0:不写 1:要写
    private Integer utAprComment;

    //qfe:utFormAllowEditFields 节点绑定表单可编辑字段
    private List<Integer> utFormAllowEditFields;

    /**
     * 从 element 读取所有属性到当前对象字段；element 为 null 时直接返回
     */
    public void loadBatch() {
        if (element == null) {
            return;
        }

        var rw = QfeXmlRw.of(element);

        var flowable = QfeXmlRw.of(element, NS_FLOWABLE);

        var qfe = QfeXmlRw.of(element, NS_QFE);

        id = rw.getAttr("id");
        name = rw.getAttr("name");
        assignee = flowable.getNsAttrLong("assignee");
        assigneeKind = flowable.getNsAttrInt("assigneeKind");

        utAprKind = qfe.getNsAttrInt(QfeVarsModel.UT_APR_KIND.getValue());
        utAprMemberKind = qfe.getNsAttrInt(QfeVarsModel.UT_APR_MEMBER_KIND.getValue());
        utAprMemberIds = qfe.getNsArrayLong(QfeVarsModel.UT_APR_MEMBER_IDS.getValue());
        utAprMemberNames = qfe.getNsArrayString(QfeVarsModel.UT_APR_MEMBER_NAMES.getValue());
        utAprMi = qfe.getNsAttrInt(QfeVarsModel.UT_APR_MI.getValue());
        utAprMiExpress = qfe.getNsAttr(QfeVarsModel.UT_APR_MI_EXPRESS.getValue());
        utAprActions = qfe.getNsArrayInt(QfeVarsModel.UT_APR_ACTIONS.getValue());
        utAprActionNames = qfe.getNsArrayString(QfeVarsModel.UT_APR_ACTION_NAMES.getValue());
        utAprComment = qfe.getNsAttrInt(QfeVarsModel.UT_APR_COMMENT.getValue());
        utFormAllowEditFields = qfe.getNsArrayInt(QfeVarsModel.UT_FORM_ALLOW_EDIT_FIELDS.getValue());
    }

    /**
     * 将当前对象字段写回 element；element 为 null 时直接返回
     */
    public void flush() {
        if (element == null) {
            return;
        }

        var rw = QfeXmlRw.of(element);
        var flowable = QfeXmlRw.of(element, NS_FLOWABLE);
        var qfe = QfeXmlRw.of(element, NS_QFE);

        rw.setAttr("id", id);
        rw.setAttr("name", name);
        flowable.setNsAttrLong("assignee", assignee);
        flowable.setNsAttrInt("assigneeKind", assigneeKind);

        qfe.setNsAttrInt(QfeVarsModel.UT_APR_KIND.getValue(), utAprKind);
        qfe.setNsAttrInt(QfeVarsModel.UT_APR_MEMBER_KIND.getValue(), utAprMemberKind);
        qfe.setNsArrayLong(QfeVarsModel.UT_APR_MEMBER_IDS.getValue(), utAprMemberIds);
        qfe.setNsArrayString(QfeVarsModel.UT_APR_MEMBER_NAMES.getValue(), utAprMemberNames);
        qfe.setNsAttrInt(QfeVarsModel.UT_APR_MI.getValue(), utAprMi);
        qfe.setNsAttr(QfeVarsModel.UT_APR_MI_EXPRESS.getValue(), utAprMiExpress);
        qfe.setNsArrayInt(QfeVarsModel.UT_APR_ACTIONS.getValue(), utAprActions);
        qfe.setNsArrayString(QfeVarsModel.UT_APR_ACTION_NAMES.getValue(), utAprActionNames);
        qfe.setNsAttrInt(QfeVarsModel.UT_APR_COMMENT.getValue(), utAprComment);
        qfe.setNsArrayInt(QfeVarsModel.UT_FORM_ALLOW_EDIT_FIELDS.getValue(), utFormAllowEditFields);
    }

}
