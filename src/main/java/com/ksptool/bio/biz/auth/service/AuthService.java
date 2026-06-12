package com.ksptool.bio.biz.auth.service;

import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Bio首创 自定义权限实现😄😄😄，auth取自Authorization前4个字母
 * <p>
 * 1.用户在登录鉴权成功后,UserDetailsService会将用户的角色+权限全部加载到UserDetails中 其中角色会被自动添加ROLE_前缀
 * 2.登录成功后会调用SessionService为用户创建Session,这Session将会缓存用户全部的权限码(包含那些带ROLE_前缀的角色码)
 * 3.当用户请求打入时,会通过UserSessionAuthFilter过滤器重建安全上下文,这会自动将这些权限与角色码统一设置到SecurityContextHolder中
 * 4.后续的接口调用会自动使用这个安全上下文,通过这个AuthService可以快捷的检查当前用户是否拥有指定权限码或用户组
 *
 * @author KspTooi
 * @since 1.2.7(G).37
 */
@Slf4j
@Service("auth")
public class AuthService {

    @Autowired
    private RegistrySdk regSdk;


    /**
     * 检查当前用户是否拥有指定权限码(静态) 兼容旧式接口
     *
     * @param permissionCode 权限码，如：core:user:add
     * @return 如果用户拥有该权限码返回true，否则返回false
     */
    public static boolean hasPermission(String permissionCode) {

        if (StringUtils.isBlank(permissionCode)) {
            return false;
        }

        //获取安全上下文
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        var targetCode = permissionCode.trim();
        boolean isRoleCheck = targetCode.startsWith("ROLE_");

        for (var pCode : authentication.getAuthorities()) {

            if (StringUtils.isBlank(pCode.getAuthority())) {
                continue;
            }

            var stdCode = pCode.getAuthority().trim();

            //如果安全上下文中有通配权限*:*:* 则直接判定通过
            if (stdCode.equals("*:*:*")) {
                return true;
            }

            //如果现在在检查角色码(ROLE_)，但当前拥有的权限码不是角色码，跳过
            if (isRoleCheck && !stdCode.startsWith("ROLE_")) {
                continue;
            }

            //如果是检查权限码(非ROLE_)，但当前拥有的权限是角色码，跳过
            if (!isRoleCheck && stdCode.startsWith("ROLE_")) {
                continue;
            }

            //匹配
            if (stdCode.equals(permissionCode.trim())) {
                return true;
            }

        }

        return false;
    }

    /**
     * 检查当前用户是否拥有超级权限(实例)
     *
     * @return 如果用户拥有超级权限返回true，否则返回false
     */
    public static boolean hasSuperCode() {
        return hasPermission("*:*:*");
    }

    /**
     * 检查当前用户是否拥有指定权限码(实例)
     *
     * @param permissionCode 权限码，如：core:user:add
     * @return 如果用户拥有该权限码返回true，否则返回false
     */
    public boolean hasCode(String permissionCode) {

        //如果注册表中关闭了操作权限校验，则直接放行
        if (regSdk.getInt(AppRegistry.FA_ENABLED_OPERATION_PERMISSION.getFullKey(), 1) == 0) {
            return true;
        }

        return hasPermission(permissionCode);
    }

    /**
     * 检查当前用户是否拥有指定用户组(实例)
     *
     * @param groupCode 用户组码，如：admin
     * @return 如果用户拥有该用户组返回true，否则返回false
     */
    public boolean hasGroup(String groupCode) {
        return hasPermission("ROLE_" + groupCode);
    }

    /**
     * 检查当前用户是否不拥有指定权限码(实例)
     *
     * @param permissionCode 权限码，如：core:user:add
     * @return 如果用户不拥有该权限码返回true，否则返回false
     */
    public boolean absentCode(String permissionCode) {
        return !hasPermission(permissionCode);
    }

    /**
     * 检查当前用户是否不拥有指定用户组(实例)
     *
     * @param groupCode 用户组码，如：admin
     * @return 如果用户不拥有该用户组返回true，否则返回false
     */
    public boolean absentGroup(String groupCode) {
        return !hasGroup(groupCode);
    }


}