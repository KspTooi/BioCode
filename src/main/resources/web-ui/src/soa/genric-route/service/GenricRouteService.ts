import { createRouter, createWebHashHistory, type Router } from "vue-router";
import { RouteEntryPo } from "@/soa/genric-route/api/RouteEntryPo";
import { type App, ref } from "vue";
import ComPage404 from "@/soa/com-series/ComPage404.vue";
import ComPage401 from "@/soa/com-series/ComPage401.vue";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";
import ComPageLanding from "@/soa/com-series/ComPageLanding.vue";
import ComTabService from "@/soa/com-series/service/ComTabService";

/**
 * 全局路由服务(GRS)
 * GRS是整个项目的路由服务，负责管理整个项目的路由，包括路由的注册、删除、获取等。它是整个项目中路由的单一事实来源。
 * GRS的注册方式是通过注册器(GRR)来注册的，注册器是一个抽象类，需要子类来实现，在子类里面可以注册多条路由、配置前置守卫和后置守卫。
 *
 * 注意：你不可以绕过GRS直接操作Vue路由，这会破坏整个项目的路由一致性，也会破坏路由表和GRS的注册机制，严重时会导致项目无法正常运行。
 *
 */

//是否已初始化
let hasInitialized = false;

//路由表
const routes = ref<RouteEntryPo[]>([]);

//路由注册器
const routeRegistries = ref<GenricRouteRegister[]>([]);

/**
 * Vue路由
 */
const vueRouter = createRouter({
  history: createWebHashHistory(),
  routes: [
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
  ],
});

//保存原始的写方法 GRS内部走这两个引用
const rawAddRoute = vueRouter.addRoute.bind(vueRouter);
const rawRemoveRoute = vueRouter.removeRoute.bind(vueRouter);

//防止外部绕过GRS直接操作Vue路由,这会导致路由管理混乱出现不同步问题。
const buildForbiddenFn = (methodName: string) => {
  return () => {
    throw new Error(`Vue路由管理器不支持直接调用, router.${methodName} 请通过 GenricRouteService(GRS) 进行路由管理`);
  };
};

Object.defineProperty(vueRouter, "addRoute", {
  value: buildForbiddenFn("addRoute"),
  writable: false,
  configurable: false,
});

Object.defineProperty(vueRouter, "removeRoute", {
  value: buildForbiddenFn("removeRoute"),
  writable: false,
  configurable: false,
});

// 路由守卫
vueRouter.beforeEach((to, from) => {
  // 仅在访问根路径时尝试恢复标签页，其他路径直接放行
  if (to.path !== "/") {
    return;
  }

  const { tabs, getActiveTab } = ComTabService.useTabService();

  //获取当前激活标签
  const activeTab = getActiveTab();

  // 优先恢复当前激活标签，但排除根路径和登录页，避免自跳转/无意义跳转
  if (activeTab && activeTab.path !== "/" && activeTab.path !== "/auth/login" && activeTab.path !== to.path) {
    return activeTab.path;
  }

  // 激活标签不可用时，回退到最近访问的业务标签（同样排除根路径和登录页）
  const fallbackTab = [...tabs.value].reverse().find((t) => t.path !== "/" && t.path !== "/auth/login");

  // 防止重定向到当前目标，避免产生循环跳转
  if (fallbackTab && fallbackTab.path !== to.path) {
    return fallbackTab.path;
  }
});

