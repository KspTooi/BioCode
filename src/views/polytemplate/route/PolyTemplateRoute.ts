import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class PolyTemplateRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "assembly",
        path: "polyTemplate",
        name: "polyTemplate",
        component: () => import("@/views/assembly/polyTemplate/PolyTemplate.vue"),
        meta: { breadcrumb: "聚合模板" },
      }),
    ];
  }
}
