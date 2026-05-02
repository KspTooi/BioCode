package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.auth.common.PermissionBucket;
import com.ksptool.bio.biz.auth.common.mybatis.RsContext;
import com.ksptool.bio.biz.auth.common.mybatis.RsContextHolder;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据权限过滤器切面(RS 7级数据权限-2ID方案)
 * 用于在Hibernate查询时为Session启用数据权限过滤器
 * <p>
 * 租户隔离原则:
 * 在没有超级数据权限的情况下,任何查询都必须强制带上 root_id = :rootId 作为硬底线
 * 即使 rsMax=0 (全集团数据) 也只能看到本租户的全部数据,而非真正跨租户的全部数据
 * 只有持有超级数据权限的用户才会跳过本过滤器,可访问所有租户的所有数据
 * <p>
 * 数据隔离模式 (由 @RowScope.mode 决定):
 * - FULL      : 启用完整 RS 7级数据权限过滤(默认),按用户真实 rsMax 注入
 * - ROOT_ONLY : 仅租户隔离,强制将 rsMax 覆盖为 0 ,忽略用户的 7级 RS 等级
 */
@Aspect
@Component
public class RowScopeAspect {

    private static final String ROW_SCOPE_FILTER_NAME = "rsFilter";

    @PersistenceContext
    private EntityManager entityManager;

    @Pointcut("@annotation(com.ksptool.bio.biz.auth.common.aop.RowScope) || @within(com.ksptool.bio.biz.auth.common.aop.RowScope)")
    public void rsPointcut() {
    }


    // 拦截标注了 @RowScope 的方法或类
    @Before("rsPointcut()")
    public void enableRowScopeFilter(JoinPoint joinPoint) throws BizException {

        //获取当前登录用户的 Session 上下文
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new BizException("在处理数据权限时，获取当前登录用户的 Authentication 失败!");
        }

        //获取登录成功的AUD
        AuthUserSession aud = (AuthUserSession) auth.getPrincipal();

        if (aud == null) {
            throw new BizException("在处理数据权限时，获取当前登录用户的 AUD 失败!");
        }

        //如果用户有超级数据权限 直接放行 不启用过滤器(可跨租户访问全部数据)
        if (PermissionBucket.of(auth).hasSuperRsCode()) {
            return;
        }

        //没有超级数据权限时,租户ID必须存在(否则无法做租户隔离,直接拒绝)
        Long rootId = aud.getRootId();
        if (rootId == null) {
            throw new BizException("在处理数据权限时，当前用户未绑定租户(rootId为空)，无法进行租户隔离!");
        }

        //解析当前切点的 @RowScope 注解,获取数据隔离模式(方法上优先,其次类上)
        RowScope.Mode mode = resolveMode(joinPoint);

        //根据模式决定实际生效的 rsMax 与 orgIds
        //- FULL      : 按用户真实 rsMax 与 rsAllowOrgIds 注入
        //- ROOT_ONLY : 强制 rsMax = 0,仅按租户隔离, orgIds 占位即可
        //- USER_ONLY : 强制 rsMax = 50,仅按用户隔离, creatorId 占位即可
        Integer rsMax = 100;
        List<Long> orgIds = new ArrayList<>();

        //FULL模式下，按用户真实 rsMax 与 rsAllowOrgIds 注入
        if (mode == RowScope.Mode.FULL) {
            rsMax = aud.getRsMax();
            if (aud.getRsAllowOrgIds() != null) {
                orgIds.addAll(aud.getRsAllowOrgIds());
            }
        }

        //ROOT_ONLY模式下，强制 rsMax = 0,仅按租户隔离, orgIds 占位即可
        if (mode == RowScope.Mode.ROOT_ONLY) {
            rsMax = 0;
        }

        //USER_ONLY模式下，强制 rsMax = 50,仅按用户隔离
        if (mode == RowScope.Mode.USER_ONLY) {
            rsMax = 50;
        }


        //在Mybatis数据权限上下文中设置当前线程的数据权限上下文
        RsContextHolder.set(new RsContext(rsMax, aud.getUserId(), rootId, new ArrayList<>(orgIds)));

        //取出 Hibernate 的 Session 并激活过滤器
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(ROW_SCOPE_FILTER_NAME);

        //注入参数 rootId 在所有分支中均强制使用,作为租户隔离硬底线
        filter.setParameter("rsMax", rsMax);
        filter.setParameter("userId", aud.getUserId());
        filter.setParameter("rootId", rootId);

        //Hibernate IN 查询集合为空时会报错，塞入无效 ID 占位
        //同时这也是 rsMax=100(用户未配置任何数据权限) 时的拒绝策略: org_id 永远不会等于 -1
        if (orgIds.isEmpty()) {
            filter.setParameterList("orgIds", Collections.singletonList(-1L));
            return;
        }

        filter.setParameterList("orgIds", orgIds);
    }

    // 后置：无论成功失败，必定关闭过滤器
    @After("rsPointcut()")
    public void disableFilter() {
        Session session = entityManager.unwrap(Session.class);
        // 强制关闭，清空当前线程/Session的权限上下文
        session.disableFilter(ROW_SCOPE_FILTER_NAME);

        //清空Mybatis数据权限上下文
        RsContextHolder.clear();
    }

    /**
     * 解析当前切点上 @RowScope 注解的 mode
     * 优先级: 方法注解 > 类注解 > 默认 FULL
     *
     * @param joinPoint 切点
     * @return 数据隔离模式
     */
    private RowScope.Mode resolveMode(JoinPoint joinPoint) {

        //方法上的注解优先级最高
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            RowScope methodAnnotation = method.getAnnotation(RowScope.class);
            if (methodAnnotation != null) {
                return methodAnnotation.mode();
            }
        }

        //其次取目标类(代理前的真实类)上的注解
        Class<?> targetClass = joinPoint.getTarget().getClass();
        RowScope classAnnotation = targetClass.getAnnotation(RowScope.class);
        if (classAnnotation != null) {
            return classAnnotation.mode();
        }

        //兜底默认 FULL (理论上不会走到此分支,因为切点已确保至少有一处注解)
        return RowScope.Mode.FULL;
    }
}
