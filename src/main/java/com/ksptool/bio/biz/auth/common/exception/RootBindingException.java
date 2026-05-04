package com.ksptool.bio.biz.auth.common.exception;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.web.ResultCode;

/**
 * 用户未绑定租户异常
 * 当用户试图执行需要绑定租户的操作时，会抛出此异常
 *
 * @since 1.6.20(T).25
 */
public class RootBindingException extends BizException {

    public RootBindingException(String message) {
        super(message);
    }

    public RootBindingException() {
        super("用户未绑定租户");
    }

    /**
     * 转换为响应结果
     *
     * @return 响应结果
     */
    public Result<String> toResult() {
        return Result.error(ResultCode.REQUIRE_ROOT.getCode(), ResultCode.REQUIRE_ROOT.getMessage());
    }

}
