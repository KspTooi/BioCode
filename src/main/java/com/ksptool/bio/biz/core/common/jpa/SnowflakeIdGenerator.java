package com.ksptool.bio.biz.core.common.jpa;

import com.ksptool.bio.commons.utils.IdWorker;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.util.EnumSet;

/**
 * @author KspTooi
 * @since 1.6.15(O).65
 */
public class SnowflakeIdGenerator implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {

        // 如果外部在新增时已经手动 set 了 ID，则保留手动设置的值，否则生成新的ID
        // 2026-03-15 修订: 现在不再支持在新增时手动从外部设置ID字段，否则会导致数据新增失败，所以这里注释掉了
        /* if (currentValue != null) {
            return currentValue;
        } */

        //检查是否是新增操作(通常从外部设置ID以后JPA将会判定为已存在实体而执行Merge操作，不会走到这一步来，这里只是为了防止万一。)
        if (eventType == EventType.INSERT) {

            //新增时如果检查到从外部设置了ID字段，则抛出异常
            if (currentValue != null) {
                throw new RuntimeException("使用了自动ID生成器的PO类在新增实体时请勿从外部手动设置ID字段，如果需要完全手动管理ID，请移除@SnowflakeIdGenerated注解。");
            }

        }

        return IdWorker.nextId();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        // 仅在插入（INSERT）时触发
        return EventTypeSets.INSERT_ONLY;
    }
}