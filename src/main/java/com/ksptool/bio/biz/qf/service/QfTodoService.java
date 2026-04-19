package com.ksptool.bio.biz.qf.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qf.commons.QfMemberKinds;
import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.AddQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.ApproveQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.EditQfTodoDto;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetQfTodoListDto;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoDetailsVo;
import com.ksptool.bio.biz.qf.model.qftodo.vo.GetQfTodoListVo;
import com.ksptool.bio.biz.qf.repository.QfTodoRepository;
import com.ksptool.bio.biz.core.common.TupleMapper;
import jakarta.persistence.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
@Service
public class QfTodoService {

    @Autowired
    private QfTodoRepository repository;

    @Autowired
    private TaskService ftService;

    @Autowired
    private QfMemberService qfMemberService;

    @Autowired
    private IdentityService fiService;

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
        return as(po, GetQfTodoDetailsVo.class);
    }

    /**
     * 删除待办事项
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQfTodo(CommonIdDto dto) throws BizException {
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


        //判断是不是我的待办
        if (updatePo.getMemberType() == QfMemberKinds.USER.getValue()) {

            //如果待办是给用户的，判断我是不是这个用户
            if (updatePo.getMemberId() != uid) {
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
        vars.put("approved", dto.getAction() == 0 ? true : false);   // 用于排他网关走向 true/false 分支
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
            updatePo.setFinMemberName(aud.getNickname());
            updatePo.setFinTime(LocalDateTime.now());
            updatePo.setAction(dto.getAction());
            repository.save(updatePo);

        } finally {
            fiService.setAuthenticatedUserId(null);
        }

    }
}
