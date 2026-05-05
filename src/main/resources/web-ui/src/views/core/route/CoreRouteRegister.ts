import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

export default class CoreRouteRegister extends GenricRouteRegister {
  /**
   * 注册核心管理路由
   * @returns 路由条目数组
   */
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "core",
        path: "user-manager",
        name: "user-manager",
        component: () => import("@/views/core/UserManager.vue"),
        meta: { breadcrumb: "用户列表" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "org-manager",
        name: "org-manager",
        component: () => import("@/views/core/OrgManager.vue"),
        meta: { breadcrumb: "组织机构管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "group-manager",
        name: "group-manager",
        component: () => import("@/views/auth/GroupManager.vue"),
        meta: { breadcrumb: "用户组" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "permission-manager",
        name: "permission-manager",
        component: () => import("@/views/auth/PermissionManager.vue"),
        meta: { breadcrumb: "权限管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "session-manager",
        name: "session-manager",
        component: () => import("@/views/auth/SessionManager.vue"),
        meta: { breadcrumb: "会话管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "application-maintain",
        name: "application-maintain",
        component: () => import("@/views/core/MaintainManager.vue"),
        meta: { breadcrumb: "维护中心" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "menu-manager",
        name: "menu-manager",
        component: () => import("@/views/core/MenuManager.vue"),
        meta: { breadcrumb: "菜单管理", keepAlive: false },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "excel-template-manager",
        name: "excel-template-manager",
        component: () => import("@/views/core/ExcelTemplateManager.vue"),
        meta: { breadcrumb: "Excel模板管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "notice",
        name: "notice",
        component: () => import("@/views/core/NoticeManager.vue"),
        meta: { breadcrumb: "消息管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "notice-template",
        name: "notice-template",
        component: () => import("@/views/core/NoticeTemplate.vue"),
        meta: { breadcrumb: "通知模板" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "registry-manager",
        name: "registry-manager",
        component: () => import("@/views/core/RegistryManager.vue"),
        meta: { breadcrumb: "注册表管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "notice-rcd",
        name: "notice-rcd",
        component: () => import("@/views/core/NoticeRcdManager.vue"),
        meta: { breadcrumb: "个人消息中心" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "post-manager",
        name: "post-manager",
        component: () => import("@/views/core/Post.vue"),
        meta: { breadcrumb: "岗位管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "app-status",
        name: "app-status",
        component: () => import("@/views/core/AppStatus.vue"),
        meta: { breadcrumb: "系统探针" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "core-root",
        name: "core-root",
        component: () => import("@/views/core/CoreRoot.vue"),
        meta: { breadcrumb: "租户管理" },
      }),
      RouteEntryPo.build({
        biz: "core",
        path: "package",
        name: "package",
        component: () => import("@/views/core/PackManager.vue"),
        meta: { breadcrumb: "菜单包" },
      }),
    ];
  }
}
