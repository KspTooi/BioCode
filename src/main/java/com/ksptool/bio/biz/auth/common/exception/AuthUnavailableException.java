package com.ksptool.bio.biz.auth.common.exception;

import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.commons.web.ResultCode;
import org.springframework.security.authentication.AccountStatusException;

/**
 * 认证不可用异常，当认证系统出现内部错误时，会抛出此异常
 *
 * @since 1.6.25(Y).1
 */
public class AuthUnavailableException extends AccountStatusException {

    public AuthUnavailableException(String message) {
        super(message);
    }

    public AuthUnavailableException() {
        super("当前认证系统暂时不可用,请稍后再试!");
    }

    /**
     * 转换为响应结果
     *
     * @return 响应结果
     */
    public Result<String> toResult() {
        return Result.error(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage());
    }

}
