package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.BioRunner;
import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import com.ksptool.bio.biz.auth.model.UserGroupPo;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.repository.GroupPermissionRepository;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.model.maintain.vo.ExecuteInstallWizardVo;
import com.ksptool.bio.biz.core.model.maintain.vo.MaintainUpdateVo;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.commons.enums.GroupEnum;
import com.ksptool.bio.commons.enums.UserEnum;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 维护中心服务
 * 提供维护中心所需的各项服务
 */
@Slf4j
@Service
public class MaintainService {

    @Autowired

    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping rmhm;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupPermissionRepository gpRepository;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Flyway flyway;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private RegistrySdk registrySdk;

    @Autowired
    private SessionService sessionService;

    /**
     * 校验系统内置权限节点
     * 检查数据库中是否存在所有系统内置权限码,如果缺失则自动创建
     *
     * @return 校验结果
     */
    @Transactional(rollbackFor = Exception.class)
    public MaintainUpdateVo validatePermissions() {

        // 扫描搜集系统中已定义的全部权限码
        Set<PermissionPo> scannedPermissions = new HashSet<>();

        var handlerMethods = rmhm.getHandlerMethods();

        for (var entry : handlerMethods.entrySet()) {

            var info = entry.getKey();
            var method = entry.getValue();

            // 提取Swagger注解
            var name = "未命名接口";
            var remark = "";

            if (method.hasMethodAnnotation(Operation.class)) {
                var operation = method.getMethodAnnotation(Operation.class);
                if (operation != null) {
                    name = operation.summary();
                    remark = operation.description();
                }
            }

            // 提取SpringSecurity注解上的权限码
            var permissionCode = "?";
            if (method.hasMethodAnnotation(PreAuthorize.class)) {

                var preAuthorize = method.getMethodAnnotation(PreAuthorize.class);

                if (preAuthorize != null) {

                    var val = preAuthorize.value().trim().replace(" ", "");

                    if (val.startsWith("@auth.hasCode('")) {
                        val = val.replace("@auth.hasCode('", "");
                    }

                    if (val.endsWith("')")) {
                        val = val.substring(0, val.length() - 2);
                    }

                    permissionCode = val.toLowerCase();
                }

            }

            // 如果权限码提取失败则不进行任何动作
            if (permissionCode.equals("?")) {
                log.warn("接口:{},方法:{} 未找到权限码，跳过处理", info.getDirectPaths(), method.getMethod().getName());
                continue;
            }

            // 构建一个权限码的PO
            var po = new PermissionPo();
            po.setCode(permissionCode);
            po.setName(name);
            po.setRemark(remark);
            po.setSeq(100);
            po.setIsSystem(1);
            scannedPermissions.add(po);
        }

        // 添加超级权限
        var superCode = new PermissionPo();
        superCode.setCode("*:*:*");
        superCode.setName("超级操作权限");
        superCode.setRemark("拥有此权限的用户组不受任何操作权限限制");
        superCode.setSeq(0);
        superCode.setIsSystem(1);
        scannedPermissions.add(superCode);

        var superRsCode = new PermissionPo();
        superRsCode.setCode("*:*:*:*");
        superRsCode.setName("超级数据权限(RS)");
        superRsCode.setRemark("拥有此权限的用户组不受任何数据权限限制");
        superRsCode.setSeq(0);
        superRsCode.setIsSystem(1);
        scannedPermissions.add(superRsCode);

        // 扫描数据库中已定义的全部权限码(这不包含那些用户自己定义的权限码 只获取系统权限码)
        Set<PermissionPo> existingPermissions = permissionRepository.getAllSystemPermissions();

        // 需要新增的权限码
        var addedPermissions = new HashSet<PermissionPo>();

        // 需要移除的权限码
        var removedPermissions = new HashSet<PermissionPo>();

        // 遍历远程权限码
        for (var existingPermission : existingPermissions) {

            // 远程有 本地无 需删除远程多余
            if (!scannedPermissions.contains(existingPermission)) {
                removedPermissions.add(existingPermission);
            }

        }

        // 遍历本地权限码
        for (var scannedPermission : scannedPermissions) {

            // 本地有 远程无 需补充远程
            if (!existingPermissions.contains(scannedPermission)) {
                addedPermissions.add(scannedPermission);
            }

        }

        // 执行变更 移除数据库中多余的权限码
        if (!removedPermissions.isEmpty()) {

            // 需要先移除这些权限码的全部关联关系
            var permissionIds = removedPermissions.stream().map(PermissionPo::getId).collect(Collectors.toList());
            gpRepository.clearGpByPermissionIds(permissionIds);

            // 然后删除这些权限码
            permissionRepository.deleteAllInBatch(removedPermissions);
        }

        // 执行变更操作 新增权限码
        if (!addedPermissions.isEmpty()) {
            permissionRepository.saveAll(addedPermissions);
        }

        // 构建响应Vo
        var vo = new MaintainUpdateVo();
        vo.setAddedCount(addedPermissions.size());
        vo.setRemovedCount(removedPermissions.size());
        vo.setAddedList(addedPermissions.stream().map(PermissionPo::getCode).collect(Collectors.toList()));
        vo.setRemovedList(removedPermissions.stream().map(PermissionPo::getCode).collect(Collectors.toList()));
        vo.setMessage("系统内置权限码校验完成");
        return vo;
    }

