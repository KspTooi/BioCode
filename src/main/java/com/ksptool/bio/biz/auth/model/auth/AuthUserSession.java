package com.ksptool.bio.biz.auth.model.auth;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * 认证用户详情
 * 这是Spring Security的UserDetails实现类，用于存储已认证的用户信息，这包括用户基础信息和权限码
 * <p>
 * 1.6.34 版本更新
 * 与UserSessionPo保持一致
 */
@Getter
@Setter
public class AuthUserSession implements UserDetails {

    /**
     * 这里是Session的属性，用于存储Session的会话信息
     */
    //用户ID
    private Long userId;

    //租户ID
    private Long rootId;

    //直属企业ID
    private Long orgId;

    //直属部门ID
    private Long deptId;

    //租户名称
    private String rootName;

    //直属企业名称
    private String orgName;

    //直属部门名称
    private String deptName;

    //用户名
    private String username;

    //用户昵称
    private String nickname;

    //用户权限码和角色码(角色码通过ROLE_前缀区分) SpringSecurity通过ROLE_前缀区分角色和权限
    private Set<GrantedAuthority> authorities;

    //最大RowScope等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织 100:未配置
    private Integer rsMax;

    //RowScope允许访问的组织IDS
    private Set<Long> rsAllowOrgIds;

    //数据版本
    private Long dataVersion;

    //会话过期时间
    private LocalDateTime expiresAt;


    /**
     * 以下字段在被重建的上下文中不可用，只有用户第一次登录通过AuthenticationManager.authenticate()方法时才会被赋值
     */
    //密码
    private String password;

    //性别 0:男 1:女 2:不愿透露
    private Integer gender;

    //手机号
    private String phone;

    //邮箱
    private String email;

    //登录次数
    private Integer loginCount;

    //用户状态 0:正常 1:封禁
    private Integer status;

    //最后登录时间
    private LocalDateTime lastLoginTime;

    //是否为系统用户 0:否 1:是
    private Integer isSystem;

    //创建时间
    private LocalDateTime createTime;


    @NullMarked
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @NullMarked
    @Override
    public String getUsername() {
        return username;
    }

    @NullMarked
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @NullMarked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @NullMarked
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == 0; // 0:正常 1:封禁
    }

    /**
     * 获取最细粒度的组织架构ID
     * 如果用户有直属部门则返回直属部门ID，否则返回直属企业ID
     *
     * @return 最细粒度的组织架构ID
     */
    public Long getMinOrgId() {
        if (this.deptId != null) {
            return this.deptId;
        }
        return this.orgId;
    }

    /**
     * 判断用户是否拥有超级操作权限
     *
     * @return 是否拥有超级操作权限
     */
    public boolean hasSuperCode() {
        return this.authorities.stream().anyMatch(authority -> Objects.equals(authority.getAuthority(), "*:*:*"));
    }

    /**
     * 判断用户是否拥有超级数据权限
     *
     * @return 是否拥有超级数据权限
     */
    public boolean hasSuperRsCode() {
        return this.authorities.stream().anyMatch(authority -> Objects.equals(authority.getAuthority(), "*:*:*:*"));
    }
}
