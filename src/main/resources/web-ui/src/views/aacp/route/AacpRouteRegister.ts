import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

export default class AacpRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "aacp",
        path: "mcp-server",
        name: "mcp-server",
        component: () => import("@/views/aacp/AacpAgentHub.vue"),
        meta: { breadcrumb: "MCP服务器" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "capability",
        name: "capability",
        component: () => import("@/views/aacp/AacpCap.vue"),
        meta: { breadcrumb: "能力包" },
      }),
      RouteEntryPo.build({
        biz: "aacp",
        path: "func",
        name: "func",
        component: () => import("@/views/aacp/AacpMicroFunc.vue"),
        meta: { breadcrumb: "微函数" },
      }),
    ];
  }
}
