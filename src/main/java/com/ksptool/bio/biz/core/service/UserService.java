package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.UserGroupPo;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.auth.repository.UserSessionRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.model.user.dto.*;
import com.ksptool.bio.biz.core.model.user.vo.GetUserDetailsVo;
import com.ksptool.bio.biz.core.model.user.vo.GetUserListVo;
import com.ksptool.bio.biz.core.model.user.vo.UserGroupVo;
import com.ksptool.bio.biz.core.model.user.vo.UserPermissionVo;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.commons.dataprocess.Str;
import jakarta.persistence.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class UserService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserSessionRepository userSessionRepository;

    /**
     * 获取用户列表
     *
     * @param dto 获取用户列表DTO
     * @return 用户列表VO
     */
    public PageResult<GetUserListVo> getUserList(GetUserListDto dto) {
        Page<Tuple> page = userRepository.getUserList(dto, dto.pageRequest());
        return PageResult.success(tupleAs(page.getContent(), GetUserListVo.class), page.getTotalElements());
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情VO
     * @throws BizException 用户不存在
     */
    public GetUserDetailsVo getUserDetails(long id) throws BizException {

        UserPo user = userRepository.findById(id).orElseThrow(() -> new BizException("用户不存在"));

        GetUserDetailsVo vo = new GetUserDetailsVo();
        assign(user, vo);

        // 获取用户组信息
        List<UserGroupVo> groupVos = new ArrayList<>();
        List<GroupPo> allGroups = groupRepository.findAll(Sort.by(Sort.Direction.ASC, "seq"));

        //获取该用户拥有的组IDS
        var userGroupIds = ugRepository.getGroupIdsByGrantedUserId(id);

        //处理用户组信息
        for (GroupPo group : allGroups) {
            UserGroupVo groupVo = new UserGroupVo();
            assign(group, groupVo);
            groupVo.setHasGroup(userGroupIds.contains(group.getId()));//是否属于该组
            groupVos.add(groupVo);
        }
        vo.setGroups(groupVos);

        // 获取用户权限信息
        List<PermissionPo> userPermissions = userRepository.getUserPermissions(id);
        vo.setPermissions(as(userPermissions, UserPermissionVo.class));

        //处理组织架构
        vo.setOrgId(user.getMinOrgId());
        return vo;
    }

    /**
     * 新增用户
     * 新增用户时，用户默认不是系统内置用户
     *
     * @param dto 新增用户DTO
     * @throws BizException 用户名已存在或无法新增用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(AddUserDto dto) throws BizException {

        if (StringUtils.isBlank(dto.getUsername())) {
            throw new BizException("用户名不能为空");
        }

        if (StringUtils.isBlank(dto.getPassword())) {
            throw new BizException("新建用户时密码不能为空");
        }

        if (userRepository.countByUsername(dto.getUsername()) > 0) {
            throw new BizException("用户名 '" + dto.getUsername() + "' 已被使用");
        }

        UserPo user = new UserPo();
        assign(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setIsSystem(Switch.no());

        //处理组织架构
        if (dto.getOrgId() != null) {

            //查询组织架构
            OrgPo org = orgRepository.findById(dto.getOrgId())
                    .orElseThrow(() -> new BizException("无法找到组织架构: " + dto.getOrgId()));

            //处理组织架构是公司或子公司的情况 
            if (org.isCompany() || org.isSubCompany()) {
                user.setOrgId(org.getId());
                user.setDeptId(null);
            }

            //处理组织架构是部门的情况
            if (org.isDepartment()) {
                user.setOrgId(org.getOrgId()); //直属企业ID
                user.setDeptId(org.getId());
            }

        }

        //清空组织架构
        if (dto.getOrgId() == null) {
            user.setOrgId(null);
            user.setDeptId(null);
        }

        //保存用户
        UserPo save = userRepository.save(user);

        //处理用户组
        var userGroupPos = new ArrayList<UserGroupPo>();

        if (!dto.getGroupIds().isEmpty()) {

            //查询启用的用户组IDS
            var enabledGroupIds = groupRepository.getUserGroupByIds(dto.getGroupIds(), 1);

            //至少有一个用户组是启用且有效的，将它们分配给用户
            if (!enabledGroupIds.isEmpty()) {

                for (var gId : enabledGroupIds) {
                    var userGroupPo = new UserGroupPo();
                    userGroupPo.setUserId(save.getId());
                    userGroupPo.setGroupId(gId);
                    userGroupPos.add(userGroupPo);
                }

            }

        }

        //保存用户组关联
        if (!userGroupPos.isEmpty()) {
            ugRepository.saveAll(userGroupPos);
        }

    }

    /**
     * 编辑用户
     *
     * @param dto 编辑用户DTO
     * @throws BizException 用户不存在或无法编辑系统内置用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void editUser(EditUserDto dto) throws Exception {

        UserPo user = userRepository.findById(dto.getId()).orElseThrow(() -> new BizException("用户不存在"));

        if (StringUtils.isNotBlank(dto.getPassword())) {
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (StringUtils.isBlank(dto.getPassword())) {
            dto.setPassword(user.getPassword());
        }

        //处理系统内置用户的更新逻辑
        if(user.isSystem()){

            //内置用户不能改组
            var gIds = ugRepository.getGroupIdsByGrantedUserId(user.getId());
            var newGIds = dto.getGroupIds() == null ? List.of() : dto.getGroupIds();

            if (!new HashSet<>(gIds).equals(new HashSet<>(newGIds))) {
                throw new BizException("内置用户不允许修改用户组！");
            }

            //内置用户不允许封禁
            if (dto.getStatus() != null && dto.getStatus() == Switch.off()) {
                throw new BizException("内置用户不允许封禁！");
            }

            //内置用户不能通过编辑用户来强制改密
            if (StringUtils.isNotBlank(dto.getPassword())) {
                throw new BizException("内置用户不允许通过编辑用户来强制改密！");
            }

        }


        //合并同类项
        assign(dto, user);

        //处理组织架构
        if (dto.getOrgId() != null) {

            //查询组织架构
            OrgPo org = orgRepository.findById(dto.getOrgId())
                    .orElseThrow(() -> new BizException("无法找到组织架构: " + dto.getOrgId()));

            //处理组织架构是公司或子公司的情况 
            if (org.isCompany() || org.isSubCompany()) {
                user.setOrgId(org.getId());
                user.setDeptId(null);
            }

            //处理组织架构是部门的情况
            if (org.isDepartment()) {
                user.setOrgId(org.getOrgId()); //直属企业ID
                user.setDeptId(org.getId());
            }

        }

        //清空组织架构
        if (dto.getOrgId() == null) {
            user.setOrgId(null);
            user.setDeptId(null);
        }

        //处理用户组 先清除该用户的全部用户组关联
        ugRepository.clearGroupGrantedByUserId(user.getId());

        var userGroupPos = new ArrayList<UserGroupPo>();
        if (!dto.getGroupIds().isEmpty()) {

            //查询启用的用户组IDS
            var enabledGroupIds = groupRepository.getUserGroupByIds(dto.getGroupIds(), 1);

            if (!enabledGroupIds.isEmpty()) {
                for (var gId : enabledGroupIds) {
                    var userGroupPo = new UserGroupPo();
                    userGroupPo.setUserId(user.getId());
                    userGroupPo.setGroupId(gId);
                    userGroupPos.add(userGroupPo);
                }
            }

        }

        //保存用户
        userRepository.save(user);

        //保存用户组关联
        if (!userGroupPos.isEmpty()) {
            ugRepository.saveAll(userGroupPos);
        }
    }

    /**
     * 移除用户
     * 如果用户是系统内置用户，则无法移除
     *
     * @param id 用户ID
     * @throws BizException 用户不存在或无法移除系统内置用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(long id) throws BizException {

        var userPo = userRepository.findById(id).orElseThrow(() -> new BizException("用户不存在"));

        if (userPo.getIsSystem() == 1) {
            throw new BizException("无法移除系统内置用户:" + userPo.getUsername());
        }

        userRepository.delete(userPo);
    }


    public UserPo getUser(String username) throws BizException {
        UserPo user = userRepository.getUserByUsername(username);
        if (user == null) {
            throw new BizException("该用户不存在");
        }
        return user;
    }

    public UserPo register(String username, String password) throws BizException {
        // 检查是否已存在同名用户
        if (userRepository.getUserByUsername(username) != null) {
            throw new BizException("用户名已存在");
        }

        UserPo newUser = new UserPo();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setGender(2);
        newUser.setIsSystem(0);

        return userRepository.save(newUser);
    }

    public UserPo registerSystemUser(String username, String password) throws BizException {
        // 检查是否已存在同名用户
        if (userRepository.getUserByUsername(username) != null) {
            throw new BizException("用户名已存在");
        }

        UserPo newUser = new UserPo();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setGender(2);
        newUser.setIsSystem(1);

        return userRepository.save(newUser);
    }


    /**
     * 导入用户
     *
     * @param data 导入用户数据
     * @return 导入用户数量
     * @throws BizException 导入用户失败
     */
    public int importUser(List<ImportUserDto> data) throws BizException {

        var addPos = new ArrayList<UserPo>();

        //搜集所有用户名
        var usernameSet = new HashSet<String>();
        for (ImportUserDto dto : data) {
            usernameSet.add(dto.getUsername());
        }

        //搜集所有被占用的用户名
        var occupiedNames = userRepository.getUsernameSetByUsernames(new ArrayList<>(usernameSet));

        if (occupiedNames.size() > 0) {
            throw new BizException("以下用户名已被占用: " + String.join(",", occupiedNames));
        }

        for (ImportUserDto dto : data) {

            UserPo user = new UserPo();
            user.setUsername(dto.getUsername());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());

            //处理性别 0:男 1:女 2:不愿透露
            if (Str.isNotBlank(dto.getGender())) {

                var genderStr = dto.getGender();

                if (genderStr.equals("男")) {
                    user.setGender(0);
                }

                if (genderStr.equals("女")) {
                    user.setGender(1);
                }

                if (genderStr.equals("不愿透露")) {
                    user.setGender(2);
                }

            }

            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setLoginCount(0);
            user.setStatus(Switch.on());//正常

            //处理所属企业(如果有)
            if (Str.isNotBlank(dto.getRootName())) {
                var rootName = dto.getRootName();
                var root = orgRepository.getRootByName(rootName);
                if (root == null) {
                    throw new BizException("所属企业:" + rootName + "不存在");
                }
                user.setOrgId(root.getId());
            }

            //处理所属部门(如果有)
            if (Str.isNotBlank(dto.getDeptName())) {
                var deptName = dto.getDeptName();

                try {
                    var dept = orgRepository.getDeptByName(deptName, user.getRootId());

                    if (dept == null) {
                        throw new BizException("所属部门:" + deptName + "不存在或不属于指定企业");
                    }
                    user.setDeptId(dept.getId());

                } catch (IncorrectResultSizeDataAccessException e) {
                    throw new BizException("公司:" + user.getOrgId() + "下存在多个同名称部门:" + deptName + "，不支持批量导入");
                }

            }

            //2025-04-12 旧式Company彻底移除
            //user.setActiveCompany(null);
            user.setAvatarAttach(null);
            user.setIsSystem(0);//0:否 1:是
            addPos.add(user);
        }

        //批量新增用户
        userRepository.saveAll(addPos);
        return addPos.size();
    }

    /**
     * 批量编辑用户
     *
     * @param dto 批量编辑用户DTO
     * @throws BizException 批量编辑用户失败
     */
    public int batchEditUser(BatchEditUserDto dto) throws BizException {

        var userIds = dto.getIds();
        var userPos = userRepository.findAllById(userIds);

        if (userPos.size() != userIds.size()) {
            throw new BizException("部分用户不存在");
        }

        //处理批量解封
        if (dto.getKind() == 0) {
            for (UserPo user : userPos) {
                user.setStatus(Switch.on());
            }
            userRepository.saveAll(userPos);
            return userPos.size();
        }

        //批量封禁
        if (dto.getKind() == 1) {
            for (UserPo user : userPos) {

                //跳过内置用户
                if (user.isSystem()) {
                    continue;
                }

                user.setStatus(Switch.off());
            }
            userRepository.saveAll(userPos);
            return userPos.size();
        }

        //批量删除
        if (dto.getKind() == 2) {

            //直接剔除内置用户
            userPos = userPos.stream().filter(user -> !user.isSystem()).collect(Collectors.toList());

            //批量删除用户
            userRepository.deleteAll(userPos);

            //销毁被删除用户的session(如果有) 强行踢他们下线
            for (UserPo user : userPos) {
                sessionService.closeSession(user.getId());
            }

            return userPos.size();
        }


        //批量变更部门
        if (dto.getKind() == 3) {

            var org = orgRepository.findById(dto.getOrgId())
                    .orElseThrow(() -> new BizException("无法找到组织架构: " + dto.getOrgId()));

            //处理组织架构是公司或子公司的情况 
            if (org.isCompany() || org.isSubCompany()) {
                for (UserPo user : userPos) {
                    user.setOrgId(org.getId());
                    user.setDeptId(null);
                }
            }


            //处理组织架构是部门的情况
            if (org.isDepartment()) {
                for (UserPo user : userPos) {
                    user.setOrgId(org.getOrgId()); //直属企业ID
                    user.setDeptId(org.getId());
                }
            }

            //批量更新用户
            userRepository.saveAll(userPos);
            return userPos.size();
        }

        throw new BizException("不支持的批量操作类型: " + dto.getKind() + "，请检查kind参数");
    }

    /**
     * 为指定用户列表的在线用户加版本
     * 当用户信息发生变更时，需要增加数据版本，这会使得用户会话失效，并在下次请求时刷新会话
     *
     * @param userIds 用户ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseDv(List<Long> userIds) {

        //只对那些当前在线的用户增加数据版本以缩小缓存更新范围
        var onlineUserIds = userSessionRepository.getOnlineUserIdsByUserIds(userIds);

        if (onlineUserIds.isEmpty()) {
            return;
        }

        //先增加数据版本
        userRepository.increaseDv(onlineUserIds);

        var cache = cacheManager.getCache("userSession");

        //如果系统使用了缓存，需向缓存添加一条数据,用于标识该用户数据版本已变更
        if (cache != null) {
            for (var userId : onlineUserIds) {
                cache.put("user_dv_changed_" + userId, "0");
            }
        }

    }

    /**
     * 为该企业/租户下的全部在线用户加版本
     *
     * @param rootId 企业/租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseDvByRootId(Long rootId) {

        //获取该企业/租户下的全部用户ID
        var onlineUserIds = userRepository.getOnlineUserIdsByRootId(rootId);

        if (onlineUserIds.isEmpty()) {
            return;
        }

        //先增加数据版本
        userRepository.increaseDv(onlineUserIds);

        var cache = cacheManager.getCache("userSession");

        //如果系统使用了缓存，需向缓存添加一条数据,用于标识该用户数据版本已变更
        if (cache != null) {
            for (var userId : onlineUserIds) {
                cache.put("user_dv_changed_" + userId, "0");
            }
        }

    }

    /**
     * 为拥有该组的全部在线用户加版本
     *
     * @param groupId 组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseDvByGroupId(Long groupId) {

        //获取拥有该组的用户ID列表
        var userIds = ugRepository.getUserIdsByGroupId(groupId);

        if (userIds.isEmpty()) {
            return;
        }

        increaseDv(userIds);
    }

    /**
     * 为拥有该权限的全部在线用户加版本
     *
     * @param permissionId 权限ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseDvByPermissionId(Long permissionId) {

        //获取拥有该权限的用户ID列表
        var onlineUserIds = userRepository.getOnlineUserIdsByPermissionId(permissionId);

        if (onlineUserIds.isEmpty()) {
            return;
        }

        increaseDv(onlineUserIds);
    }

}
