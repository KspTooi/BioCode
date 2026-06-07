package com.ksptool.bio.biz.qf.service;

import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.qf.commons.qfe.QfeBpmnModel;
import com.ksptool.bio.biz.qf.commons.qfe.QfeUserTask.MemberKind;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 办理成员服务实现类
 * 这个服务主要用于获取办理成员ID和办理成员类型
 *
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-15
 */
@Slf4j
@Service
public class QfMemberServiceImpl implements QfMemberService {

    @Autowired
    private RepositoryService frpService;

    @Autowired
    private RuntimeService frService;

    @Autowired
    private TaskService ftService;

    @Autowired
    private UserGroupRepository augRepository;

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

        //不论是否多实例，先尝试从 collection 变量解析成员ID（匹配 List 类型流程变量 + loopCounter）
        var miResult = tryResolveFromCollection(task);
        if (miResult != null) {
            return miResult;
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

        //用户类型/发起人类型：优先从流程变量读取 qfAprNode_<taskDefKey>，兜底读 task.getAssignee()/initiator
        if (memberKind == MemberKind.USER || memberKind == MemberKind.INITIATOR) {
            var vars = ftService.getVariables(task.getId());
            if (vars != null) {
                var nodeKey = "qfAprNode_" + task.getTaskDefinitionKey();
                if (vars.containsKey(nodeKey)) {
                    // 发起时选人注入的是 Long，旧数据可能是 String，两者都要兼容
                    var assignee = parseElement(vars.get(nodeKey));
                    if (assignee != null) {
                        return assignee;
                    }
                }
            }
            // 兜底读引擎原生 task.assignee
            var member = task.getAssignee();

            if (StringUtils.isBlank(member)) {
                // 发起人类型(utAprMemberKind=3)兜底：从流程变量读取 initiator
                if (vars != null && vars.containsKey("initiator")) {
                    var initiator = vars.get("initiator");
                    if (initiator instanceof String s && StringUtils.isNotBlank(s) && NumberUtils.isCreatable(s)) {
                        return Long.parseLong(s);
                    }
                }
                return null;
            }

            var memberId = NumberUtils.toLong(member, 0L);

            if (memberId == 0L) {
                return null;
            }

            return memberId;
        }

        //用户组类型 和 组织机构类型：优先从流程变量读用户组ID（发起时选人场景 injectSingleInstanceApprover 已注入）
        if (memberKind == MemberKind.GROUP || memberKind == MemberKind.DEPT) {
            // QFE 优先：从流程变量读取节点级 qfAprGroup_<taskDefKey>，拿到的是用户组ID
            var vars = ftService.getVariables(task.getId());
            if (vars != null) {
                var nodeKey = "qfAprGroup_" + task.getTaskDefinitionKey();
                if (vars.containsKey(nodeKey)) {
                    // 注入的用户组ID为 Long，旧数据可能是 String，两者都要兼容
                    var groupId = parseElement(vars.get(nodeKey));
                    if (groupId != null) {
                        return groupId;
                    }
                }
            }
            // 兜底：从 IdentityLink 读取组ID（旧模型/非发起时选人场景）
            List<IdentityLink> links = ftService.getIdentityLinksForTask(task.getId());
            if (links == null || links.isEmpty()) {
                return null;
            }
            for (IdentityLink link : links) {
                if (!IdentityLinkType.CANDIDATE.equals(link.getType())) {
                    continue;
                }
                String groupId = link.getGroupId();
                if (StringUtils.isBlank(groupId) || !NumberUtils.isCreatable(groupId)) {
                    continue;
                }
                return Long.parseLong(groupId);
            }
            return null;
        }

        //任意人类型：已分配则返回实际办理人，未分配则返回 null（任何人可认领）
        if (memberKind == MemberKind.ANYONE) {
            // QFE 优先：从流程变量读取节点级 qfAprNode_<taskDefKey>（checkAndInjectNextApprover 注入的节点独立变量）
            var vars = ftService.getVariables(task.getId());
            if (vars != null) {
                var nodeKey = "qfAprNode_" + task.getTaskDefinitionKey();
                if (vars.containsKey(nodeKey)) {
                    // 发起时选人注入的是 Long，旧数据可能是 String，两者都要兼容
                    var assignee = parseElement(vars.get(nodeKey));
                    if (assignee != null) {
                        return assignee;
                    }
                }
            }
            // 兜底读引擎原生 task.assignee（可能为 BPMN XML 残留的 ${initiator} 解析值，不一定准确）
            String member = task.getAssignee();
            if (StringUtils.isNotBlank(member) && NumberUtils.isCreatable(member)) {
                return Long.parseLong(member);
            }
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
    public MemberKind getMemberKind(Task task) {

        //先获取流程模型
        var ut = new QfeBpmnModel().of(frpService.getBpmnModel(task.getProcessDefinitionId()))
                .getUserTask(task.getTaskDefinitionKey());

        if (ut == null) {
            return null;
        }

        return ut.getMemberKind();
    }

    /**
     * 根据人员ID获取人员所属组(角色)ID列表
     *
     * @param memberId 办理成员ID
     * @return 人员所属组(角色)ID列表
     */
    @Override
    public List<Long> getMemberGroupIds(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        return augRepository.getGroupIdsByGrantedUserId(memberId);
    }

    /**
     * 从流程变量中尝试通过 collection 解析多实例任务的成员ID
     * <p>
     * 不依赖 BpmnModel 的 loopCharacteristics（部署期重命名后可能丢失），
     * 直接扫描流程变量中的 List 集合，配合 loopCounter/elementVariable 定位当前实例的成员ID。
     *
     * @param task 任务
     * @return 成员ID 获取失败返回null
     */
    private Long tryResolveFromCollection(Task task) {
        var vars = ftService.getVariables(task.getId());
        if (vars == null) {
            return null;
        }

        // 优先匹配当前任务节点专属的 collection（qfMi_<taskDefKey> 约定名）
        String taskKey = task.getTaskDefinitionKey();
        String expectedVar = "qfMi_" + taskKey;
        if (vars.containsKey(expectedVar) && vars.get(expectedVar) instanceof List<?> expectedList
                && !expectedList.isEmpty()) {
            var first = expectedList.get(0);
            if (first instanceof String || first instanceof Number) {
                return resolveFromCollection(task, expectedVar, expectedList, vars);
            }
        }

        // 兜底扫描所有包含 String/Number 内容的 List 变量（兼容旧模型/非 QFE 场景）
        List<?> collection = null;
        String matchedName = null;
        for (var entry : vars.entrySet()) {
            if (entry.getValue() instanceof List<?> list && !list.isEmpty()) {
                var first = list.get(0);
                if (first instanceof String || first instanceof Number) {
                    collection = list;
                    matchedName = entry.getKey();
                    break;
                }
            }
        }

        if (collection == null) {
            return null;
        }


        return resolveFromCollection(task, matchedName, collection, vars);
    }

    /**
     * 从 collection 变量名推导 elementVariable 名
     * <p>
     * qfMi_xxx → assignee（QFE 多实例重命名后的默认 elementVariable）
     * assigneeList → assignee, groupList → group
     */
    private static String deriveElementVarName(String collectionName) {
        if (collectionName == null) {
            return "assignee";
        }
        if (collectionName.startsWith("qfMi_")) {
            return "assignee";
        }
        if (collectionName.endsWith("List") && collectionName.length() > 4) {
            return collectionName.substring(0, collectionName.length() - 4);
        }
        return "assignee";
    }

    /**
     * 将集合中的元素解析为 Long
     */
    private static Long parseElement(Object element) {
        if (element instanceof String s && StringUtils.isNotBlank(s) && NumberUtils.isCreatable(s)) {
            return Long.parseLong(s);
        }
        if (element instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    /**
     * 从多实例 collection 中解析当前任务的成员ID
     * <p>
     * 优先级：单元素集合直接返回 → elementVariable 读取 → loopCounter 索引读取
     *
     * @param task       当前任务
     * @param varName    collection 变量名（用于推导 elementVariable 名）
     * @param collection collection 内容
     * @param vars       当前任务可见的所有流程变量
     * @return 成员ID，解析失败返回 null
     */
    private Long resolveFromCollection(Task task, String varName, List<?> collection, Map<String, Object> vars) {
        // 单元素集合直接返回
        if (collection.size() == 1) {
            return parseElement(collection.get(0));
        }

        // 尝试通过 elementVariable 读取当前实例的值（Flowable 多实例循环时设置）
        String elementVar = deriveElementVarName(varName);
        if (elementVar != null && vars.containsKey(elementVar)) {
            var val = parseElement(vars.get(elementVar));
            if (val != null) {
                log.info("[MI] 通过{}读取到memberId={}", elementVar, val);
                return val;
            }
        }

        // 从任务变量读取 loopCounter（Flowable 多实例内置，0基索引），
        // 注意：loopCounter 有时不被 TaskService.getVariables() 返回，尝试从执行实例读取
        Object counterObj = vars.get("loopCounter");
        if (counterObj == null && task.getExecutionId() != null) {
            try {
                counterObj = frService.getVariable(task.getExecutionId(), "loopCounter");
            } catch (Exception e) {
            }
        }

        if (!(counterObj instanceof Number counter)) {
            return null;
        }

        int index = counter.intValue();
        if (index < 0 || index >= collection.size()) {
            return null;
        }

        return parseElement(collection.get(index));
    }

}