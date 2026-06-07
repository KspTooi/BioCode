package com.ksptool.bio.biz.qf.commons.qfe;

import com.ksptool.bio.biz.qf.commons.QfeVarsModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.UserTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * QfeUserTask 包装类，用于包装 UserTask 对象，提供更方便的访问和操作，也提供QFE的扩展属性访问和操作
 */
public class QfeUserTask {

    private UserTask userTask;

    /**
     * 从 UserTask 包装为 QfeUserTask
     *
     * @param userTask UserTask
     * @return QfeUserTask
     */
    public static QfeUserTask of(UserTask userTask) {
        var t = new QfeUserTask();
        t.userTask = userTask;
        return t;
    }

    /**
     * 获取审批节点类型
     *
     * @return 审批节点类型
     */
    public AprKind getApproveKind() {
        var attr = getAttr(QfeVarsModel.UT_APR_KIND);
        if (StringUtils.isBlank(attr)) {
            return null;
        }
        for (var kind : AprKind.values()) {
            if (String.valueOf(kind.value).equals(attr)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * 获取审批成员类型
     *
     * @return 审批成员类型，未配置时返回 null
     */
    public MemberKind getMemberKind() {
        var attr = getAttr(QfeVarsModel.UT_APR_MEMBER_KIND);
        if (StringUtils.isBlank(attr)) {
            return null;
        }
        for (var kind : MemberKind.values()) {
            if (String.valueOf(kind.value).equals(attr)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * 是否是多实例任务
     *
     * @return 是否是多实例任务
     */
    public boolean isMultiInstance() {
        var k = getMultiInstanceKind();
        return k != null && k != AprMi.NONE;
    }

    /**
     * 是否允许发起时跳过
     *
     * @return 是否允许发起时跳过
     */
    public boolean isInitSkip() {
        var attr = getAttr(QfeVarsModel.UT_GE_INIT_SKIP);
        if (StringUtils.isBlank(attr)) {
            return false;
        }
        
        return attr.equals("1");
    }


    /**
     * 获取多实例实现类型
     *
     * @return 多实例实现类型，未配置时返回 null
     */
    public AprMi getMultiInstanceKind() {
        var attr = getAttr(QfeVarsModel.UT_APR_MI);
        if (StringUtils.isBlank(attr)) {
            return null;
        }
        for (var mi : AprMi.values()) {
            if (String.valueOf(mi.value).equals(attr)) {
                return mi;
            }
        }
        return null;
    }

    /**
     * 获取办理成员ID列表
     *
     * @return 办理成员ID列表
     */
    public List<Long> getMemberIds() {
        try {
            var attr = getAttr(QfeVarsModel.UT_APR_MEMBER_IDS);
            if (StringUtils.isBlank(attr)) {
                return Collections.emptyList();
            }
            return Arrays.stream(attr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::parseLong)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取允许的审批操作列表；utAprActions 为逗号分隔整数（0:同意 1:驳回 2:转交 3:驳回节点）
     *
     * @return 审批操作 value 列表，未配置或解析失败时返回空列表
     */
    public List<Integer> getActions() {
        try {
            var attr = getAttr(QfeVarsModel.UT_APR_ACTIONS);
            if (StringUtils.isBlank(attr)) {
                return Collections.emptyList();
            }
            return Arrays.stream(attr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Integer::parseInt)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取允许的审批操作显示名列表；与 getActions() 按下标一一对应
     *
     * @return 操作名列表，未配置或解析失败时返回空列表
     */
    public List<String> getActionNames() {
        var attr = getAttr(QfeVarsModel.UT_APR_ACTION_NAMES);
        if (StringUtils.isBlank(attr)) {
            return Collections.emptyList();
        }
        return Arrays.stream(attr.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    /**
     * 获取节点绑定表单的可编辑字段名列表（utFormAllowEditFields，逗号分隔）
     *
     * @return 字段名列表，未配置时返回空列表
     */
    public List<String> getFormAllowEditFields() {
        var attr = getAttr(QfeVarsModel.UT_FORM_ALLOW_EDIT_FIELDS);
        if (StringUtils.isBlank(attr)) {
            return Collections.emptyList();
        }
        return Arrays.stream(attr.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    /**
     * 获取多实例自定义表达式；仅 utAprMi=3（CUSTOM）时有效
     *
     * @return 表达式字符串，未配置时返回 null
     */
    public String getMultiInstanceExpress() {
        return getAttr(QfeVarsModel.UT_APR_MI_EXPRESS);
    }

    /**
     * 节点ID
     *
     * @return BPMN UserTask 的 id
     */
    public String getId() {
        return userTask.getId();
    }

    /**
     * 节点名称
     *
     * @return BPMN UserTask 的 name
     */
    public String getName() {
        return userTask.getName();
    }

    /**
     * 是否为"发起时选人"节点（utAprKind=1）
     *
     * @return true=发起时由发起人选择办理人
     */
    public boolean isInitSelected() {
        return getApproveKind() == AprKind.INIT_SELECTED;
    }

    /**
     * 校验给定ID是否落在本节点配置的成员范围（utAprMemberIds）内
     * <p>
     * 发起时选人节点的 memberIds 表示"允许选择的范围"，此处仅用于校验发起人所选是否越界；
     * 范围未配置时视为不限制，返回 true。
     *
     * @param id 待校验的用户ID或用户组ID
     * @return true=在允许范围内
     */
    public boolean isInMemberScope(long id) {
        var ids = getMemberIds();
        if (ids.isEmpty()) {
            return true;
        }
        return ids.contains(id);
    }

    /**
     * 获取包装后的 UserTask
     *
     * @return UserTask
     */
    public UserTask getUserTask() {
        return userTask;
    }

    /**
     * 获取 QFE 扩展属性值
     *
     * @param attr QFE 扩展属性枚举
     * @return 属性值
     */
    public String getAttr(QfeVarsModel attr) {
        return userTask.getAttributeValue(QfeVarsModel.NS_URI, attr.getValue());
    }

    /**
     * QFE 审批节点类型（utAprKind）
     */
    @Getter
    public enum AprKind {

        /**
         * 标准
         */
        STANDARD(0),

        /**
         * 发起时选人
         */
        INIT_SELECTED(1);

        private final int value;

        AprKind(int value) {
            this.value = value;
        }
    }

    /**
     * QFE 审批成员类型（utAprMemberKind）
     */
    @Getter
    public enum MemberKind {

        /**
         * 指定人
         */
        USER(0),

        /**
         * 用户组
         */
        GROUP(1),

        /**
         * 组织机构（部门）
         */
        DEPT(2),

        /**
         * 发起人
         */
        INITIATOR(3),

        /**
         * 任意人
         */
        ANYONE(10);

        private final int value;

        MemberKind(int value) {
            this.value = value;
        }

    }

    /**
     * QFE 审批操作（utAprActions）
     */
    @Getter
    public enum AprAction {

        /**
         * 同意
         */
        AGREE(0),

        /**
         * 驳回
         */
        REJECT(1),

        /**
         * 转交
         */
        TRANSFER(2),

        /**
         * 驳回到节点
         */
        REJECT_TO_NODE(3);

        private final int value;

        AprAction(int value) {
            this.value = value;
        }
    }


    /**
     * QFE 多实例实现类型（utAprMi）
     */
    @Getter
    public enum AprMi {

        /**
         * 无多实例（单人）
         */
        NONE(0),

        /**
         * 会签（所有人同意才通过）
         */
        ALL_SIGN(1),

        /**
         * 或签（任意一人同意即通过）
         */
        ANY_SIGN(2),

        /**
         * 自定义表达式
         */
        CUSTOM(3);

        private final int value;

        AprMi(int value) {
            this.value = value;
        }
    }
    

}
