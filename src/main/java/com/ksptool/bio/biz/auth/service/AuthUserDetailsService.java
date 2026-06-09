package com.ksptool.bio.biz.auth.service;

import com.ksptool.bio.biz.auth.common.PermissionBucket;
import com.ksptool.bio.biz.auth.common.RsCalculator;
import com.ksptool.bio.biz.auth.common.exception.AuthUnavailableException;
import com.ksptool.bio.biz.auth.common.exception.RootUnavailableException;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.repository.GroupDeptRepository;
import com.ksptool.bio.biz.auth.repository.GroupMenuRepository;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.repository.CoreRootRepository;
import com.ksptool.bio.biz.core.repository.MenuPackRepository;
import com.ksptool.bio.biz.core.repository.MenuRepository;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTool
 * @since 1.5.23(W).109
 */
@Slf4j
@Service
public class AuthUserDetailsService implements UserDetailsService {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserRepository uRepository;

    @Autowired
    private GroupRepository gRepository;

    @Autowired
    private GroupDeptRepository gdRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private CoreRootRepository rRepository;

    @Autowired
    private PermissionRepository pRepository;

    @Autowired
    private GroupMenuRepository gmRepository;

    @Autowired
    private MenuRepository mRepository;

    @NullMarked
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {

            //根据用户名查询用户
            var user = uRepository.getUserByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("用户 [" + username + "] 不存在");
            }

            //检查租户是否正常
            var root = rRepository.findById(user.getRootId()).orElse(null);

            if (root == null) {
                throw new RootUnavailableException("用户 [" + username + "] 未绑定租户");
            }

            if (root.isExpired()) {
                throw new RootUnavailableException("用户 [" + username + "] 所属租户已于" + root.getExpireTime().format(dtf) + "到期");
            }

            if (root.getStatus() != 0) {
                throw new RootUnavailableException("用户 [" + username + "] 所属租户已被禁用！");
            }

            //检查用户是否正常
            if (user.isDisabled()) {
                throw new DisabledException("用户 [" + username + "] 已被封禁！");
            }

            //获取用户拥有的全部权限码(这里只获取启用的组派生出的权限码)
            var permissionCodes = pRepository.getCodesByUserId(user.getId());

            //获取用户拥有的全部用户组
            var groups = gRepository.getGroupsByUserIdAndStatus(user.getId(), Switch.on());

            //通过菜单衍生的权限码
            var menusCodes = new HashSet<String>();

            //如果用户是租管,还需要获取租户上的菜包，这些菜包里面也有权限码，也要合并到权限码中
            if(rRepository.isAdminOfRoot(user.getRootId(), user.getId())){
                var mPos = mRepository.getMenusByGrantedPack(user.getRootId());
                for (var menu : mPos) {
                    menusCodes.addAll(menu.getPermissionCode());
                }
            }

            //获取组上的全部菜单，并合并权限码
            if (!groups.isEmpty()) {
                var menus = gmRepository.getMenusByGids(groups.stream().map(GroupPo::getId).toList());
                for (var menu : menus) {
                    menusCodes.addAll(menu.getPermissionCode());
                }
            }

            //组装AUS
            var aus = new AuthUserSession();

            //合并同类项
            assign(user, aus);
            aus.setUserId(user.getId());
            aus.setRootName(root.getName());

            //如果用户有直属企业和部门 则需要查询对应的名称
            if (user.getOrgId() != null) {
                var org = orgRepository.findById(user.getOrgId()).orElseThrow(() -> new DisabledException("企业信息异常:" + user.getOrgId()));
                aus.setOrgName(org.getName());
            }

            if (user.getDeptId() != null) {
                var dept = orgRepository.findById(user.getDeptId()).orElseThrow(() -> new DisabledException("部门信息异常:" + user.getDeptId()));
                aus.setDeptName(dept.getName());
            }

            //GrantedAuthority包括角色码和权限码 SpringSecurity通过ROLE_前缀区分角色和权限
            var pb = new PermissionBucket();
            pb.addGroupPos(groups);
            pb.addPermission(permissionCodes);
            pb.addPermission(menusCodes);
            aus.setAuthorities(pb.toGrantedAuthorities());

            //开始计算RS数据权限(使用全新的RS计算器)
            var rsCalculator = new RsCalculator(user, groups, gdRepository, orgRepository);
            var result = rsCalculator.calculate();

            //设置RS数据权限计算结果到AUS中 
            aus.setRsMax(result.rsMax());
            aus.setRsAllowOrgIds(result.allowOrgIds());

            /*
             * 关于RS为3和2的处理
             * 这里有两种方案
             * 方案A: 用户登录/刷新会话时预计算部门以及下级部门IDS,存储到AUD中。（此方案需要处理部门树或用户角色/部门变动时的会话更新）
             * 方案B: 在查询时按数据范围动态拼接/注入SQL(基于 core_org.org_path_ids 做“包含当前 deptId 的后代”匹配再 IN)
             *
             * ⭕#0 初版:使用方案A时需要在此处理预计算 我们采纳方案B所以不进行预计算,RS处理延后到Hibernate过滤器或者Specification中
             * #0原因:
             * 主流做法是方案B：查询时按数据范围动态拼接/注入 SQL（AOP @DataScope），例如“本部门及以下”用 dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id=? OR FIND_IN_SET(?, ancestors)) 这类子查询，不在登录时预计算。
             * 你们系统建议优先采纳方案B（基于 core_org.org_path_ids 做“包含当前 deptId 的后代”匹配再 IN），一致性最好、权限/组织变更即时生效；方案1 只适合作为优化（加缓存/会话字段）且必须处理变更失效与会话膨胀问题。
             *
             * ✅#1 修订:现在采用方案A,即预计算RS允许访问的部门IDS并存储到AUS中
             * #1原因:
             * 你们用的是 JPA/Hibernate，不是 MyBatis,因为 MyBatis 的 ${params.dataScope} 可以直接拼接任意 SQL 片段。你们用 JPA，要在 Hibernate @Filter 或 Specification 里动态注入包含 LIKE 的子查询，写起来痛苦且脆弱。方案A 下 Filter/Spec 只需要 dept_id IN (:ids)，这在 JPA 里是最自然的写法。
             * 你们的数据模型已经为方案A而设计 UserSessionPo.rs_allow_depts 是 JSON 列，AuthUserSession.rsAllowDepts 是 Set<Long> — 这就是为"存一组预计算好的部门ID"而建的容器。如果走方案B，这两个字段在 RS=2/3 时永远为空，完全浪费。
             * 组织树变更频率极低 部门调整是低频管理操作（天/周级别），而列表查询是高频操作（秒级别）。把低频计算放在登录/刷新时做一次，让高频查询保持简单，是正确的权衡。至于一致性，组织树变更时清一下受影响用户的 Session 就够了，不需要复杂的版本号机制。
             *
             */

            return aus;

        } catch (Exception e) {

            if(e instanceof RootUnavailableException){
                throw e;
            }
            if(e instanceof DisabledException){
                throw e;
            }
            if(e instanceof AuthUnavailableException){
                throw e;
            }

            log.error(e.getMessage(), e);
            //出现内部错误时抛出认证不可用异常
            throw new AuthUnavailableException("当前认证系统暂时不可用,请稍后再试!");
        }
    }

}
