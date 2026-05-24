import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo.ts";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";

export default class PlayGroundRouteRegister extends GenricRouteRegister {
  public doRegister(): RouteEntryPo[] {
    return [
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-adv-tree",
        name: "高级树组件演示",
        component: () => import("@/views/playground/PgStdAdvTree.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-input-user-selector",
        name: "人员选择器演示",
        component: () => import("@/views/playground/PgInputUserSelector.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-input-org-tree",
        name: "组织机构选择器演示",
        component: () => import("@/views/playground/PgInputOrgTree.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-customize-tag-select",
        name: "自定义标签选择器演示",
        component: () => import("@/views/playground/PgStdCustomizeTagSelect.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-iframe",
        name: "Iframe容器演示",
        component: () => import("@/views/playground/PgStdIframe.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-expand-button",
        name: "展开按钮演示",
        component: () => import("@/views/playground/PgStdExpandButton.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-icon-picker",
        name: "图标选择器演示",
        component: () => import("@/views/playground/PgStdIconPicker.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-date-range",
        name: "日期范围选择器演示",
        component: () => import("@/views/playground/PgStdDateRange.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-list-area-action",
        name: "列表操作区组件演示",
        component: () => import("@/views/playground/PgStdListAreaAction.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-list-container",
        name: "列表容器演示",
        component: () => import("@/views/playground/PgStdListContainer.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-list-area-table",
        name: "列表区域表格演示",
        component: () => import("@/views/playground/PgStdListAreaTable.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-list-area-query",
        name: "列表区域查询容器演示",
        component: () => import("@/views/playground/PgStdListAreaQuery.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-pg-layout",
        name: "演示页布局组件演示",
        component: () => import("@/views/playground/PgStdPgLayout.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-query-collapse",
        name: "查询区域折叠组件演示",
        component: () => import("@/views/playground/PgStdQueryCollapse.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-list-layout",
        name: "列表页布局演示",
        component: () => import("@/views/playground/PgStdListLayout.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-table-check-column",
        name: "表格勾选列演示",
        component: () => import("@/views/playground/PgStdTableCheckColumn.vue"),
        meta: {},
      }),
      RouteEntryPo.build({
        biz: "playground",
        path: "pg-std-time-range",
        name: "时间范围选择器演示",
        component: () => import("@/views/playground/PgStdTimeRange.vue"),
        meta: {},
      }),
    ];
  }
}
