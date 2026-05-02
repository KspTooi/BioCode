package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.core.common.AppRegistry;
import com.ksptool.bio.biz.core.model.maintain.vo.ExecuteInstallWizardVo;
import com.ksptool.bio.biz.core.model.maintain.vo.MaintainUpdateVo;
import com.ksptool.bio.biz.core.repository.MenuRepository;
import com.ksptool.bio.biz.core.service.MaintainService;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;

/**
 * 维护工具控制器
 */
@PrintLog
@Controller
@RestController
@RequestMapping("/maintain")
@Tag(name = "CORE-维护中心", description = "维护中心")
public class MaintainController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MaintainService maintainService;

    @Autowired
    private RegistrySdk regSdk;

    @Autowired
    private MenuRepository menuRepository;

    @PreAuthorize(value = "@auth.hasCode('maintain:validate:permissions')")
    @Operation(summary = "校验系统内置权限节点")
    @PostMapping("/validatePermissions")
    public Result<MaintainUpdateVo> validatePermissions() {
        return Result.success(maintainService.validatePermissions());
    }

    @Operation(summary = "用户体系冷启动")
    @PostMapping("/userSystemColdStartup")
    public Result<MaintainUpdateVo> userSystemColdStartup() throws BizException {
        return Result.success(maintainService.userSystemColdStartup());
    }


    @PreAuthorize(value = "@auth.hasCode('maintain:reset:menus')")
    @Operation(summary = "维护中心:重置菜单")
    @PostMapping("/resetMenus")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    public Result<MaintainUpdateVo> resetMenus() throws BizException {

        ClassPathResource sqlScript = new ClassPathResource("sql/default_menus_1_5Z25.sql");

        if (!sqlScript.exists()) {
            throw new BizException("SQL脚本文件 'sql/default_menus_1_5Z25.sql' 不存在。请检查文件是否已正确放置。");
        }

        try {
            menuRepository.clearMenu();
            // 使用JdbcTemplate执行回调，以确保使用的是当前事务的连接
            jdbcTemplate.execute((Connection connection) -> {
                ScriptUtils.executeSqlScript(connection, sqlScript);
                return null;
            });
            return Result.success("重置菜单成功", null);
        } catch (Exception e) {
            throw new RuntimeException("重置菜单失败: " + e.getMessage(), e);
        }
    }

    @PreAuthorize(value = "@auth.hasCode('maintain:upgrade:database')")
    @Operation(summary = "维护中心:升级数据库")
    @PostMapping("/upgradeDatabase")
    public Result<MaintainUpdateVo> upgradeDatabase() throws BizException {
        return Result.success(maintainService.upgradeDatabase());
    }

    @PreAuthorize(value = "@auth.hasCode('maintain:repair:registry')")
    @Operation(summary = "维护中心:修复注册表")
    @PostMapping("/repairRegistry")
    @Transactional(rollbackFor = Exception.class)
    public Result<MaintainUpdateVo> repairRegistry() throws BizException {
        return Result.success(maintainService.repairRegistry());
    }

    /**
     * 检查当前是否处于安装向导模式
     */
    @PreAuthorize(value = "@auth.hasCode('maintain:check:installWizardMode')")
    @Operation(summary = "维护中心:检查当前是否处于安装向导模式")
    @PostMapping("/checkInstallWizardMode")
    public Result<Integer> checkInstallWizardMode() {
        return Result.success("操作成功", regSdk.getInt(AppRegistry.CIW_ENABLED.getFullKey(), 0));
    }

    @PreAuthorize(value = "@auth.hasCode('maintain:execute:installWizard')")
    @Operation(summary = "维护中心:执行安装向导")
    @PostMapping("/executeInstallWizard")
    @Transactional(rollbackFor = Exception.class)
    public Result<ExecuteInstallWizardVo> executeInstallWizard() throws BizException {

        //清除全部注册表缓存
        regSdk.clearAllCache();

        //检查当前是否处于安装向导模式
        if (regSdk.getInt(AppRegistry.CIW_ENABLED.getFullKey(), 0) == 0) {
            throw new BizException("无法执行安装向导,因为当前未处于安装向导模式。");
        }

        return Result.success(maintainService.executeInstallWizard());
    }


}