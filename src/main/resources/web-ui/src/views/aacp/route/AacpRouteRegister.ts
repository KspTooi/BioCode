import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

export default class AacpRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "aacp",
        path: "agent-hub",
        name: "agent-hub",
        component: () => import("@/views/aacp/AacpAgentHub.vue"),
        meta: { breadcrumb: "智能体枢纽" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "cap",
        name: "cap",
        component: () => import("@/views/aacp/AacpCap.vue"),
        meta: { breadcrumb: "能力包" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "micro-func",
        name: "micro-func",
        component: () => import("@/views/aacp/AacpMicroFunc.vue"),
        meta: { breadcrumb: "微函数" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "online-session",
        name: "online-session",
        component: () => import("@/views/aacp/AacpOnlineSession.vue"),
        meta: { breadcrumb: "在线会话" },
      }),
    ];
  }
}
