import { createApp, markRaw } from "vue";
import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import AdminRoot from "@/AdminRoot.vue";
// 导入 Element Plus
import ElementPlus from "element-plus";
import "@/styles/element-theme.scss";
import "@/assets/tailwind.css";
// 导入中文语言包
import zhCn from "element-plus/dist/locale/zh-cn.mjs";

// 导入Element Plus图标并进行全局注册
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
// 导入并设置 Iconify
import { setupIconify } from "@/commons/Iconify.ts";
import GenricRouteService from "@/soa/genric-route/service/GenricRouteService.ts";
import ComPage404 from "@/soa/com-series/ComPage404.vue";
import ComPage401 from "@/soa/com-series/ComPage401.vue";
import ComPageLanding from "@/soa/com-series/ComPageLanding.vue";
import StdIframe from "@/soa/std-series/StdIframe.vue";
import AuthRouteRegister from "@/views/auth/route/AuthRouteRegister";
import CoreRouteRegister from "@/views/core/route/CoreRouteRegister";
import AuditRouteRegister from "@/views/audit/route/AuditRouteRegister";
import QtRouteRegister from "@/views/qt/route/QtRouteRegister.ts";
import QfRouteRegister from "@/views/qf/route/QfRouteRegister.ts";
import AssemblyRouteRegister from "@/views/assembly/route/AssemblyRouteRegister.ts";
import PlayGroundRouteRegister from "@/views/playground/route/PlayGroundRouteRegister";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import DefaultLayout from "@/soa/layout-series-default/DefaultLayout.vue";
import ComLayoutProviderService from "@/soa/com-series/service/ComLayoutProviderService.ts";
import AacpRouteRegister from "@/views/aacp/route/AacpRouteRegister";
import ComAuthProviderService from "@/soa/com-series/service/ComAuthProvider.ts";
import UserLogin from "@/views/auth/UserLogin.vue";
import PatLogin from "@/views/auth/PatLogin.vue";
import NeoLayout from "@/soa/layout-series-neo/NeoFramework.vue";
/**
 * 固定路由 这些路由不会被GenricRouteService动态注册 请注意不要随意修改这些路由，因为它们游离于业务域之外，会引发严重的路由冲突问题。
 */
const grsFixedRoutes = [
  {
    path: "/index",
    name: "index",
    component: ComPageLanding,
    meta: {},
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: ComPage404,
    meta: {
      layout: "blank",
    },
  },
  {
    path: "/no-permission",
    name: "no-permission",
    component: ComPage401,
    meta: {
      layout: "blank",
    },
  },
  {
    path: "/external-link",
    name: "external-link",
    component: StdIframe,
    meta: {
      keepAlive: true,
    },
  },
];

/**
 * 固定标签页 这些标签不会被关闭，且始终位于标签栏前部
 */
const ctsFixedTabs = [
  {
    id: "index",
    icon: null,
    title: "首页",
    path: "/index",
    closable: false,
    kind: "normal" as const,
  },
];

//初始化 Iconify
setupIconify();

//注册多布局
ComLayoutProviderService.registerLayout("default", DefaultLayout);
ComLayoutProviderService.registerLayout("neo", NeoLayout);

//设置默认布局（路由 meta.layout 为 default 或未指定时生效）
ComLayoutProviderService.setDefaultLayout("neo");

//注册多认证组件
ComAuthProviderService.registerAuth("default", UserLogin);
ComAuthProviderService.registerAuth("pat", PatLogin);

//设置默认认证组件
ComAuthProviderService.setDefaultAuth("default");

//创建应用实例
const app = createApp(AdminRoot);

//注册所有图标并使用 markRaw 包装
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, markRaw(component));
}

//注册Pinia 并加入持久化插件
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

//在标签服务中注册固定标签页
ComTabService.addFixedTabs(ctsFixedTabs);

const { initialize, addRoute } = GenricRouteService.useGenricRoute();

//在SOA路由服务中注册域业务路由
addRoute(new AuthRouteRegister());
addRoute(new CoreRouteRegister());
addRoute(new AuditRouteRegister());
addRoute(new QtRouteRegister());
addRoute(new QfRouteRegister());
addRoute(new PlayGroundRouteRegister());
addRoute(new AacpRouteRegister());
addRoute(new AssemblyRouteRegister());

//初始化SOA路由服务
initialize(app, grsFixedRoutes);

// 使用Element Plus并设置为中文
app.use(ElementPlus, {
  locale: zhCn,
});

//挂载应用
app.mount("#app");
