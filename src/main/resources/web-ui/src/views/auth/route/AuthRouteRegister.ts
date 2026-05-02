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
        component: () => import("@/views/auth/UserLogin.vue"),
        meta: { breadcrumb: "用户登录", layout: "blank" },
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
