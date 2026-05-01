package com.ksptool.bio.biz.auth.common.aop;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.service.SessionService;
import jakarta.persistence.PrePersist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RS数据权限字段自动填充监听器
 * <p>
 * 在实体类 @PrePersist 时自动填充 @CreatedRootId 和 @CreatedOrgId 标注的字段
 * 字段已有值时不覆盖，仅在字段值为 null 时填充
 * <p>
 * 使用方法:
 * 1. 在需要自动填充的字段上添加 @CreatedRootId 或 @CreatedOrgId 注解
 * 2. 在实体类 @EntityListeners 中添加本监听器
 * <p>
 * 示例:
 * <pre>
 * {@code
 * @EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})
 * public class YourPo extends RowScopePo {
 *
 *     @CreatedRootId
 *     @Column(name = "root_id")
 *     private Long rootId;
 *
 *     @CreatedOrgId
 *     @Column(name = "org_id")
 *     private Long orgId;
 * }
 * }
 * </pre>
 *
 * @author KspTool
 * @since 2026-04-29
 */
public class RsAuditingEntityListener {

    private static final Map<Class<?>, List<Field>> ROOT_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> ORG_FIELD_CACHE = new ConcurrentHashMap<>();

    @PrePersist
    public void prePersist(Object target) throws AuthException {
        AuthUserSession session = SessionService.session();
        fillIfNull(target, ROOT_FIELD_CACHE, CreatedRootId.class, session.getRootId());
        fillIfNull(target, ORG_FIELD_CACHE, CreatedOrgId.class, session.getMinOrgId());
        fillIfNull(target, ORG_FIELD_CACHE, CreatedDirectOrgId.class, session.getOrgId());
    }

    private void fillIfNull(Object target,
                            Map<Class<?>, List<Field>> cache,
                            Class<? extends Annotation> annotation,
                            Object value) {
        List<Field> fields = cache.computeIfAbsent(target.getClass(), clazz -> scanFields(clazz, annotation));

        for (Field field : fields) {
            try {
                if (field.get(target) == null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "RsAuditingEntityListener: 无法访问字段 " + field.getName()
                        + " in " + target.getClass().getName(), e);
            }
        }
    }

    /**
     * 扫描类及其父类中所有标注了指定注解的字段，并设置可访问
     */
    private List<Field> scanFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    field.setAccessible(true);
                    result.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return result;
    }

}
