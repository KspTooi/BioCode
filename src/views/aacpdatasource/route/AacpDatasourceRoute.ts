import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class AacpDatasourceRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "aacp",
        path: "aacpDatasource",
        name: "aacpDatasource",
        component: () => import("@/views/aacp/aacpDatasource/AacpDatasource.vue"),
        meta: { breadcrumb: "AACP数据源" },
      }),
    ];
  }
}
