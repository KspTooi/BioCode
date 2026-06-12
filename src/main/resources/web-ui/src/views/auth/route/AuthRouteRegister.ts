import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";
import type { NavigationGuardWithThis } from "vue-router";
import UserAuthService from "@/views/auth/service/UserAuthService";

export default class AuthRouteRegister extends GenricRouteRegister {
  /**
   * 注册认证相关路由
   * @returns 路由条目数组
   */
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "auth",
        path: "login",
        name: "login",
        component: () => import("@/soa/com-series/ComAuthProvider.vue"),
        meta: { breadcrumb: "用户登录", layout: "blank" },
      }),
      RouteEntryPo.build({
        biz: "auth",
        path: "group-manager",
        name: "group-manager",
        component: () => import("@/views/auth/GroupManager.vue"),
        meta: { breadcrumb: "用户组" },
      }),
      RouteEntryPo.build({
        biz: "auth",
        path: "permission-manager",
        name: "permission-manager",
        component: () => import("@/views/auth/PermissionManager.vue"),
        meta: { breadcrumb: "权限管理" },
      }),
      RouteEntryPo.build({
        biz: "auth",
        path: "session-manager",
        name: "session-manager",
        component: () => import("@/views/auth/SessionManager.vue"),
        meta: { breadcrumb: "会话管理" },
      }),
      RouteEntryPo.build({
        biz: "auth",
        path: "basic-pat",
        name: "basic-pat",
        component: () => import("@/views/auth/basicpat/BasicPat.vue"),
        meta: { breadcrumb: "基本PAT" },
      }),
    ];
  }

  public override doBeforeEach(): NavigationGuardWithThis<undefined> {
    const authStore = UserAuthService.AuthStore();

    return (to, from) => {
      //如果访问了login 且用户已登录 则跳转到首页
      if (to.name === "login" && authStore.getSessionId) {
        console.log(authStore.getSessionId);
        return "/";
      }
    };
  }
}
