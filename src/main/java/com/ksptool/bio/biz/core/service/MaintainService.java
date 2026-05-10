package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.BioRunner;
import com.ksptool.bio.biz.auth.common.CheatPermission;
import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import com.ksptool.bio.biz.auth.model.UserGroupPo;
import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.repository.GroupPermissionRepository;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.common.SuperEntities;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.model.maintain.vo.ExecuteInstallWizardVo;
import com.ksptool.bio.biz.core.model.maintain.vo.MaintainUpdateVo;
import com.ksptool.bio.biz.core.repository.CoreRootRepository;
import com.ksptool.bio.biz.core.repository.MaintainRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 维护中心服务
 * 提供维护中心所需的各项服务
 *
 * @author KspTooi
 * @since 1.2.7(G).37
 */
@Slf4j
@Service
public class MaintainService {

    @Autowired

    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping rmhm;

    @Autowired
    private GroupRepository gRepository;

    @Autowired
    private GroupPermissionRepository gpRepository;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private PermissionRepository pRepository;

    @Autowired
    private UserRepository uRepository;

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

    @Autowired
    private CoreRootRepository rRepository;

    @Autowired
    private MaintainRepository mRepository;

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
            po.setIsSystem(Switch.yes());
            scannedPermissions.add(po);
        }

        // 添加超级权限(以CheatPermission为准)
        for (var cp : CheatPermission.values()) {
            var po = new PermissionPo();
            po.setCode(cp.getCode());
            po.setName(cp.getName());
            po.setRemark(cp.getRemark());
            po.setSeq(-1);
            po.setIsSystem(Switch.yes());
            scannedPermissions.add(po);
        }

        // 扫描数据库中已定义的全部权限码(这不包含那些用户自己定义的权限码 只获取系统权限码)
        Set<PermissionPo> existingPermissions = pRepository.getAllSystemPermissions();

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
            pRepository.deleteAllInBatch(removedPermissions);
        }

        // 执行变更操作 新增权限码
        if (!addedPermissions.isEmpty()) {
            pRepository.saveAll(addedPermissions);
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
     * 用户体系冷启动
     * 检查数据库中是否存在所有系统内置用户，如果不存在则自动创建
     * 对于Admin用户，会赋予管理员组
     *
     * @return 校验结果消息
     */
    @Transactional(rollbackFor = Exception.class)
    public MaintainUpdateVo userSystemColdStartup() throws BizException {

        var sRootId = SuperEntities.ROOT.getId();
        var sUserId = SuperEntities.USER.getId();
        var sGroupId = SuperEntities.GROUP.getId();
        var spCode = CheatPermission.SA.getCode();
        var srCode = CheatPermission.SR.getCode();

        //获取超级租户
        var superRoot = rRepository.findById(sRootId).orElse(null);

        //获取超级用户
        var superUser = uRepository.findById(sUserId).orElse(null);

        //获取超级组
        var superGroup = gRepository.findById(sGroupId).orElse(null);

        //获取超级权限
        var superPermission = pRepository.getByCode(spCode);

        //获取超级数据权限
        var superRsPermission = pRepository.getByCode(srCode);

        var actions = 0;
        var addedList = new ArrayList<String>();

        //如果系统默认租户不存在 则创建
        if (superRoot == null) {
            actions += mRepository.createDefaultRoot(sRootId);
            superRoot = rRepository.findById(sRootId).orElse(null);
            addedList.add("创建 超级租户(id=" + sRootId + ")");
        }

        //如果系统默认用户不存在 则创建
        if (superUser == null) {
            actions += mRepository.createDefaultUser(sUserId, sRootId, "admin", passwordEncoder.encode("admin"));
            superUser = uRepository.findById(sUserId).orElse(null);
            addedList.add("创建 超级用户(id=" + sUserId + ")");
        }

        //如果超级组不存在 则先创建超级组
        if (superGroup == null) {
            actions += mRepository.createDefaultGroup(sGroupId, sRootId, "admin", "超级组", "超级组是系统内置的组，拥有至高无上的权限且不受任何限制。");
            superGroup = gRepository.findById(sGroupId).orElse(null);
            addedList.add("创建 超级组(id=" + sGroupId + ")");
        }

        //如果超级权限不存在 则创建超级权限
        if (superPermission == null) {
            var p = new PermissionPo();
            p.setCode(spCode);
            p.setName("超级操作权限(SA)");
            p.setRemark("拥有此权限的用户组不受任何操作权限限制。");
            p.setSeq(0);
            p.setIsSystem(Switch.yes());
            p.setCreatorId(SuperEntities.USER.getId());
            p.setUpdaterId(SuperEntities.USER.getId());
            superPermission = pRepository.save(p);
            addedList.add("创建 超级操作权限 (id=" + superPermission.getId() + ")");
        }

        //如果超级数据权限不存在 则创建超级数据权限
        if (superRsPermission == null) {
            var p = new PermissionPo();
            p.setCode(srCode);
            p.setName("超级数据权限(SR)");
            p.setRemark("拥有此权限的用户组不受任何数据权限限制。");
            p.setSeq(0);
            p.setIsSystem(Switch.yes());
            p.setCreatorId(SuperEntities.USER.getId());
            p.setUpdaterId(SuperEntities.USER.getId());
            superRsPermission = pRepository.save(p);
            addedList.add("创建 超级数据权限 (id=" + superRsPermission.getId() + ")");
        }

        //检查超级组是否关联了超级操作权限和超级数据权限
        var saGp = gpRepository.getGpByGroupIdAndPermissionId(sGroupId, superPermission.getId());
        var srGp = gpRepository.getGpByGroupIdAndPermissionId(sGroupId, superRsPermission.getId());

        var gps = new ArrayList<GroupPermissionPo>();

        if (saGp == null) {
            var gp = new GroupPermissionPo();
            gp.setGroupId(sGroupId);
            gp.setPermissionId(superPermission.getId());
            gps.add(gp);
            addedList.add("超级组 连接到 超级操作权限");
        }

        if (srGp == null) {
            var gp = new GroupPermissionPo();
            gp.setGroupId(sGroupId);
            gp.setPermissionId(superRsPermission.getId());
            gps.add(gp);
            addedList.add("超级组 连接到 超级数据权限");
        }

        //保存这些GP关系
        if (!gps.isEmpty()) {
            gpRepository.saveAll(gps);
        }

        //检查超级用户是否关联超级组
        var ug = ugRepository.getUgByUserIdAndGroupId(sUserId, sGroupId);
        if (ug == null) {
            var ugPo = new UserGroupPo();
            ugPo.setUserId(sUserId);
            ugPo.setGroupId(sGroupId);
            ugRepository.save(ugPo);
            addedList.add("超级用户 连接到 超级组");
        }

        //重新初始化超级用户、超级组的归属(以防止租户数据错误)
        if (superUser != null) {
            superUser.setRootId(sRootId);
            superUser.setStatus(Switch.on());
            superUser.setIsSystem(Switch.yes());
            uRepository.save(superUser);
        }
        if (superGroup != null) {
            superGroup.setRootId(sRootId);
            superGroup.setOrgId(null);
            superGroup.setStatus(Switch.on());
            superGroup.setIsSystem(Switch.yes());
            gRepository.save(superGroup);
        }

        //超级租户的管理员账户、管理角色指向超级用户和超级组
        if (superRoot != null) {
            superRoot.setAdminUserId(sUserId);
            superRoot.setAdminGroupId(sGroupId);
            superRoot.setIsSystem(Switch.yes());
            rRepository.save(superRoot);
        }

        var vo = new MaintainUpdateVo();
        vo.setExistCount(0);
        vo.setAddedCount(actions);
        vo.setAddedList(addedList);
        vo.setRemovedCount(0);
        vo.setRemovedList(new ArrayList<>());
        vo.setMessage("用户体系冷启动完成");
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

        // Step4: 冷启动用户体系
        var userSystemResult = userSystemColdStartup();
        changesContent.add("[用户体系冷启动] " + userSystemResult.getMessage());

        // Step5: 清除所有注册表缓存
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
