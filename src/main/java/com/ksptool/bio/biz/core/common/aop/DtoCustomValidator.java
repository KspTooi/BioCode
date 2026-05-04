package com.ksptool.bio.biz.core.common.aop;

/**
 * 自定义DTO验证接口
 * 所有自定义DTO验证类都实现此接口
 *
 * @author KspTooi
 * @since 1.6.14(N).68
 */
public interface DtoCustomValidator {

    /**
     * 自定义DTO验证方法，在此编写您的自定义验证逻辑
     *
     * @return 验证结果 如果验证通过则返回null 否则返回错误信息
     */
    String validate();

}
