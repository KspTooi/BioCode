import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class PolyTemplateFieldRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "assembly",
        path: "polyTemplateField",
        name: "polyTemplateField",
        component: () => import("@/views/assembly/polyTemplateField/PolyTemplateField.vue"),
        meta: { breadcrumb: "聚合模板字段" },
      }),
    ];
  }
}
