package com.ksptool.bio.biz.auth.common.aop;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * 系统内置数据权限过滤器基类(isSystem 字段过滤)
 * <p>
 * 继承此类的实体会被 {@link SystemScopeAspect} 管控。
 * 当用户不具备透视权限(PERSP)或超级数据权限(SR)时，
 * Hibernate 自动追加 is_system = false 的过滤条件，屏蔽内置数据。
 * <p>
 * 请注意: 继承此类的子类需要显式声明 isSystem 字段，否则在查询时会报错。
 *
 * @author KspTool
 */
@MappedSuperclass
@FilterDef(name = "systemScopeFilter", parameters = {
        @ParamDef(name = "isSystem", type = Integer.class)
})
@Filter(
        name = "systemScopeFilter",
        condition = "is_system = :isSystem"
)
public abstract class SystemScopePo {

    @Transient
    @Column(name = "is_system", nullable = false, comment = "是否为系统内置数据(0:否 1:是)")
    private Integer isSystem;

}
