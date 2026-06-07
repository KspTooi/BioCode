import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class AacpMcpRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "aacp",
        path: "aacpMcp",
        name: "aacpMcp",
        component: () => import("@/views/aacp/aacpMcp/AacpMcp.vue"),
        meta: { breadcrumb: "MCP服务器" },
      }),
    ];
  }
}
