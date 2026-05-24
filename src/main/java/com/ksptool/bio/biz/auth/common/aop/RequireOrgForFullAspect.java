package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.common.PermissionBucket;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 组织必填切面
 * <p>
 * 拦截标注了 {@link RequireOrgForFull} 的方法，验证在 FULL 数据权限模式下新增数据时用户必须有部门或公司
 * <p>
 * 验证逻辑:
 * 1. 获取当前登录用户的 {@link AuthUserSession}
 * 2. 解析当前方法/类上的 {@link RowScope.Mode}
 * 3. 如果是 FULL 模式且用户既没有 orgId 也没有 deptId，则抛出异常
 *
 */
@Aspect
@Component
public class RequireOrgForFullAspect {

    private static final String ERROR_MESSAGE = "当前用户未绑定部门或公司，无法新增数据！请联系管理员分配组织信息。";

    @Before("@annotation(com.ksptool.bio.biz.auth.common.aop.RequireOrgForFull)")
    public void requireOrgForFull(JoinPoint joinPoint) throws BizException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BizException("获取当前登录用户失败!");
        }

        AuthUserSession aud = (AuthUserSession) auth.getPrincipal();
        if (aud == null) {
            throw new BizException("获取当前登录用户会话失败!");
        }

        RowScope.Mode mode = resolveMode(joinPoint);
        if (mode != RowScope.Mode.FULL) {
            return;
        }

        if (aud.getOrgId() == null || aud.getDeptId() == null) {
            throw new BizException(ERROR_MESSAGE);
        }
    }

    private RowScope.Mode resolveMode(JoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        RowScope classAnnotation = targetClass.getAnnotation(RowScope.class);
        if (classAnnotation != null) {
            return classAnnotation.mode();
        }
        return RowScope.Mode.FULL;
    }
}