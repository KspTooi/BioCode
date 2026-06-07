import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class AacpCapabilityRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "aacp",
        path: "aacpCapability",
        name: "aacpCapability",
        component: () => import("@/views/aacp/aacpCapability/AacpCapability.vue"),
        meta: { breadcrumb: "能力包" },
      }),
    ];
  }
}
