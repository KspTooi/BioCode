package com.ksptool.bio.biz.qt.common;

import com.ksptool.bio.biz.qt.model.qttaskrcd.QtTaskRcdPo;
import com.ksptool.bio.biz.qt.repository.QtTaskRcdRepository;
import com.ksptool.bio.biz.qt.repository.QtTaskRepository;
import com.ksptool.bio.biz.qt.service.QtTaskRcdService;
import com.ksptool.bio.biz.qt.service.QtTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.ksptool.entities.Entities.toJson;

/**
 * QF本地任务Bean执行器
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
@Slf4j
@Component
public class LocalBeanExecutionJob extends QuartzJobBean {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QtTaskService qtTaskService;

    @Autowired
    private QtTaskRepository qtTaskRepository;

    @Autowired
    private QtTaskRcdService qtTaskRcdService;

    @Autowired
    private QtTaskRcdRepository qtTaskRcdRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        //获取任务快照数据 (这些数据是调度时塞进去的)
        var dataMap = context.getMergedJobDataMap();
        var taskId = dataMap.getLong("taskId"); // 任务ID
        var beanName = dataMap.getString("target");   // BEAN代码
        var jsonParams = dataMap.getString("params"); // 调用参数JSON
        var policyError = dataMap.getInt("policyError"); // 失败策略 0:默认 1:自动暂停
        var policyRcd = dataMap.getInt("policyRcd"); // 日志策略 0:全部 1:仅异常 2:不记录

        var taskPo = qtTaskRepository.getById(taskId);
        ;

        if (taskPo == null) {
            log.error("任务不存在,这可能是因为任务被删除或从未被创建: {}", taskId);
            qtTaskService.abortTask(taskId);
            return;
        }

        //记录任务开始时间
        var startTime = LocalDateTime.now();
        var endTime = LocalDateTime.now();
        QtTaskRcdPo taskRcdPo = null;
        String result = null; //执行结果 成功时为JSON字符串 异常时为异常堆栈
        var lastExecStatus = 0; //上次执行状态 0:成功 1:异常

        //是否存在参数转换错误
        var hasParamError = false;

        //如果日志策略为 0:全部 则需要在任务开始时记录日志
        if (policyRcd == 0) {
            taskRcdPo = qtTaskRcdService.createNewTaskRcd(taskPo);
        }

        try {
            //查找 Bean (安全检查)
            QuickTask<?> handler = QuickTaskRegistry.getBean(beanName);

            if (handler == null) {
                throw new RuntimeException("本地任务Bean不存在: " + beanName);
            }

            //提供给执行器的参数
            Object paramObj = null;

            //入参不为空 需要处理参数映射
            if (StringUtils.isNotBlank(jsonParams)) {

                try {

                    //参数自动转换黑科技
                    ResolvableType resolvableType = ResolvableType.forClass(AopUtils.getTargetClass(handler)).as(QuickTask.class);
                    Class<?> paramType = resolvableType.getGeneric(0).resolve();

                    //处理特殊情况
                    if (paramType != null) {

                        //用户想直接要 JSON 字符串或未定义泛型
                        if (String.class.isAssignableFrom(paramType)) {
                            paramObj = jsonParams;
                        }

                        //复用Spring Boot 默认配置好的 ObjectMapper
                        if (!String.class.isAssignableFrom(paramType)) {
                            JavaType javaType = objectMapper.getTypeFactory().constructType(paramType);
                            paramObj = objectMapper.readValue(jsonParams, javaType);
                        }

                    }

                } catch (Exception e) {
                    //处理参数转换时出现异常,终止任务
                    log.error(e.getMessage(), e);

                    result = ExceptionUtils.getStackTrace(e);
                    lastExecStatus = 1; //设置上次执行状态为异常
                    hasParamError = true; //设置存在参数转换错误
                    return;
                }

            }

            //执行任务业务
            //这里的强转是安全的，因为我们已经处理了泛型
            result = toJson(((QuickTask<Object>) handler).execute(paramObj));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //截取异常堆栈
            result = ExceptionUtils.getStackTrace(e);
            //设置上次执行状态为异常
            lastExecStatus = 1;

        } finally {

            endTime = LocalDateTime.now();

            //更新任务快照 不论日志策略如何 都需要更新任务快照
            taskPo.setLastExecStatus(lastExecStatus);
            taskPo.setLastStartTime(startTime);
            taskPo.setLastEndTime(endTime);
            qtTaskRepository.save(taskPo);

            //处理任务已失败
            if (lastExecStatus == 1) {

                //如果日志策略为 1:仅异常 则需要在异常时创建新日志
                if (policyRcd == 1) {
                    taskRcdPo = qtTaskRcdService.buildNewTaskRcdWithException(taskPo);
                    taskRcdPo.setTargetResult(result);
                    taskRcdPo.setStatus(1);
                    taskRcdPo.setStartTime(startTime);
                    taskRcdPo.setEndTime(endTime);
                    taskRcdPo.setCostTime((int) Duration.between(startTime, endTime).toMillis());
                    qtTaskRcdRepository.saveAndFlush(taskRcdPo);
                }

                //如果失败策略为自动暂停，则暂停任务(如果有参数转换错误,则不走此分支)
                if (policyError == 1 && !hasParamError) {
                    qtTaskService.abortTask(taskId, true);
                    log.info("任务: {} 执行异常,触发失败策略,已暂停任务", taskId);
                }

                //如果有参数转换错误 则不论失败策略如何 都直接终止任务
                if (hasParamError) {
                    qtTaskService.abortTask(taskId);
                    log.info("任务: {} 执行异常,存在参数转换错误,已终止任务", taskId);
                }

            }


            //如果日志策略为 0:全部 则需要在任务结束时更新日志
            if (policyRcd == 0) {
                taskRcdPo.setEndTime(endTime);
                taskRcdPo.setCostTime((int) Duration.between(startTime, endTime).toMillis());
                taskRcdPo.setTargetResult(result);
                taskRcdPo.setStatus(lastExecStatus);
                qtTaskRcdRepository.saveAndFlush(taskRcdPo);
            }

        }
    }


}