    /**
     * 校验系统内置用户
     * 检查数据库中是否存在所有系统内置用户，如果不存在则自动创建
     * 对于Admin用户，会赋予管理员组
     *
     * @return 校验结果消息
     */
    @Transactional(rollbackFor = Exception.class)
    public MaintainUpdateVo validateUsers() throws BizException {

        // 先获取超级组
        var superGroup = groupRepository.getGroupByCode(GroupEnum.ADMIN.getCode());

        if (superGroup == null) {
            throw new BizException("在校验系统内置用户时出现问题，超级组不存在,请检查系统内置用户组是否完整!");
        }

        // 获取所有系统内置用户枚举
        UserEnum[] userEnums = UserEnum.values();

        // 记录已存在和新增的用户数量
        int existCount = userRepository.countBySystemUser();
        List<String> addedUsers = new ArrayList<>();

        // 遍历所有系统内置用户
        for (UserEnum userEnum : userEnums) {
            String username = userEnum.getUsername();

            // 先查找数据库用户
            UserPo user = userRepository.getUserByUsername(username);

            // 数据库用户不存在 直接创建
            if (user == null) {
                user = new UserPo();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(username));
                user.setNickname(userEnum.getNickname());
                user.setGender(2);
                user.setLoginCount(0);
                user.setStatus(0);
                user.setIsSystem(1);

                //由系统创建的用户其创建者ID和修改者ID为-1
                user.setCreatorId(-1L);
                user.setUpdaterId(-1L);
                user = userRepository.save(user);
            }

            // 检查用户是否是管理员用户
            if (user.getUsername().equals(UserEnum.ADMIN.getUsername())) {

                // 如果管理员用户没有超级组关联 则赋予超级组
                var ug = ugRepository.getUgByUserIdAndGroupId(user.getId(), superGroup.getId());
                if (ug == null) {
                    var ugPo = new UserGroupPo();
                    ugPo.setUserId(user.getId());
                    ugPo.setGroupId(superGroup.getId());
                    ugRepository.save(ugPo);
                }
            }

            addedUsers.add(username);
        }

