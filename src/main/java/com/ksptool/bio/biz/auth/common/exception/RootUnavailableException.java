package com.ksptool.bio.biz.auth.common.exception;

import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.web.ResultCode;
import org.springframework.security.authentication.AccountStatusException;

/**
 * 租户不可用异常，当用户租户未找到、被禁用、被删除时，会抛出此异常
 *
 * @since 1.6.20(T).25
 */
public class RootUnavailableException extends AccountStatusException {

    public RootUnavailableException(String message) {
        super(message);
    }

    public RootUnavailableException() {
        super("租户不可用");
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
