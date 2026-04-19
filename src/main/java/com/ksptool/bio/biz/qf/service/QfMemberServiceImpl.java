package com.ksptool.bio.biz.qf.service;

import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import com.ksptool.bio.biz.qf.commons.QfModelTools;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 办理成员服务实现类
 * 这个服务主要用于获取办理成员ID和办理成员类型
 * 
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @since 2026-04-15
 * @license Apache License 2.0
 */
@Slf4j
@Service
public class QfMemberServiceImpl implements QfMemberService {

    @Autowired
    private RepositoryService frpService;

    @Autowired
    private TaskService ftService;

    /**
     * 根据任务ID获取办理成员ID
     *
     * @param task 任务
     * @return 办理成员ID 获取失败返回null
     */
    @Override
    public Long getMemberId(Task task) {

        //先获取流程模型
        var model = frpService.getBpmnModel(task.getProcessDefinitionId());

        if (model == null) {
            return null;
        }

        //获取前端在BPMN中配置的办理人类型
        var memberKind = this.getMemberKind(task);
        
        if (memberKind == null) {
            return null;
        }

        /*
         * #2026-04-15 @KspTool
         * 这里注意 QF是不支持单实例任务+多个办理人情况，如果有会签/或签，则该任务一定是多实例任务。
         * <p>
         * 我已经让前端把单实例能选多个人的框干掉了。后端应该也会在模型部署时不让这种模型上线(!现在还没有做@KspTool)
         * 如果前端正确配出了多实例，这里会是多个任务，每个任务就一个人。
         * 所以这里我就不管这种情况，永远只拿第一个人。
         * <p>
         * 
         * 但在这之前，我还要说明一下，在标准的Flowable里面，优先级是靠猜的(也就是这个任务到底是一个用户在办还是几个用户在办还是用户组在办)
         * 也就是先拿assignee(拿到说明是一个人在办)
         * assignee拿不到就拿candidateUsers(拿到了说明是多个人在办 #前端已经干掉了这个选项)，
         * candidateUsers拿不到就拿candidateGroups(说明了是用户组在办)
         * 
         * 但现在前端在BPMN模型里面吐了assigneeKind 所以可以精准的拿到办理人是谁。(因为未来还可能会有恶心的组织机构，所以原生的flowable搞不定这里)
         */

        //用户类型
        if(memberKind == QfMemberKinds.USER){
            var member = task.getAssignee();

            //如果assignee为空，说明前端设计的模型有问题
            if(StringUtils.isBlank(member)){
                return null;
            }

            var memberId = NumberUtils.toLong(member, 0L);

            if(memberId == 0L){
                return null;
            }

            return memberId;
        }

        //用户组类型
        if(memberKind == QfMemberKinds.GROUP){

            List<IdentityLink> links = ftService.getIdentityLinksForTask(task.getId());

            if (links == null || links.isEmpty()) {
                return null;
            }

            for (IdentityLink link : links) {

                // 只关心"候选组"类型 (CANDIDATE)
                if (!IdentityLinkType.CANDIDATE.equals(link.getType())) {
                    continue;
                }
                String groupId = link.getGroupId();

                if (StringUtils.isBlank(groupId) || !NumberUtils.isCreatable(groupId)) {
                    continue;
                }

                // 多实例展开后一个子 Task 也只会有一个 groupId
                return Long.parseLong(groupId);
            }

            //拿不到组ID，说明前端设计的模型有问题
            return null;
        }

        //如果以上都没有拿到，说明前端设计的模型有问题
        return null;
    }

    /**
     * 根据任务ID获取办理成员类型
     *
     * @param task 任务
     * @return 办理成员类型
     */
    @Override
    public QfMemberKinds getMemberKind(Task task) {

        //先获取流程模型
        var model = frpService.getBpmnModel(task.getProcessDefinitionId());

        if (model == null) {
            return null;
        }

        return QfModelTools.resolveMemberKind(model, task.getTaskDefinitionKey());
    }

}
