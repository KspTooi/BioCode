package com.ksptool.bio.biz.auth.common;

import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限桶，用于把一堆乱七八糟的权限码都塞进来然后统一转换。
 * <p>
 * 其实系统里面有好几种权限码，但它们都是同一个东西。
 * 比如:
 * Auth域的PermissionPo
 * Auth区的GroupPo(角色码)
 * SpringSecurity的GrantedAuthority
 * 这三类东西在系统中的本质就是一个字符串，但是类型不同，这个桶支持一锅大乱炖。
 *
 * @author KspTool
 * @since 2026-04-29
 */
public class PermissionBucket {

    //在SpringSecurity里面 角色和权限 都属于权限码，但是框架会通过ROLE_前缀来区分这是一个权限码还是角色
    private static final String ROLE_PREFIX = "ROLE_";

    //原始权限码集合
    private final Set<String> rawCodes;

    public PermissionBucket() {
        //普通的HashSet输出不稳定,用LinkedHashSet保证顺序
        this.rawCodes = new LinkedHashSet<>();
    }

    /**
     * 往桶内加入原始权限码
     *
     * @param rawPermissionCode 原始权限码
     */
    public void addPermission(String rawPermissionCode) {
        if (StringUtils.isBlank(rawPermissionCode)) {
            return;
        }
        rawCodes.add(rawPermissionCode.trim());
    }

    /**
     * 往桶内加入一堆原始权限码
     *
     * @param rawPermissionCodes 原始权限码集合
     */
    public void addPermission(Collection<String> rawPermissionCodes) {
        if (rawPermissionCodes == null) {
            return;
        }
        for (var rawPermissionCode : rawPermissionCodes) {
            addPermission(rawPermissionCode);
        }
    }

    /**
     * 往桶内加入权限码PO
     *
     * @param permissionPo 权限码PO
     */
    public void addPermission(PermissionPo permissionPo) {
        if (permissionPo == null) {
            return;
        }
        addPermission(permissionPo.getCode());
    }

    /**
     * 往桶内加入一堆权限码PO
     *
     * @param pos 权限码PO集合
     */
    public void addPermissionPos(Collection<PermissionPo> pos) {
        if (pos == null) {
            return;
        }
        for (var po : pos) {
            addPermission(po);
        }
    }

    /**
     * 往桶内加入原始角色码
     *
     * @param rawGroupCode 角色码
     */
    public void addGroup(String rawGroupCode) {
        if (StringUtils.isBlank(rawGroupCode)) {
            return;
        }

        var trim = rawGroupCode.trim();

        //如果角色码已经带上了ROLE_前缀，则直接添加
        if (trim.startsWith(ROLE_PREFIX)) {
            rawCodes.add(trim);
            return;
        }

        //如果角色码不带前缀 自动添加
        rawCodes.add(ROLE_PREFIX + trim);
    }

    /**
     * 往桶内加入一堆原始角色码
     *
     * @param rawGroupCodes 角色码集合
     */
    public void addGroups(Collection<String> rawGroupCodes) {
        if (rawGroupCodes == null) {
            return;
        }
        for (var rawGroupCode : rawGroupCodes) {
            addGroup(rawGroupCode);
        }
    }

    /**
     * 往桶内加入角色PO
     *
     * @param groupPo 角色PO
     */
    public void addGroup(GroupPo groupPo) {
        if (groupPo == null) {
            return;
        }
        addGroup(groupPo.getCode());
    }

    /**
     * 往桶内加入一堆角色PO
     *
     * @param pos 角色PO集合
     */
    public void addGroupPos(Collection<GroupPo> pos) {
        if (pos == null) {
            return;
        }
        for (var po : pos) {
            addGroup(po);
        }
    }

    /**
     * 往桶内加入SpringSecurity的GrantedAuthority
     *
     * @param ga GrantedAuthority 这东西可能是角色码也可能是权限码(角色码已经自动带上了ROLE_前缀)
     */
    public void addGrantedAuthority(GrantedAuthority ga) {
        if (ga == null || StringUtils.isBlank(ga.getAuthority())) {
            return;
        }
        rawCodes.add(ga.getAuthority().trim());
    }

    /**
     * 往桶内加入一堆SpringSecurity的GrantedAuthority
     *
     * @param gas SpringSecurity的GrantedAuthority集合
     */
    public void addGrantedAuthorities(Collection<GrantedAuthority> gas) {
        if (gas == null) {
            return;
        }
        for (var ga : gas) {
            addGrantedAuthority(ga);
        }
    }

    /**
     * 将桶内元素转换为GrantedAuthority列表
     *
     * @return GrantedAuthority列表
     */
    public Set<GrantedAuthority> toGrantedAuthorities() {
        return rawCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 将桶内元素转换为原始权限码列表
     *
     * @return 原始权限码列表
     */
    public Set<String> toRaw() {
        return new LinkedHashSet<>(rawCodes);
    }

    /**
     * 判断桶内是否包含超级操作权限
     *
     * @return 是否包含超级操作权限
     */
    public boolean hasSuperCode() {
        return rawCodes.contains("*:*:*");
    }

    /**
     * 判断桶内是否包含超级数据权限
     *
     * @return 是否包含超级数据权限
     */
    public boolean hasSuperRsCode() {
        return rawCodes.contains("*:*:*:*");
    }

    /**
     * 判断桶是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return rawCodes.isEmpty();
    }

    /**
     * 判断桶是否不为空
     *
     * @return 是否不为空
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * 清空桶
     */
    public void clear() {
        rawCodes.clear();
    }

    /**
     * 获取桶中元素的数量
     *
     * @return 元素数量
     */
    public int size() {
        return rawCodes.size();
    }

    /**
     * 获取桶中元素的列表
     *
     * @return 元素列表
     */
    public List<String> toRawList() {
        return new ArrayList<>(rawCodes);
    }

    /**
     * 合并另一个桶的内容到当前桶
     * <p>
     * 另一个桶里的元素已经做过归一化(trim/ROLE_前缀)，这里直接搬运，不再走校验逻辑。
     *
     * @param other 另一个权限桶
     */
    public void merge(PermissionBucket other) {
        if (other == null) {
            return;
        }
        if (other == this) {
            return;
        }
        rawCodes.addAll(other.rawCodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PermissionBucket that)) {
            return false;
        }
        return rawCodes.equals(that.rawCodes);
    }

    @Override
    public int hashCode() {
        return rawCodes.hashCode();
    }

    @Override
    public String toString() {
        return "PermissionBucket(" + rawCodes + ")";
    }

    /**
     * 从Authentication中获取权限桶
     *
     * @param authentication Authentication
     * @return 权限桶
     */
    public static PermissionBucket of(Authentication authentication) {
        if (authentication == null) {
            return new PermissionBucket();
        }
        var bucket = new PermissionBucket();
        bucket.addGrantedAuthorities(new ArrayList<>(authentication.getAuthorities()));
        return bucket;
    }
}
