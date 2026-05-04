package com.ksptool.bio.biz.qt.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.qt.common.LocalBeanExecutionJob;
import com.ksptool.bio.biz.qt.common.QuickTask;
import com.ksptool.bio.biz.qt.common.QuickTaskRegistry;
import com.ksptool.bio.biz.qt.model.qttask.QtTaskPo;
import com.ksptool.bio.biz.qt.model.qttask.dto.*;
import com.ksptool.bio.biz.qt.model.qttask.vo.ExportQtTaskVo;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetLocalBeanListVo;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetQtTaskDetailsVo;
import com.ksptool.bio.biz.qt.model.qttask.vo.GetQtTaskListVo;
import com.ksptool.bio.biz.qt.model.qttaskgroup.QtTaskGroupPo;
import com.ksptool.bio.biz.qt.repository.QtTaskGroupRepository;
import com.ksptool.bio.biz.qt.repository.QtTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * QF任务服务
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
@Service
@Slf4j
public class QtTaskService {

    @Autowired
    private QtTaskRepository repository;

    @Autowired
    private QtTaskGroupRepository groupRepository;

    @Autowired
    private Scheduler scheduler;

    /**
     * 获取任务列表
     *
     * @param dto 查询条件
     * @return 任务列表
     */
    public PageResult<GetQtTaskListVo> getQtTaskList(GetQtTaskListDto dto) {
        QtTaskPo query = new QtTaskPo();
        assign(dto, query);

        Page<QtTaskPo> page = repository.getQtTaskList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetQtTaskListVo> vos = as(page.getContent(), GetQtTaskListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增任务
     *
     * @param dto 新增任务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addQtTask(AddQtTaskDto dto) throws BizException {
        QtTaskPo insertPo = as(dto, QtTaskPo.class);

        //验证任务名称是否被占用
        Long count = repository.countByName(dto.getName());
        if (count > 0) {
            throw new BizException("任务名称已存在:[" + dto.getName() + "]");
        }

        //如果配置了分组 需要处理分组信息
        if (dto.getGroupId() != null) {
            QtTaskGroupPo groupPo = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new BizException("任务分组不存在:[" + dto.getGroupId() + "]"));
            insertPo.setGroupName(groupPo.getName());
        }

        //验证任务信息
        String validateResult = insertPo.validate();

        if (validateResult != null) {
            throw new BizException(validateResult);
        }

        //保存任务信息
        repository.save(insertPo);

        //更新Quartz Job
        updateQuartzJob(insertPo);
    }

    /**
     * 编辑任务
     *
     * @param dto 编辑任务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQtTask(EditQtTaskDto dto) throws BizException {
        QtTaskPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);

        //验证任务名称是否被占用
        Long count = repository.countByNameExcludeId(dto.getName(), updatePo.getId());
        if (count > 0) {
            throw new BizException("任务名称已存在:[" + dto.getName() + "]");
        }

        //如果配置了分组 需要处理分组信息
        if (dto.getGroupId() != null) {
            QtTaskGroupPo groupPo = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new BizException("任务分组不存在:[" + dto.getGroupId() + "]"));
            updatePo.setGroupName(groupPo.getName());
        }

        //处理清除分组信息
        if (dto.getGroupId() == null) {
            updatePo.setGroupName(null);
        }

        //验证任务信息
        String validateResult = updatePo.validate();

        if (validateResult != null) {
            throw new BizException(validateResult);
        }

        //保存任务信息
        repository.save(updatePo);

        //更新Quartz Job
        updateQuartzJob(updatePo);
    }

    /**
     * 获取任务详情
     *
     * @param dto 获取任务详情条件
     * @return 任务详情
     */
    public GetQtTaskDetailsVo getQtTaskDetails(CommonIdDto dto) throws BizException {
        QtTaskPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetQtTaskDetailsVo.class);
    }

    /**
     * 删除任务
     *
     * @param dto 删除任务条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeQtTask(CommonIdDto dto) throws BizException {

        var ids = dto.toIds();
        repository.deleteAllById(ids);

        for (Long id : ids) {
            abortTask(id);
        }

    }

    /**
     * 获取本地任务Bean列表
     *
     * @return 本地任务Bean列表
     */
    public List<GetLocalBeanListVo> getLocalBeanList() {

        List<GetLocalBeanListVo> vos = new ArrayList<>();
        for (QuickTask<?> bean : QuickTaskRegistry.getBeans()) {
            GetLocalBeanListVo vo = new GetLocalBeanListVo();
            vo.setName(bean.getClass().getSimpleName());
            vo.setFullClassName(bean.getClass().getName());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 立即执行任务
     *
     * @param dto 执行任务条件
     */
    public void executeTask(ExecuteTaskDto dto) throws BizException {
        var taskPo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("执行任务失败,数据不存在或无权限访问."));

        //如果任务状态为暂停 则抛出异常
        if (taskPo.getStatus() == 1) {
            throw new BizException("任务已暂停,请先启用任务.");
        }

        var jobKey = new JobKey(taskPo.getIdentity());

        //准备任务数据
        var jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskPo.getId());
        jobDataMap.put("target", taskPo.getTarget());
        jobDataMap.put("params", taskPo.getTargetParam());
        jobDataMap.put("policyError", taskPo.getPolicyError());
        jobDataMap.put("policyRcd", taskPo.getPolicyRcd());

        //立即执行任务
        try {
            scheduler.triggerJob(jobKey, jobDataMap);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new BizException("处理QuartZ任务时发生了一个错误,请联系管理员.");
        }

    }


