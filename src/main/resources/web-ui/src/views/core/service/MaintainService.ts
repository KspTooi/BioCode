import { ref } from "vue";
import type { MaintainOperation } from "@/views/core/api/MaintainApi.ts";
import MaintainApi from "@/views/core/api/MaintainApi.ts";
import { Lock, User, UserFilled, Cpu, Menu as IconMenu, Upload, Tools } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import ComMenuService from "@/soa/com-series/service/ComMenuService";

export default {
  useMaintainOperation: () => {
    const globalLoading = ref(false);

    //先加载菜单服务
    const { loadMenus } = ComMenuService.useMenuService();

    const maintainOperations: MaintainOperation[] = [
      {
        title: "权限码同步",
        description: "自动扫描并同步系统最新的功能权限码，确保后台新增的功能可以在权限管理中看到。",
        icon: Lock,
        buttonText: "同步权限码",
        bgColor: "rgba(64, 158, 255, 0.1)",
        iconColor: "#409EFF",
        key: "permissions",
        warning:
          "危险操作警告：\n此操作将执行双向同步，可能会产生破坏性变更：\n1. 自动将代码中新增的权限写入数据库。\n2. **永久删除**数据库中已废弃的系统权限码。\n3. **强制解除**所有用户组与这些废弃权限的关联关系。\n\n请确保您了解此操作的后果，是否继续？",
        action: async () => await MaintainApi.validatePermissions(),
      },
      {
        title: "系统用户组修复",
        description: "检查并自动修复系统默认的用户组（如管理员组）。当管理员组权限丢失时可使用此功能修复。",
        icon: UserFilled,
        buttonText: "修复用户组",
        bgColor: "rgba(103, 194, 58, 0.1)",
        iconColor: "#67C23A",
        key: "groups",
        action: async () => await MaintainApi.validateGroups(),
      },
      {
        title: "系统账号修复",
        description: "检查并自动修复系统默认的账号（如 admin）。当内置账号误删或无法登录时可使用此功能修复。",
        icon: User,
        buttonText: "修复系统账号",
        bgColor: "rgba(230, 162, 60, 0.1)",
        iconColor: "#E6A23C",
        key: "users",
        action: async () => await MaintainApi.validateUsers(),
      },
      {
        title: "重置系统菜单",
        description: "将菜单导航恢复到出厂默认状态。用于解决菜单显示错乱或丢失的问题。",
        icon: IconMenu,
        buttonText: "重置菜单",
        bgColor: "rgba(245, 108, 108, 0.1)",
        iconColor: "#F56C6C",
        key: "menus",
        warning: "警告：此操作将把所有菜单恢复为出厂默认设置，您自定义的菜单调整都将丢失！是否确定要继续？",
        action: async () => await MaintainApi.resetMenus(),
        onComplete: () => {
          loadMenus();
        },
      },
      {
        title: "修复注册表",
        description: "检查系统内置注册表条目是否完整，自动补全缺失的配置项，不覆盖已有数据。",
        icon: Tools,
        buttonText: "修复注册表",
        bgColor: "rgba(0, 188, 212, 0.1)",
        iconColor: "#00bcd4",
        key: "registry",
        action: async () => await MaintainApi.repairRegistry(),
      },
      {
        title: "升级数据库",
        description: "执行数据库迁移脚本，将数据库表结构升级到最新版本。此操作会自动检测待执行的迁移脚本并依次执行。",
        icon: Upload,
        buttonText: "升级数据库",
        bgColor: "rgba(255, 152, 0, 0.1)",
        iconColor: "#ff9800",
        key: "database",
        warning:
          "危险操作警告：\n此操作将执行数据库迁移脚本，可能会产生以下影响：\n1. **修改数据库表结构**（新增/删除/修改表或字段）。\n2. **可能影响现有数据**（取决于具体迁移脚本内容）。\n3. 执行期间请**不要重启服务或中断操作**。\n\n**强烈建议：执行前先备份数据库！**\n\n是否确定要继续？",
        action: async () => await MaintainApi.upgradeDatabase(),
      },
    ];

    /**
     * 执行维护操作
     * @param operation 维护操作
     */
    const executeOperation = async (operation: MaintainOperation): Promise<void> => {
      // 如果正在执行其他操作，直接返回
      if (globalLoading.value) {
        return;
      }

      // 结果处理函数
      const handleResult = async (result: any): Promise<void> => {
        if (operation.onComplete) {
          operation.onComplete();
        }

        // 如果结果是 MaintainUpdateVo 类型，展示详细信息
        if (result && typeof result === "object" && "addedCount" in result) {
          const vo = result as any;
          const detailHtml = `
            <div style="font-size: 14px; line-height: 1.6; max-width: 400px;">
              ${
                vo.message
                  ? `
                <div style="margin-bottom: 12px; padding: 12px; background: #f4f4f5; border-left: 4px solid #909399; color: #606266; font-size: 13px; word-break: break-all;">
                  ${vo.message}
                </div>
              `
                  : '<p style="margin-bottom: 10px;">操作已完成，变动详情如下：</p>'
              }
              
              <div style="padding: 12px; background: #f8fafc; border-radius: 6px; border: 1px solid #ebeef5;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                  <span style="color: #606266;">新增条目：</span>
                  <span style="color: #67C23A; font-weight: bold;">${vo.addedCount ?? 0}</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                  <span style="color: #606266;">移除条目：</span>
                  <span style="color: #F56C6C; font-weight: bold;">${vo.removedCount ?? 0}</span>
                </div>
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: #606266;">当前存量：</span>
                  <span style="color: #409EFF; font-weight: bold;">${vo.existCount ?? "-"}</span>
                </div>
              </div>
            </div>
          `;

          await ElMessageBox.alert(detailHtml, "操作执行成功", {
            dangerouslyUseHTMLString: true,
            confirmButtonText: "我知道了",
            type: "success",
          });
          return;
        }

        await ElMessageBox.alert(String(result || "操作执行成功"), "操作执行成功", {
          confirmButtonText: "我知道了",
          type: "success",
        });
      };

      // 危险操作处理（带 Loading 的确认框）
      if (operation.warning) {
        const message = operation.warning.replace(/\n/g, "<br/>").replace(/\*\*(.*?)\*\*/g, "<b>$1</b>");

        try {
          await ElMessageBox.confirm(message, "危险操作确认", {
            confirmButtonText: "确定执行",
            cancelButtonText: "取消",
            type: "warning",
            dangerouslyUseHTMLString: true,
            beforeClose: async (action, instance, done) => {
              if (action !== "confirm") {
                done();
                return;
              }
              instance.confirmButtonLoading = true;
              instance.confirmButtonText = "执行中...";
              globalLoading.value = true;

              try {
                const result = await operation.action();
                done();
                // 延迟一小会儿确保弹窗关闭动画完成
                setTimeout(() => handleResult(result), 300);
              } catch (error: any) {
                done();
                ElMessage.error(error.message || "操作执行失败");
              } finally {
                instance.confirmButtonLoading = false;
                globalLoading.value = false;
              }
            },
          });
        } catch {
          // 用户取消
        }
        return;
      }

      // 普通操作处理
      try {
        globalLoading.value = true;
        const result = await operation.action();
        await handleResult(result);
      } catch (error: any) {
        ElMessage.error(error.message || "操作执行失败");
      } finally {
        globalLoading.value = false;
      }
    };

    return {
      loading: globalLoading,
      maintainOperations,
      executeOperation,
    };
  },
};
