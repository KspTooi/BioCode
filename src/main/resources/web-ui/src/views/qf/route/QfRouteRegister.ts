import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

export default class QfRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "qf",
        path: "qfModelGroup",
        name: "qfModelGroup",
        component: () => import("@/views/qf/QfModelGroup.vue"),
        meta: { breadcrumb: "流程模型分组" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfModel",
        name: "qfModel",
        component: () => import("@/views/qf/QfModel.vue"),
        meta: { breadcrumb: "流程模型" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfModelDeployRcd",
        name: "qfModelDeployRcd",
        component: () => import("@/views/qf/QfModelDeployRcd.vue"),
        meta: { breadcrumb: "流程模型部署历史" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfModelDesigner",
        name: "qfModelDesigner",
        component: () => import("@/views/qf/sfc_private/QfModelDeginer.vue"),
        meta: { breadcrumb: "Flowable 流程设计" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfBizForm",
        name: "qfBizForm",
        component: () => import("@/views/qf/QfBizForm.vue"),
        meta: { breadcrumb: "业务表单" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfTodo",
        name: "qfTodo",
        component: () => import("@/views/qf/QfTodo.vue"),
        meta: { breadcrumb: "待办事项" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfCc",
        name: "qfCc",
        component: () => import("@/views/qf/QfCc.vue"),
        meta: { breadcrumb: "抄送" },
      }),
      RouteEntryPo.build({
        biz: "qf",
        path: "qfApprove",
        name: "qfApprove",
        component: () => import("@/views/qf/QfProc.vue"),
        meta: { breadcrumb: "流程办理" },
      }),
    ];
  }
}