        // 构建响应Vo
        var vo = new MaintainUpdateVo();
        vo.setExistCount(existCount);
        vo.setAddedCount(addedUsers.size());
        vo.setAddedList(addedUsers);
        vo.setRemovedCount(0);
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("系统内置用户校验完成");
        return vo;
    }

    /**
     * 校验系统内置组
     * 检查数据库中是否存在所有系统内置组，如果不存在则自动创建
     * 对于管理员组，会赋予所有现有权限
     *
     * @return 校验结果消息
     */
    @Transactional(rollbackFor = Exception.class)
    public MaintainUpdateVo validateGroups() throws BizException {

        // 获取超级权限
        var superPermission = permissionRepository.getSuperPermission();

        if (superPermission == null) {
            throw new BizException("在校验系统内置组时出现问题，超级权限不存在,请检查系统内置权限码是否完整!");
        }

        // 获取所有系统内置组枚举
        GroupEnum[] groupEnums = GroupEnum.values();

        // 记录已存在和新增的组数量
        int existCount = groupRepository.countBySystemGroup();
        List<String> addedGroups = new ArrayList<>();

        // 遍历所有系统内置组
        for (GroupEnum groupEnum : groupEnums) {
            String code = groupEnum.getCode();

            // 先查找数据库组
            var group = groupRepository.getGroupByCode(code);

            // 数据库组不存在 直接创建
            if (group == null) {
                group = new GroupPo();
                group.setCode(code);
                group.setName(groupEnum.getName());
                group.setRemark(groupEnum.getName());
                group.setIsSystem(1);
                group.setSeq(groupRepository.findMaxSortOrder() + 1);
                group.setStatus(1);
                group = groupRepository.save(group);
            }

            // 检查组是否是管理员组
            if (group.getCode().equals(GroupEnum.ADMIN.getCode())) {

                // 如果管理员组没有超级权限关联 则赋予超级权限
                var gp = gpRepository.getGpByGroupIdAndPermissionId(group.getId(), superPermission.getId());

                if (gp == null) {
                    var gpPo = new GroupPermissionPo();
                    gpPo.setGroupId(group.getId());
                    gpPo.setPermissionId(superPermission.getId());
                    gpRepository.save(gpPo);
                }

            }

            addedGroups.add(code);
        }

        // 返回结果消息
        var vo = new MaintainUpdateVo();
        vo.setExistCount(existCount);
        vo.setAddedCount(addedGroups.size());
        vo.setAddedList(addedGroups);
        vo.setRemovedCount(0);
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("系统内置组校验完成");
        return vo;
    }

    /**
     * 升级数据库
     * 升级数据库到最新版本
     *
     * @return 升级结果
     */
    public MaintainUpdateVo upgradeDatabase() throws BizException {

        var vo = new MaintainUpdateVo();
        vo.setExistCount(0);
        vo.setAddedCount(0);
        vo.setRemovedCount(0);
        vo.setAddedList(new ArrayList<>());
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("");

        //先校验历史一致性
/*        try {
            flyway.validate();
        } catch (Exception e) {
            var applied = flyway.info().applied();
            vo.setExistCount(applied.length);
            vo.setMessage("[数据库升级] 当前数据库表结构与代码不一致，请先修复历史一致性，这可能是脚本执行后又被改过内容，或历史脚本被删/改名/移动了位置。");
            return vo;
        }*/

        var pending = flyway.info().pending();
        if (pending.length < 1) {
            var applied = flyway.info().applied();
            vo.setExistCount(applied.length);
            vo.setMessage("[数据库升级] 当前数据库表结构已经是最新版本，无需执行升级操作。");
            return vo;
        }

        // 迁移前收集待执行脚本信息，用于回显
        var pendingList = new ArrayList<String>();
        for (var info : pending) {

            var version = "";
            if (info.getVersion() != null) {
                version = info.getVersion().getVersion();
            }

            var description = info.getDescription();
            var script = info.getScript();

            var item = "";

            // 有版本号和描述 拼接展示
            if (StringUtils.isNotBlank(version) && StringUtils.isNotBlank(description)) {
                item = version + " - " + description;
            }

            // 只有版本号
            if (StringUtils.isBlank(item) && StringUtils.isNotBlank(version)) {
                item = version;
            }

            // 只有描述
            if (StringUtils.isBlank(item) && StringUtils.isNotBlank(description)) {
                item = description;
            }

            // 兜底用脚本文件名
            if (StringUtils.isBlank(item)) {
                item = script;
            }

            // 最终兜底
            if (StringUtils.isBlank(item)) {
                item = "未知迁移脚本";
            }

            pendingList.add(item);
        }

        try {
            flyway.migrate();
        } catch (Exception e) {
            log.error("[数据库升级] 执行升级失败", e);
            vo.setAddedCount(0);
            vo.setAddedList(new ArrayList<>());
            vo.setRemovedCount(0);
            vo.setRemovedList(new ArrayList<>());
            vo.setMessage("[数据库升级] 执行升级失败:" + e.getMessage());

            // 发送升级失败通知给操作人
            try {
                var session = SessionService.session();
                var uid = session.getUserId();

                var noticeContent = new StringBuilder();
                noticeContent.append("数据库升级执行失败！\n\n");
                noticeContent.append("失败原因: ").append(e.getMessage()).append("\n\n");
                noticeContent.append("待执行的迁移脚本:\n");

                for (var script : pendingList) {
                    noticeContent.append("- ").append(script).append("\n");
                }

                noticeContent.append("\n请检查迁移脚本是否正确，或联系技术人员处理。");

                noticeService.sendSystemNotice(uid, "数据库升级失败", "数据库升级", noticeContent.toString());
            } catch (Exception noticeEx) {
                log.warn("[数据库升级] 发送升级失败通知失败", noticeEx);
            }

            return vo;
        }

        var applied = flyway.info().applied();

        vo.setExistCount(applied.length);
        vo.setAddedCount(pendingList.size());
        vo.setAddedList(pendingList);
        vo.setRemovedCount(0);
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("[数据库升级] 执行升级完成，本次执行迁移脚本数量:" + pendingList.size());

        // 发送升级成功通知给操作人
        try {
            var session = SessionService.session();
            var uid = session.getUserId();

            var noticeContent = new StringBuilder();
            noticeContent.append("数据库升级已成功完成！\n\n");
            noticeContent.append("本次执行迁移脚本数量: ").append(pendingList.size()).append("\n");
            noticeContent.append("当前数据库版本迁移总数: ").append(applied.length).append("\n\n");
            noticeContent.append("执行的迁移脚本:\n");

            for (var script : pendingList) {
                noticeContent.append("- ").append(script).append("\n");
            }

            noticeService.sendSystemNotice(uid, "数据库升级完成", "数据库升级", noticeContent.toString());
        } catch (Exception e) {
            log.warn("[数据库升级] 发送升级通知失败", e);
        }

        return vo;
    }

    /**
     * 修复注册表
     * 遍历SystemRegistry枚举,对数据库中缺失的条目自动创建
     *
     * @return 修复结果
     */
    @Transactional(rollbackFor = Exception.class)
    public MaintainUpdateVo repairRegistry() {

        var addedList = new ArrayList<String>();

        for (AppRegistry item : AppRegistry.values()) {

            String nodeKeyPath = item.getNodeKeyPath();
            String nkey = item.getNkey();
            String defaultValue = item.getValue();
            String label = item.getLabel();
            AppRegistry.NvalueKind kind = item.getNvalueKind();

            boolean created = false;

            if (kind == AppRegistry.NvalueKind.STRING) {
                created = registrySdk.createStringEntry(nodeKeyPath, nkey, defaultValue, label);
            }

            if (kind == AppRegistry.NvalueKind.INTEGER) {
                created = registrySdk.createIntEntry(nodeKeyPath, nkey, Integer.parseInt(defaultValue), label);
            }

            if (kind == AppRegistry.NvalueKind.DOUBLE) {
                created = registrySdk.createDoubleEntry(nodeKeyPath, nkey, Double.parseDouble(defaultValue), label);
            }

            if (kind == AppRegistry.NvalueKind.DATETIME) {
                created = registrySdk.createDateTimeEntry(nodeKeyPath, nkey, LocalDateTime.parse(defaultValue), label);
            }

            if (created) {
                addedList.add(item.getFullKey());
            }
        }

        var vo = new MaintainUpdateVo();
        vo.setExistCount(AppRegistry.values().length - addedList.size());
        vo.setAddedCount(addedList.size());
        vo.setAddedList(addedList);
        vo.setRemovedCount(0);
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("注册表修复完成,新增条目:" + addedList.size() + " 条");
        return vo;
    }

    /**
     * 执行安装向导
     * 依次执行: 修复注册表 → 升级数据库 → 校验权限 → 校验用户组 → 校验用户
     * 全部完成后自动关闭向导模式
     */
    @Transactional(rollbackFor = Exception.class)
    public ExecuteInstallWizardVo executeInstallWizard() throws BizException {

        var changesContent = new ArrayList<String>();

        // 读取执行前的系统版本
        String oldVersion = registrySdk.getString(AppRegistry.CM_VERSION.getFullKey(), "未知");

        // Step1: 修复注册表 — 确保所有内置配置条目存在
        var registryResult = repairRegistry();
        changesContent.add("[修复注册表] " + registryResult.getMessage());

        // Step2: 升级数据库 — 执行 Flyway 迁移脚本
        var dbResult = upgradeDatabase();
        changesContent.add("[升级数据库] " + dbResult.getMessage());

        // Step3: 校验系统内置权限码
        var permResult = validatePermissions();
        changesContent.add("[权限码同步] 新增 " + permResult.getAddedCount() + " 条，移除 " + permResult.getRemovedCount() + " 条");

        // Step4: 校验系统内置用户组
        var groupResult = validateGroups();
        changesContent.add("[用户组修复] " + groupResult.getMessage());

        // Step5: 校验系统内置用户
        var userResult = validateUsers();
        changesContent.add("[账号修复] " + userResult.getMessage());

        registrySdk.clearAllCache();

        //写入新系统版本到注册表
        String newVersion = BioRunner.getVersion().toString();
        registrySdk.setString(AppRegistry.CM_VERSION.getFullKey(), newVersion);

        //关闭向导模式
        registrySdk.setInt(AppRegistry.CIW_ENABLED.getFullKey(), 0);

        //清除所有用户登录状态
        sessionService.clearUserSession();

        var vo = new ExecuteInstallWizardVo();
        vo.setOldVersion(oldVersion);
        vo.setNewVersion(newVersion);
        vo.setChangesContent(changesContent);
        return vo;
    }

}
