package com.ksptool.bio.biz.qf.commons;

import org.flowable.common.engine.impl.cfg.IdGenerator;

import xyz.downgoon.snowflake.Snowflake;

/**
 * 雪花算法ID生成器
 * <p>
 * 此类用于自定义Flowable的ID生成器,因原版ID生成器使用UUID，长度太长，不利于存储和查询。
 * 故使用雪花算法ID生成器来生成ID。
 *
 * @author WangQingHua(603484930@qq.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
public class QfSnowflakeIdGenerator implements IdGenerator {

    private static final Snowflake generator = new Snowflake(1, 2);


    @Override
    public String getNextId() {
        return "QF_ENG_" + String.valueOf(generator.nextId());
    }

}
