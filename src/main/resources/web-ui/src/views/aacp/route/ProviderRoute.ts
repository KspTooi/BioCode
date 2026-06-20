import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";


export default class ProviderRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
	  //如果该域已经有一个路由注册器了，你需要把下面的内容合入到现有的路由注册器，然后删除本文件
      RouteEntryPo.build({
        biz: "aacp",
        path: "provider",
        name: "provider",
        component: () => import("@/views/aacp/Provider.vue"),
        meta: { breadcrumb: "模型供应商" },
      }),
    ];
  }
}
