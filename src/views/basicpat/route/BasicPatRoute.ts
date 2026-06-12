import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class BasicPatRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "auth",
        path: "basicPat",
        name: "basicPat",
        component: () => import("@/views/auth/basicPat/BasicPat.vue"),
        meta: { breadcrumb: "基本PAT" },
      }),
    ];
  }
}
