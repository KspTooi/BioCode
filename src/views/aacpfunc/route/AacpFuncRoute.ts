import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class AacpFuncRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "accp",
        path: "aacpFunc",
        name: "aacpFunc",
        component: () => import("@/views/accp/aacpFunc/AacpFunc.vue"),
        meta: { breadcrumb: "微函数" },
      }),
    ];
  }
}
