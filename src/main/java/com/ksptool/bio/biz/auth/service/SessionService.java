package com.ksptool.bio.biz.auth.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.auth.AuthUserSession;
import com.ksptool.bio.biz.auth.model.session.UserSessionPo;
import com.ksptool.bio.biz.auth.model.session.dto.GetSessionListDto;
import com.ksptool.bio.biz.auth.model.session.vo.GetSessionDetailsVo;
import com.ksptool.bio.biz.auth.model.session.vo.GetSessionListVo;
import com.ksptool.bio.biz.auth.repository.UserSessionRepository;
import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.commons.utils.SHA256;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.assign;

@Slf4j
@Service
public class SessionService {

    @Value("${session.expires}")
    private long expiresInSeconds;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 获取当前用户会话
     *
     * @return 当前用户会话
     * @throws AuthException 如果用户会话不存在，或用户未登录。
     */
    public static AuthUserSession session() throws AuthException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthException("用户未登录");
        }

        return (AuthUserSession) authentication.getPrincipal();
    }

    /**
     * 获取当前用户会话
     *
     * @return 当前用户会话，如果用户未登录，则返回null
     */
    public static AuthUserSession sessionWithNullable() {
        try {
            return session();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户权限
     *
     * @return 当前用户权限
     * @throws AuthException 如果用户未登录
     */
    public static List<GrantedAuthority> authorities() throws AuthException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthException("用户未登录");
        }
        return new ArrayList<>(authentication.getAuthorities());
    }

    /**
     * 判断当前用户是否具有超级权限
     *
     * @return 是否具有超级权限
     * @throws AuthException 如果用户未登录
     */
    public static boolean hasSuperCode() throws AuthException {
        var authorities = authorities();
        return authorities.stream().anyMatch(authority -> authority.getAuthority().equals("*:*:*"));
    }

    /**
     * 获取在线用户会话列表
     *
     * @param dto 查询条件
     * @return 在线用户会话列表
     */
    public PageResult<GetSessionListVo> getSessionList(GetSessionListDto dto) {
        Page<Tuple> page = userSessionRepository.getSessionList(dto, dto.pageRequest());
        return PageResult.success(tupleAs(page.getContent(), GetSessionListVo.class), page.getTotalElements());
    }

    /**
     * 获取在线用户会话详情
     *
     * @param id 会话ID
     * @return 用户会话详情
     * @throws BizException 如果会话不存在
     */
    public GetSessionDetailsVo getSessionDetails(Long id) throws BizException {

        UserSessionPo session = userSessionRepository.findById(id)
                .orElseThrow(() -> new BizException("会话不存在"));

        GetSessionDetailsVo vo = new GetSessionDetailsVo();
        vo.setId(session.getId());
        vo.setUsername("----");
        userRepository.findById(session.getUserId())
                .ifPresent(user -> vo.setUsername(user.getUsername()));
        vo.setCreateTime(session.getCreateTime());
        vo.setExpiresAt(session.getExpiresAt());
        vo.setPermissions(session.getPermissionCodes());

        //RS 0:全部 1:本公司/租户及以下 2:本部门及以下 3:本部门 4:仅本人 5:指定部门
        var maxRs = session.getRsMax();
        var rsAllowDepts = session.getRsAllowOrgIds();
        var rsAllowDeptNames = new ArrayList<String>();


        //处理RS权限列表
        if (maxRs == 2 || maxRs == 3 || maxRs == 5) {

            var depts = orgRepository.getDeptsByIds(new ArrayList<>(rsAllowDepts));

            if (depts.isEmpty()) {
                return vo;
            }

            var rootIds = depts.stream().map(OrgPo::getRootId).distinct().collect(Collectors.toList());
            var roots = orgRepository.getRootsByIds(rootIds);

            for (var dept : depts) {

                var rootName = "未知企业";

                for (var root : roots) {

                    if (Objects.equals(root.getId(), dept.getRootId())) {
                        rootName = root.getName();
                        break;
                    }

                }

                rsAllowDeptNames.add(rootName + " - " + dept.getName());
            }

        }

        vo.setRsMax(maxRs);
        vo.setRsDeptNames(rsAllowDeptNames);
        return vo;
    }

    /**
     * 关闭用户会话
     *
     * @param uid 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long uid) {
        closeSession(List.of(uid));
    }

    /**
     * 批量关闭用户会话
     *
     * @param uids 用户ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(List<Long> uids) {

        //获取该用户所有的会话
        var sessions = userSessionRepository.getSessionByUserIds(uids);

        Cache cache = cacheManager.getCache("userSession");

        if (cache != null) {
            //失效所有缓存
            for (var session : sessions) {
                cache.evict(session.getSessionId());
            }
        }

        //删除该用户所有的会话
        userSessionRepository.removeUserSessionByUserIds(uids);
    }

    /**
     * 关闭租户下所有用户会话
     *
     * @param rootId 租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeSessionByRootId(Long rootId) {
        closeSession(userSessionRepository.getOnlineUserIdsByRootId(rootId));
    }

    /**
     * 根据SessionId关闭用户会话
     *
     * @param sessionPrimaryKey 会话PK
     */
    public void closeSessionByPrimaryKey(Long sessionPrimaryKey) throws BizException {

        var session = userSessionRepository.findById(sessionPrimaryKey).orElseThrow(() -> new BizException("会话 [" + sessionPrimaryKey + "] 不存在"));

        //先失效缓存
        Cache cache = cacheManager.getCache("userSession");

        if (cache != null) {
            cache.evict(session.getSessionId());
        }

        //删除数据库中的会话
        userSessionRepository.delete(session);
    }

    /**
     * 获取当前登录的用户
     *
     * @return 当前用户
     */
    public UserPo requireUser() throws AuthException {
        var userId = session().getUserId();
        return userRepository.findById(userId).orElseThrow(() -> new AuthException("user not found"));
    }


    /**
     * 创建用户会话
     *
     * @param aus 认证用户详情
     * @return 用户会话ID(未经过SHA256的SessionId)
     */
    public String createSession(AuthUserSession aus) throws BizException {

        var sessionId = UUID.randomUUID().toString();
        var hashedSessionId = SHA256.hex(sessionId);

        var newSession = new UserSessionPo();

        //合并同类项
        assign(aus, newSession);

        //搜集权限码
        var permCodes = new HashSet<String>();

        for (var authority : aus.getAuthorities()) {
            permCodes.add(authority.getAuthority());
        }

        //存入数据库
        newSession.setId(null);
        newSession.setUsername(aus.getUsername());
        newSession.setUserId(aus.getUserId());
        newSession.setSessionId(hashedSessionId);
        newSession.setPermissionCodes(permCodes);
        newSession.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        newSession.setCreatorId(aus.getUserId());
        newSession.setDataVersion(aus.getDataVersion());
        userSessionRepository.save(newSession);

        //处理用户登录次数与最后登录时间
        var userPo = userRepository.findById(aus.getUserId()).orElseThrow(() -> new BizException("用户不存在"));
        userPo.setLoginCount(userPo.getLoginCount() + 1);
        userPo.setLastLoginTime(LocalDateTime.now());

        //更新用户
        userRepository.save(userPo);
        return sessionId;
    }

    /**
     * 刷新用户会话
     * 这个函数将会刷新
     * 1.会话基本数据
     * 2.权限码
     * 3.RS数据
     * 4.过期时间
     *
     * @param oldSession 旧用户会话
     * @return 新用户会话
     */
    public UserSessionPo refreshSession(UserSessionPo oldSession) throws BizException {

        //过期则不刷新
        if (oldSession.isExpired()) {
            return oldSession;
        }

        //更新基本信息
        var aud = (AuthUserSession) userDetailsService.loadUserByUsername(oldSession.getUsername());

        oldSession.setUsername(aud.getUsername());
        oldSession.setRootId(aud.getRootId());
        oldSession.setRootName(aud.getRootName());
        oldSession.setDeptId(aud.getDeptId());
        oldSession.setDeptName(aud.getDeptName());
        oldSession.setDataVersion(aud.getDataVersion());

        //更新权限码
        var permCodes = new HashSet<String>();

        for (var authority : aud.getAuthorities()) {
            permCodes.add(authority.getAuthority());
        }
        oldSession.setPermissionCodes(permCodes);

        //更新RS数据
        oldSession.setRsMax(aud.getRsMax());
        oldSession.setRsAllowOrgIds(new HashSet<>(aud.getRsAllowOrgIds()));

        //更新过期时间
        oldSession.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));

        //更新会话并返回
        return userSessionRepository.save(oldSession);
    }

    /**
     * 清除所有已登录用户的会话状态
     */
    @CacheEvict(cacheNames = "userSession", allEntries = true)
    public void clearUserSession() {
        // 删除所有用户会话记录
        userSessionRepository.deleteAll();
    }

    /**
     * 根据SessionId获取会话 这是一个带有缓存的查询方法 缓存配置位于com.ksptool.bio.commons.config.CacheConfig
     *
     * @param sessionId 会话SessionId
     * @return 会话
     * @throws BizException 如果会话不存在，或会话已过期。
     */
    @Cacheable(cacheNames = "userSession", key = "T(com.ksptool.bio.commons.utils.SHA256).hex(#sessionId)")
    public UserSessionPo getSessionBySessionId(String sessionId) throws BizException {

        var session = userSessionRepository.getSessionBySessionId(SHA256.hex(sessionId));

        if (session == null) {
            throw new BizException("会话不存在");
        }

        //使用当前数据库会话DV对比用户DV
        var uDv = userRepository.getDvByUserId(session.getUserId());
        var sDv = session.getDataVersion();

        //如果用户DV大于会话DV，说明会话过期,需要刷新该用户下本次会话
        if (!Objects.equals(uDv, sDv)) {
            return refreshSession(session);
        }

        return session;
    }

}
