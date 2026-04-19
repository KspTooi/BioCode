package com.ksptool.bio.biz.qf.commons.listener;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

/**
 * 多实例 collection 变量重命名解析处理器
 * <p>
 * 前端设计器对所有多实例节点统一落 {@code ${assigneeList}} / {@code ${groupList}}，
 * 同一流程内多个多实例节点会使用同名变量，启动时后写的会覆盖先写的，导致人员错乱。
 * <p>
 * 本处理器在 <b>部署期</b>（BPMN 解析阶段）被调用，自动将每个多实例 UserTask 的
 * collection 表达式改写为 {@code ${qfMi_<taskId>}}，利用节点 ID 的唯一性区分变量名，
 * 彻底避免同名冲突，且无需修改前端代码。
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @since 2026-04-15
 * @license Apache License 2.0
 */
public class QfMiRenameParseHandler extends AbstractBpmnParseHandler<UserTask> {

    /**
     * 重命名后的变量名前缀，以 QF 域命名空间避免与业务变量冲突
     */
    private static final String VAR_PREFIX = "qfMi_";

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask ut) {

        MultiInstanceLoopCharacteristics loop = ut.getLoopCharacteristics();

        if (loop == null) {
            //非多实例节点
            return;
        }

        String coll = loop.getInputDataItem();

        if (StringUtils.isBlank(coll)) {
            //使用 loopCardinality 写死次数的多实例，不需要 collection 变量
            return;
        }

        //幂等：已经被重命名过则跳过（重新部署同一 XML 时不会重复加前缀）
        if (coll.contains(VAR_PREFIX)) {
            return;
        }

        //把 ${assigneeList} 改写为 ${qfMi_<taskId>}
        loop.setInputDataItem("${" + VAR_PREFIX + ut.getId() + "}");
    }
}
