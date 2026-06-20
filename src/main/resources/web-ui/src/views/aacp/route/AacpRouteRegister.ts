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
      RouteEntryPo.build({
        biz: "aacp",
        path: "datasource",
        name: "datasource",
        component: () => import("@/views/aacp/AacpDatasource.vue"),
        meta: { breadcrumb: "数据源" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "provider",
        name: "provider",
        component: () => import("@/views/aacp/Provider.vue"),
        meta: { breadcrumb: "模型供应商" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "appLogs",
        name: "appLogs",
        component: () => import("@/views/aacp/AppLogs.vue"),
        meta: { breadcrumb: "模型调用记录" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "aacpApp",
        name: "aacpApp",
        component: () => import("@/views/aacp/AacpApp.vue"),
        meta: { breadcrumb: "AACP应用" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "model",
        name: "model",
        component: () => import("@/views/aacp/Model.vue"),
        meta: { breadcrumb: "模型变体" },
      }),
    ];
  }
}