export default {
  /**
   * 获取Vue路由
   * @returns Vue路由
   */
  getVueRouter(): Router {
    return vueRouter;
  },

  /**
   * 使用全局路由服务
   */
  useGenricRoute() {
    /**
     * 初始化路由服务
     * @param app 应用实例
     */
    const initialize = (app: App): void => {
      if (hasInitialized) {
        return;
      }
      hasInitialized = true;

      //注册路由注册器
      routeRegistries.value.forEach((register: GenricRouteRegister) => {
        //注册路由
        addRoutes(register.doRegister());

        //注册前置守卫
        if (register.doBeforeEach()) {
          vueRouter.beforeEach(register.doBeforeEach());
        }

        //注册后置守卫
        if (register.doAfterEach()) {
          console.log("注册后置守卫", register.doAfterEach());
          vueRouter.afterEach(register.doAfterEach);
        }
      });

      app.use(vueRouter);
    };

    /**
     * 添加路由
     * @param entry 路由条目或路由注册器
     */
    const addRoute = (entry: RouteEntryPo | GenricRouteRegister): void => {
      //如果是路由注册器 则注册到路由注册器列表
      if (entry instanceof GenricRouteRegister) {
        routeRegistries.value.push(entry);
        return;
      }

      //校验路由条目
      entry.validate();

      let hasConflict = false;

      for (const route of routes.value) {
        //查找同名路由
        if (route.name === entry.name) {
          //删除Vue路由
          rawRemoveRoute(route.name);

          //更新路由条目
          route.biz = entry.biz;
          route.path = entry.path;
          route.name = entry.name;
          route.component = entry.component;
          route.meta = entry.meta;
          hasConflict = true;
          break;
        }
      }

      const breadcrumbTitle = entry.meta.breadcrumb;

      //如果无冲突 同时更新路由表+Vue路由
      if (!hasConflict) {
        //更新路由表
        routes.value.push(entry);

        //添加Vue路由
        rawAddRoute({
          path: entry.buildPath(),
          name: entry.name,
          component: entry.component,
          meta: {
            keepAlive: entry.meta.keepAlive,
            breadcrumb: breadcrumbTitle,
            layout: entry.meta.layout,
          },
        });
      }

      //路由有名称冲突 只更新Vue路由,不更新内置路由表
      if (hasConflict) {
        rawAddRoute({
          path: entry.buildPath(),
          name: entry.name,
          component: entry.component,
          meta: {
            keepAlive: entry.meta.keepAlive,
            breadcrumb: breadcrumbTitle,
            layout: entry.meta.layout,
          },
        });
      }
    };

    /**
     * 批量注册路由
     * @param entries 路由条目数组
     */
    const addRoutes = (entries: RouteEntryPo[]): void => {
      entries.forEach((entry) => addRoute(entry));
    };

    /**
     * 删除路由
     * @param name 路由名称
     */
    const removeRoute = (name: string): void => {
      rawRemoveRoute(name);
      routes.value = routes.value.filter((route) => route.name !== name);
    };

    /**
     * 获取路由表
     * @returns 路由表的副本 对副本操作不会改变原始路由表
     */
    const getRoutes = (): RouteEntryPo[] => {
      const result: RouteEntryPo[] = [];

      //遍历路由表
      for (const item of routes.value) {
        const po = new RouteEntryPo();
        po.biz = item.biz;
        po.path = item.path;
        po.name = item.name;
        po.component = item.component;
        po.meta = item.meta;
        result.push(po);
      }

      return result;
    };

    /**
     * 根据名称或路径获取路由
     * 优先匹配名称 其次匹配路径，只要有任意一个匹配成功就返回
     *
     * 请注意: 该方法返回的是路由PO对象 而不是Vue路由对象，该项目中的所有Vue路由都需要通过GRS进行注册，(调用GRS的addRoute方法)
     * 如果你绕过GRS直接操作Vue路由，将无法保证路由的正确性。
     *
     * @param nameOrPath 路由名称或路径
     * @returns 路由条目 如果无对应路由则返回null
     */
    const getRouteByNameOrPath = (nameOrPath: string): RouteEntryPo | null => {
      //先根据名称查找
      const routeByName = routes.value.find((route) => route.name === nameOrPath);
      if (routeByName) {
        return routeByName;
      }

      //再根据路径查找
      const routeByPath = routes.value.find((route) => route.path === nameOrPath);
      if (routeByPath) {
        return routeByPath;
      }

      //无匹配路由
      return null;
    };

    return {
      initialize,
      addRoute,
      addRoutes,
      removeRoute,
      getRoutes,
      getRouteByNameOrPath,
    };
  },
};
