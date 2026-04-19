package com.ksptool.bio.biz.qf.commons;

import lombok.Getter;

/**
 * 前端设计器 (flowable-designer) 扩展的 BPMN 自定义属性常量
 * <p>
 * 请注意: 这些自定义属性不会被注入到Proc变量中，如果需要获取这些值，应该通过静态的Bpmn模型来获取
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-17
 * @license Apache License 2.0
 */
@Getter
public enum QfVarsModel {

    //办理人类型 user:用户 dept:组织机构 group:用户组 initiator:发起人
    ASSIGNEE_KIND("assigneeKind"),

    //选中的用户姓名回显（逗号分隔）
    CANDIDATE_USER_NAMES("candidateUserNames"),

    //选中的部门名称回显（逗号分隔）
    CANDIDATE_DEPT_NAMES("candidateDeptNames"),

    //选中的用户组名称回显（逗号分隔）
    CANDIDATE_GROUP_NAMES("candidateGroupNames");

    private final String value;

    QfVarsModel(String value) {
        this.value = value;
    }
}
