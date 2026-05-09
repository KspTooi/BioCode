package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.common.TupleMapper;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import com.ksptool.bio.biz.qf.commons.util.Flowable8NodeUtil;
import com.ksptool.bio.biz.qf.model.qfbizform.vo.GetQfBizFormDetailsVo;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.AddQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.ApproveQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.EditQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetQfTodoListDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.CancelQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoDetailsVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.ApproveFlowRecordVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoListVo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import jakarta.persistence.Tuple;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * 待办事项服务
 *
 * @author WangQingHua(603484930@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Slf4j
@Service
public class QfTodoService {

    @Autowired
    private QfTodoRepository repository;

    @Autowired
    private TaskService ftService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private QfMemberService qfMemberService;

    @Autowired
    private IdentityService fiService;

    @Autowired
    private QfBizFormService bizFormService;

    @Autowired
    private Flowable8NodeUtil flowable8NodeUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询待办事项列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetQfTodoListVo> getQfTodoList(GetQfTodoListDto dto) throws AuthException {
        QfTodoPo query = new QfTodoPo();
        assign(dto, query);

        var uid = session().getUserId();
        var gIds = qfMemberService.getMemberGroupIds(uid);

        //如果用户组ID列表为空，则设置为-1 防止Hibernate查询报错
        if (gIds == null || gIds.isEmpty()) {
            gIds = List.of(-1L);
        }

        Page<Tuple> page = repository.getQfTodoList(dto, uid, gIds, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQfTodoListVo> vos = TupleMapper.tupleAs(page.getContent(), GetQfTodoListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增待办事项
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQfTodo(AddQfTodoDto dto) {
        QfTodoPo insertPo = as(dto, QfTodoPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑待办事项
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQfTodo(EditQfTodoDto dto) throws BizException {
        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));
        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询待办事项详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetQfTodoDetailsVo getQfTodoDetails(CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        CommonIdDto commonIdDto = new CommonIdDto();
        commonIdDto.setId(po.getBizFormId());
        GetQfBizFormDetailsVo getQfBizFormDetailsVo = bizFormService.getBizFormDetails(commonIdDto);
        GetQfTodoDetailsVo details = as(po, GetQfTodoDetailsVo.class);
        details.setRouteMobile(getQfBizFormDetailsVo.getRouteMobile());
        details.setRoutePc(getQfBizFormDetailsVo.getRoutePc());
        return details;
    }

    /**
     * 取消/作废待办事项
     *
     * @param dto 取消条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelQfTodo(CancelQfTodoDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId()).orElse(null);
        if (po == null) {
            throw new BizException("待办不存在或无权限访问.");
        }
        if (po.getStatus() != 0) {
            throw new BizException("只能取消待办状态的记录");
        }
        String engTaskId = po.getEngTaskId();
        String engProcId = po.getEngProcId();
        if (StringUtils.isNotBlank(engTaskId)) {
            try {
                var task = ftService.createTaskQuery().taskId(engTaskId).singleResult();
                if (task != null) {
                    ftService.deleteTask(engTaskId);
                }
            } catch (Exception e) {
                log.error("[cancelQfTodo] 删除Flowable任务失败, taskId: {}", engTaskId, e);
            }
        }
        if (StringUtils.isNotBlank(engProcId)) {
            try {
                runtimeService.deleteProcessInstance(engProcId, dto.getReason());
            } catch (Exception e) {
                log.error("[cancelQfTodo] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
            }
        }
        po.setStatus(10);
        po.setComment(dto.getReason());
        repository.save(po);
    }

    /**
     * 根据表名和数据ID批量取消待办事项
     *
     * @param tableName 表名
     * @param dataId   数据ID
     * @param reason   取消原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelQfTodoByData(String tableName, Long dataId, String reason) {
        List<QfTodoPo> todoList = repository.findByTableNameAndDataId(tableName, dataId);
        for (QfTodoPo po : todoList) {
            if (po.getStatus() != 0) {
                continue;
            }
            String engTaskId = po.getEngTaskId();
            String engProcId = po.getEngProcId();
            if (StringUtils.isNotBlank(engTaskId)) {
                try {
                    var task = ftService.createTaskQuery().taskId(engTaskId).singleResult();
                    if (task != null) {
                        ftService.deleteTask(engTaskId);
                    }
                } catch (Exception e) {
                    log.error("[cancelQfTodoByData] 删除Flowable任务失败, taskId: {}", engTaskId, e);
                }
            }
            if (StringUtils.isNotBlank(engProcId)) {
                try {
                    runtimeService.deleteProcessInstance(engProcId, reason);
                } catch (Exception e) {
                    log.error("[cancelQfTodoByData] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
                }
            }
            po.setStatus(10);
            po.setComment(reason);
            repository.save(po);
        }
    }

    /**
     * 删除待办事项
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfTodo(CommonIdDto dto) throws BizException {
        List<Long> ids = dto.isBatch() ? dto.getIds() : List.of(dto.getId());
        for (Long id : ids) {
            QfTodoPo po = repository.findById(id).orElse(null);
            if (po == null) {
                continue;
            }
            if (po.getStatus() != 0) {
                throw new BizException("只能删除待办状态的记录");
            }
            String engTaskId = po.getEngTaskId();
            String engProcId = po.getEngProcId();
            if (StringUtils.isNotBlank(engTaskId)) {
                try {
                    var task = ftService.createTaskQuery().taskId(engTaskId).singleResult();
                    if (task != null) {
                        ftService.deleteTask(engTaskId);
                    }
                } catch (Exception e) {
                    log.error("[removeQfTodo] 删除Flowable任务失败, taskId: {}", engTaskId, e);
                }
            }
            if (StringUtils.isNotBlank(engProcId)) {
                try {
                    runtimeService.deleteProcessInstance(engProcId, "待办已删除");
                } catch (Exception e) {
                    log.error("[removeQfTodo] 终止Flowable流程实例失败, processInstanceId: {}", engProcId, e);
                }
            }
        }
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 审批待办事项
     *
     * @param dto 审批条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveQfTodo(ApproveQfTodoDto dto) throws Exception {

        QfTodoPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("审批失败,数据不存在或无权限访问."));

        if (updatePo.getStatus() != 0) {
            throw new BizException("待办状态异常，无法审批.");
        }

        var aud = session();
        var uid = aud.getUserId();
        var userPo = userRepository.findById(uid);

        //判断是不是我的待办
        if (updatePo.getMemberType() == QfMemberKinds.USER.getValue()) {

            //如果待办是给用户的，判断我是不是这个用户
            if (!Objects.equals(updatePo.getMemberId(), uid)) {
                throw new BizException("该待办属于用户:" + updatePo.getMemberId() + "，审批人不是本人，无法审批.");
            }

        }

        //如果待办是给用户组的，判断我是不是这个用户组的一员
        if (updatePo.getMemberType() == QfMemberKinds.GROUP.getValue()) {
            var groupIds = qfMemberService.getMemberGroupIds(updatePo.getMemberId());
            if (!groupIds.contains(uid)) {
                throw new BizException("该待办属于用户组:" + updatePo.getMemberId() + "，审批人不是该用户组的一员，无法审批.");
            }

        }

        //先获取任务
        var task = ftService.createTaskQuery()
                .taskId(updatePo.getEngTaskId())
                .singleResult();

        if (task == null) {
            throw new BizException("引擎故障，无法获取任务信息.");
        }

        var comment = "";

        if (StringUtils.isNotBlank(dto.getComment())) {
            comment = dto.getComment();
        }

        var vars = new HashMap<String, Object>();
        vars.put("approved", dto.getAction() == 0);   // 用于排他网关走向 true/false 分支
        vars.put("comment", comment);     // 审批意见

        //设置审批人(为了让 Flowable 在历史表 ACT_HI_TASKINST 记录"谁办的)
        fiService.setAuthenticatedUserId(String.valueOf(uid));

        try {

            //这为了走 Flowable 自己的评论体系（历史记录会看到），在 complete 之前加：
            ftService.addComment(task.getId(), task.getProcessInstanceId(), comment);

            //审批任务
            ftService.complete(task.getId(), vars);

            //更新待办状态为已办
            updatePo.setStatus(1);

            //更新实际办理人
            updatePo.setFinMemberId(uid);
            //todo 获取用户昵称 目前临时从core域获取，以后还是会从auth域session中获取
            updatePo.setFinMemberName(userPo.get().getNickname());
            updatePo.setFinTime(LocalDateTime.now());
            updatePo.setAction(dto.getAction());
            updatePo.setComment(comment);
            repository.save(updatePo);

        } finally {
            fiService.setAuthenticatedUserId(null);
        }

    }

    public String getQfTodoApproveFlow(@Valid CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询失败,数据不存在或无权限访问."));
        return flowable8NodeUtil.generateColorBpmnXml(po.getEngProcId());
    }

    /**
     * 获取待办事项流程流转记录
     * 按时间顺序返回：节点名称、节点审批人、节点审批时间、节点审批结果
     *
     * @param dto 查询条件
     * @return 流转记录列表
     * @throws BizException 业务异常
     */
    public List<ApproveFlowRecordVo> getQfTodoApproveFlowRecord(CommonIdDto dto) throws BizException {
        QfTodoPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询失败,数据不存在或无权限访问."));

        // 通过流程ID查询该流程所有待办，即为流转记录
        List<QfTodoPo> todoList = repository.findAllByEngProcIdOrderByCreateTimeAsc(po.getEngProcId());

        //获取所有人的用户信息
        //获取代办的信息
        List<QfTodoPo> waitingList = todoList.stream().filter(todo -> (todo.getStatus() == 0 || todo.getStatus() == 10)).toList();
        Map<Long, UserPo> userMap;
        if(!waitingList.isEmpty()) {
            List<UserPo> userList = userRepository.findAllById(waitingList.stream().map(QfTodoPo::getMemberId).toList());
            userMap = userList.stream().collect(Collectors.toMap(UserPo::getId, user -> user));
        } else {
            userMap = new HashMap<>();
        }
        return todoList.stream().map(todo -> {
            ApproveFlowRecordVo vo = new ApproveFlowRecordVo();
            vo.setNodeName(todo.getNodeName());
            vo.setFinMemberName(StringUtils.isNoneBlank(todo.getFinMemberName()) ? todo.getFinMemberName() : userMap.get(todo.getMemberId()).getNickname());
            vo.setFinTime(todo.getFinTime());
            vo.setAction(todo.getAction());
            vo.setComment(todo.getComment());
            vo.setStatus(todo.getStatus());
            return vo;
        }).toList();
    }
}
