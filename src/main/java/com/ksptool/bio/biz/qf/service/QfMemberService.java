package com.ksptool.bio.biz.qf.service;

import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * 办理成员服务接口
 * 这个服务主要用于获取办理成员ID和办理成员类型
 * <p>
 * 你们可以在你们自己的域里面重写这个服务，用来把QF默认的人员服务替换掉。(比如你自己的域里面有组织机构，你就可以重写这个服务，用来获取组织机构ID)
 * 但请注意，你们重写这个服务的时候，别修改QF的任何代码，只是重写这个服务。
 * <p>
 *
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-15
 */
public interface QfMemberService {

    /**
     * 根据任务ID获取办理成员ID
     *
     * @param task 任务
     * @return 办理成员ID
     */
    Long getMemberId(Task task);

    /**
     * 根据任务ID获取办理成员类型
     *
     * @param task 任务
     * @return 办理成员类型
     */
    MemberKind getMemberKind(Task task);


    /**
     * 根据人员ID获取人员所属组(角色)ID列表
     *
     * @param memberId 办理成员ID
     * @return 人员所属组(角色)ID列表
     */
    List<Long> getMemberGroupIds(Long memberId);

}
