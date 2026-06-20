import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

/**
 * 代码生成路由注册
 */
export default class AssemblyRouteRegister extends GenricRouteRegister {
  /**
   * 注册代码生成路由
   * @returns 路由条目数组
   */
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "assembly",
        path: "data-source-manager",
        name: "data-source-manager",
        component: () => import("@/views/assembly/DataSource.vue"),
        meta: { breadcrumb: "数据源管理" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "ty-schema-manager",
        name: "ty-schema-manager",
        component: () => import("@/views/assembly/TymSchema.vue"),
        meta: { breadcrumb: "类型映射方案管理" },
      }),
      // 类型映射方案字段管理（通常由 TymSchema 通过 CDRC 跳转进入）
      RouteEntryPo.build({
        biz: "assembly",
        path: "tym-schema-field-manager",
        name: "tym-schema-field-manager",
        component: () => import("@/views/assembly/TymSchemaField.vue"),
        meta: { breadcrumb: "类型映射方案字段管理" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "scm-manager",
        name: "scm-manager",
        component: () => import("@/views/assembly/Scm.vue"),
        meta: { breadcrumb: "SCM管理" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "op-schema-manager",
        name: "op-schema-manager",
        component: () => import("@/views/assembly/OpSchema.vue"),
        meta: { breadcrumb: "输出方案管理" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "op-schema-design",
        name: "op-schema-design",
        component: () => import("@/views/assembly/OpSchemaDesign.vue"),
        meta: { breadcrumb: "输出方案设计" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "op-schema-preview",
        name: "op-schema-preview",
        component: () => import("@/views/assembly/OpSchemaPreview.vue"),
        meta: { breadcrumb: "蓝图预览" },
      }),
      RouteEntryPo.build({
        biz: "assembly",
        path: "polyTemplate",
        name: "polyTemplate",
        component: () => import("@/views/assembly/polyTemplate/PolyTemplate.vue"),
        meta: { breadcrumb: "聚合模板" },
      }),
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
