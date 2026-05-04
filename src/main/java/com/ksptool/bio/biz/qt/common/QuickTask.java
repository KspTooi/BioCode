package com.ksptool.bio.biz.qt.common;

import com.ksptool.assembly.entity.web.Result;

/**
 * QF任务接口
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Apache License 2.0
 * @since 1.6.1(A).1
 */
public interface QuickTask<T> {

    /**
     * 执行任务逻辑
     *
     * @param params 前端传入的JSON会自动转为这个对象
     * @return 任务执行结果
     */
    Result<?> execute(T params) throws Exception;

}
