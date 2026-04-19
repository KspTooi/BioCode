package com.ksptool.bio.biz.qf.service;

import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import org.flowable.task.api.Task;

/**
 * 办理成员服务接口
 * 这个服务主要用于获取办理成员ID和办理成员类型
 * 
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @since 2026-04-15
 */
public interface QfMemberService {

    /**
     * 根据任务ID获取办理成员ID
     *
     * @param task 任务
     * @return 办理成员ID
     */
    public Long getMemberId(Task task);

    /**
     * 根据任务ID获取办理成员类型
     *
     * @param task 任务
     * @return 办理成员类型
     */
    public QfMemberKinds getMemberKind(Task task);

}