    /**
     * 更新Quartz Job
     *
     * @param po 任务信息
     */
    public void updateQuartzJob(QtTaskPo po) throws BizException {

        //构建JobDetail
        JobDetail jobDetail = JobBuilder.newJob(LocalBeanExecutionJob.class)
                .withIdentity(po.getIdentity())
                .usingJobData("taskId", po.getId())
                .usingJobData("target", po.getTarget()) // Bean Name
                .usingJobData("params", po.getTargetParam()) // JSON
                .usingJobData("policyError", po.getPolicyError()) // 失败策略 0:默认 1:自动暂停
                .usingJobData("policyRcd", po.getPolicyRcd()) // 日志策略 0:全部 1:仅异常 2:不记录
                .build();

        //构建Trigger
        var cronSchedule = CronScheduleBuilder.cronSchedule(po.getCron());

        //处理过期策略 0:放弃执行等待下一次(不补跑)
        if (po.getPolicyMisfire() == 0) {
            cronSchedule.withMisfireHandlingInstructionDoNothing();
        }

        //1:立即执行(仅一次)
        if (po.getPolicyMisfire() == 1) {
            cronSchedule.withMisfireHandlingInstructionFireAndProceed();
        }

        //2:全部执行(补跑所有)
        if (po.getPolicyMisfire() == 2) {
            cronSchedule.withMisfireHandlingInstructionIgnoreMisfires();
        }

        var triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(po.getIdentity())
                .withSchedule(cronSchedule);

        //处理有效期截止
        if (po.getExpireTime() != null) {
            triggerBuilder.endAt(Date.from(po.getExpireTime().atZone(ZoneId.systemDefault()).toInstant()));
        }

        try {

            //如果任务存在则覆盖
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.deleteJob(jobDetail.getKey());
            }

            //只有状态为“正常”才调度，暂停的不调度
            if (po.getStatus() == 0) {
                scheduler.scheduleJob(jobDetail, triggerBuilder.build());
            }

        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new BizException("处理QuartZ任务时发生了一个错误,请联系管理员.");
        }

    }

    /**
     * 终止任务
     *
     * @param id 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void abortTask(Long id) {
        abortTask(id, false);
    }

    /**
     * 终止任务
     *
     * @param id 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void abortTask(Long id, boolean hasError) {

        var taskPo = repository.findById(id).orElse(null);

        //如果非空 将任务更改为暂停状态
        if (taskPo != null) {
            taskPo.setStatus(1);

            //如果出现异常 则设置为异常状态
            if (hasError) {
                taskPo.setStatus(2);
            }

            repository.save(taskPo);
        }

        String jobName = "TASK_" + id;
        JobKey jobKey = new JobKey(jobName);

        //安全删除 Quartz Job
        try {
            //先检查是否存在，避免 Quartz 内部抛出 NoRecordFoundException
            if (scheduler.checkExists(jobKey)) {
                //暂不支持任务中断  !!可以尝试中断正在运行的线程(需要 Job 实现 InterruptableJob)
                //scheduler.interrupt(jobKey);
                //删除任务（这会自动删除关联的 Trigger）
                boolean deleted = scheduler.deleteJob(jobKey);
                log.info("Quartz任务 ID: {} 删除结果: {}", id, deleted);
            } else {
                log.warn("Quartz任务 ID: {} 在调度器中不存在，无需删除", id);
            }
        } catch (SchedulerException e) {
            //这里的异常通常是数据库连接问题或严重的配置问题
            log.error("删除 Quartz 任务失败: " + e.getMessage(), e);
        }

    }

    /**
     * 导入任务
     *
     * @param data 导入数据
     */
    public int importQtTask(List<ImportQtTaskDto> data) throws BizException {

        var success = 0;

        for (var dto : data) {

            try {

                Long groupId = null;

                //如果填写了分组名 则需要查询分组信息
                if (dto.getGroupName() != null) {
                    var groupPo = groupRepository.getByName(dto.getGroupName());

                    if (groupPo == null) {
                        throw new BizException("任务分组不存在:[" + dto.getGroupName() + "]");
                    }

                    groupId = groupPo.getId();
                }

                var addDto = as(dto, AddQtTaskDto.class);
                addDto.setGroupId(groupId);
                addQtTask(addDto);
                success++;
            } catch (BizException e) {
                log.error(e.getMessage(), e);
            }
        }

        return success;
    }

    /**
     * 导出任务列表
     *
     * @param dto 查询条件
     * @return 导出数据列表
     */
    public List<ExportQtTaskVo> exportQtTask(GetQtTaskListDto dto) throws BizException {
        QtTaskPo query = new QtTaskPo();
        assign(dto, query);
        List<QtTaskPo> pos = repository.getQtTaskListNotPage(query);
        return as(pos, ExportQtTaskVo.class);
    }

}