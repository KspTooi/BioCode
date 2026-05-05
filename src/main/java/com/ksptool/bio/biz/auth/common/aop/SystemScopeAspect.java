package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.common.PermissionBucket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 系统内置数据权限过滤器切面(isSystem 字段过滤)
 * <p>
 * 拦截标注了 {@link SystemScope} 的方法或类，根据当前用户是否持有
 * 透视权限(PERSP: *:*:*:*:PS) 或超级数据权限(SR: *:*:*:*) 决定是否启用过滤器：
 * - 持有超级数据权限或透视权限: 直接放行，不启用过滤器
 * - 无以上权限: 启用 systemScopeFilter，限制查询结果为 is_system = false
 *
 * @author KspTool
 */
@Aspect
@Component
public class SystemScopeAspect {

    private static final String SYSTEM_SCOPE_FILTER_NAME = "systemScopeFilter";

    @PersistenceContext
    private EntityManager entityManager;

    @Pointcut("@annotation(com.ksptool.bio.biz.auth.common.aop.SystemScope) || @within(com.ksptool.bio.biz.auth.common.aop.SystemScope)")
    public void systemScopePointcut() {
    }

    @Before("systemScopePointcut()")
    public void enableSystemScopeFilter() throws BizException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new BizException("在处理系统内置数据权限时，获取当前登录用户的 Authentication 失败!");
        }

        PermissionBucket bucket = PermissionBucket.of(auth);

        // 持有超级数据权限或透视权限，直接放行
        if (bucket.hasSuperRsCode() || bucket.hasPerspCode()) {
            return;
        }

        // 无权限时启用过滤器，只允许查询 is_system = false 的数据
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(SYSTEM_SCOPE_FILTER_NAME);
        filter.setParameter("isSystem", 0);
    }

    @After("systemScopePointcut()")
    public void disableSystemScopeFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter(SYSTEM_SCOPE_FILTER_NAME);
    }

}
